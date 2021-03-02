package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wiktionary;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import org.apache.jena.query.*;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService.PreconfiguredPersistences.*;

/**
 * Class utilizing DBnary, a SPARQL endpoint for Wiktionary.
 */
public class WiktionaryKnowledgeSource extends SemanticWordRelationDictionary {


    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WiktionaryKnowledgeSource.class);

    /**
     * directory where the TDB database with the wiktionary files lies
     */
    public String tdbDirectoryPath;

    /**
     * Service responsible for disk buffers.
     */
    private PersistenceService persistenceService;

    /**
     * Buffer for synonyms.
     */
    private ConcurrentMap<String, HashSet<String>> synonymyBuffer;

    /**
     * Buffer for hypernymy.
     */
    private ConcurrentMap<String, HashSet<String>> hypernymyBuffer;

    /**
     * Buffer for ask queries.
     */
    private ConcurrentMap<String, Boolean> askBuffer;

    /**
     * The TDB dataset into which the dbnary data set was loaded.
     */
    private Dataset tdbDataset;

    /**
     * The public SPARQL endpoint.
     */
    private static final String ENDPOINT_URL = "http://kaiko.getalp.org/sparql";

    /**
     * True if a tdb source shall be used rather than an on-line SPARQL endpoint.
     */
    private boolean isUseTdb = false;

    private boolean isDiskBufferEnabled;

    /**
     * The linker that links input strings to terms.
     */
    private WiktionaryLinker linker;

    /**
     * Constructor for Wiktionary online (SPARQL endpoint) access.
     * By default, a disk-buffer is enabled.
     */
    public WiktionaryKnowledgeSource(){
        this(true);
    }

    /**
     * Constructor
     * @param isDiskBufferEnabled True if buffers shall be written to disk.
     */
    public WiktionaryKnowledgeSource(boolean isDiskBufferEnabled){
        isUseTdb = false;
        this.isDiskBufferEnabled = isDiskBufferEnabled;
        initialize();
    }

    /**
     * Constructor for DBnary TDB access.
     *
     * @param tdbDirectoryPath Path to the Wiktionary <a href="https://jena.apache.org/documentation/tdb/index.html">TDB</a>
     *                         directory.
     */
    public WiktionaryKnowledgeSource(String tdbDirectoryPath) {
        this.tdbDirectoryPath = tdbDirectoryPath;

        // convenience checks for stable code
        File tdbDirectoryFile = new File(tdbDirectoryPath);
        if (!tdbDirectoryFile.exists()) {
            LOGGER.error("tdbDirectoryPath does not exist. - ABORTING PROGRAM");
            return;
        }
        if (!tdbDirectoryFile.isDirectory()) {
            LOGGER.error("tdbDirectoryPath is not a directory. - ABORTING PROGRAM");
            return;
        }
        this.isUseTdb = true;

        // dataset and model creation
        //tdbDataset = TDB2Factory.connectDataset(tdbDirectoryPath);
        tdbDataset = TDBFactory.createDataset(tdbDirectoryPath);
        tdbDataset.begin(ReadWrite.READ);
        this.isDiskBufferEnabled = false;
        initialize();
    }

    /**
     * Helper functions for constructor-independent actions.
     */
    private void initialize(){
        if(isDiskBufferEnabled){
            this.persistenceService = PersistenceService.getService();
            this.synonymyBuffer = persistenceService.getMapDatabase(WIKTIONARY_SYNONYMY_BUFFER);
            this.hypernymyBuffer = persistenceService.getMapDatabase(WIKTIONARY_HYPERNYMY_BUFFER);
            this.askBuffer = persistenceService.getMapDatabase(WIKTIONARY_ASK_BUFFER);
        } else {
            this.synonymyBuffer = new ConcurrentHashMap<>();
            this.hypernymyBuffer = new ConcurrentHashMap<>();
            this.askBuffer = new ConcurrentHashMap<>();
        }
        linker = new WiktionaryLinker(this);
    }

    /**
     * De-constructor; call before ending the program.
     */
    public void close() {
        commitAll();
        if(tdbDataset != null) {
            tdbDataset.end();
            tdbDataset.close();
        }
        LOGGER.info("Dataset closed.");
    }

    public boolean isInDictionary(String word) {
        return this.isInDictionary(word, Language.ENGLISH);
    }

    /**
     * Language dependent query for existence in the dbnary dictionary.
     * Note that case-sensitivity applies ( (Katze, deu) can be found whereas (katze, deu) will not return any results ).
     *
     * @param word     The word to be looked for.
     * @param language The language of the word.
     * @return boolean indicating whether the word exists in the dictionary in the corresponding language.
     */
    public boolean isInDictionary(String word, Language language) {
        word = encodeWord(word);
        String key = "in_dict_" + word + "_" + language.toSparqlChar2();
        if(askBuffer.containsKey(key)){
            return askBuffer.get(key);
        }

        String queryString =
                "PREFIX lexvo: <http://lexvo.org/id/iso639-3/>\r\n" +
                        "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\r\n" +
                        "ASK {  <http://kaiko.getalp.org/dbnary/" + language.toWiktionaryChar3() + "/" + word + "> ?p ?o . }";
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution;
        if(isUseTdb) {
            queryExecution = QueryExecutionFactory.create(query, tdbDataset);
        } else {
            queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
        }
        boolean result = queryExecution.execAsk();
        queryExecution.close();
        askBuffer.put(key, result);
        commit(WIKTIONARY_ASK_BUFFER);
        return result;
    }

    @Override
    public Set<String> getSynonymsLexical(String linkedConcept) {
        if (linkedConcept != null) {
            HashSet<String> result = getSynonyms(linkedConcept, Language.ENGLISH);
            if (result.size() == 0) {
                return null;
            } else return result;
        } else {
            return null;
        }
    }

    /**
     * Retrieves the synonyms of a particular word in a particular language.
     *
     * @param word     Word for which the synonyms shall be retrieved.
     * @param language Language of the word.
     * @return Set of synonyms.
     */
    public HashSet<String> getSynonyms(String word, Language language) {
        word = encodeWord(word);
        if (synonymyBuffer.containsKey(word + "_" + language.toWiktionaryChar3())) {
            return synonymyBuffer.get(word + "_" + language.toWiktionaryChar3());
        }
        HashSet<String> result = new HashSet<>();
        String queryString =
                "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\r\n" +
                        "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>\r\n" +
                        "select distinct ?synonym WHERE {\r\n" +
                        "\r\n" +
                        "{" +
                        // synonyms of described concepts
                        "select distinct ?synonym where {\r\n" +
                        "<http://kaiko.getalp.org/dbnary/" + language.toWiktionaryChar3() + "/" + word + "> <http://kaiko.getalp.org/dbnary#describes> ?descriptionConcepts .\r\n" +
                        "?descriptionConcepts dbnary:synonym ?synonym .\r\n" +
                        "}" +
                        "}\r\n" +
                        "UNION\r\n" +
                        // and now synonyms of senses
                        "{\r\n" +
                        "select distinct ?synonym where {\r\n" +
                        "<http://kaiko.getalp.org/dbnary/" + language.toWiktionaryChar3() + "/" + word + "> <http://kaiko.getalp.org/dbnary#describes> ?descriptionConcepts .\r\n" +
                        "?descriptionConcepts ontolex:sense ?sense .\r\n" +
                        "?sense dbnary:synonym ?synonym .\r\n" +
                        "}\r\n" +
                        "}\r\n" +
                        "}";
        //System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution;
        if(isUseTdb) {
            queryExecution = QueryExecutionFactory.create(query, tdbDataset);
        } else {
            queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
        }
        ResultSet queryResult = queryExecution.execSelect();
        while (queryResult.hasNext()) {
            result.add(getLemmaFromURI(queryResult.next().getResource("synonym").toString()));
        }
        queryExecution.close();
        synonymyBuffer.put(word + "_" + language.toWiktionaryChar3(), result);
        commit(WIKTIONARY_SYNONYMY_BUFFER);
        return result;
    }

    /**
     * Given a resource URI, this method will transform it to a lemma.
     *
     * @param uri Resource URI to be transformed.
     * @return Lemma.
     */
    private static String getLemmaFromURI(String uri) {
        return uri.substring(35, uri.length()).replace("_", " ");
    }

    /**
     * Encodes words so that they can be looked up in the wiktionary dictionary.
     *
     * @param word Word to be encoded.
     * @return encoded word
     */
    static String encodeWord(String word) {
        // we cannot use the Java default encoder due to some ideosyncratic encodings
        word = word.replace("%", "%25");
        word = word.replace(" ", "_");
        word = word.replace(".", "%2E");
        word = word.replace("^", "%5E");
        return word;
    }

    /**
     * Obtain hypernyms for the given concept. The assumed language is English.
     *
     * @param linkedConcept The linked concept for which hypernyms shall be retrieved.
     * @return A set of hypernyms.
     */
    @Override
    public HashSet<String> getHypernyms(String linkedConcept) {
        return this.getHypernyms(linkedConcept, Language.ENGLISH);
    }

    /**
     * Obtain hypernyms for the given concept.
     *
     * @param linkedConcept The linked concept for which hypernyms shall be retrieved.
     * @param language      The desired language of the hypernyms.
     * @return A set of hypernyms.
     */
    public HashSet<String> getHypernyms(String linkedConcept, Language language) {
        if (linkedConcept == null) return null;
        linkedConcept = encodeWord(linkedConcept);
        if (hypernymyBuffer.containsKey(linkedConcept + "_" + linkedConcept)) {
            return hypernymyBuffer.get(linkedConcept + "_" + linkedConcept);
        }
        HashSet<String> result = new HashSet<>();
        String queryString = "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX dbnarylan: <http://kaiko.getalp.org/dbnary/eng/>\n" +
                "SELECT distinct ?hypernym {\n" +
                "{select ?hypernym where {\n" +
                "dbnarylan:" + linkedConcept + " dbnary:hypernym ?hypernym.\n" +
                "?hypernym rdf:type dbnary:Page .}}\n" +
                "UNION\n" +
                "{select ?hypernym where {\n" +
                "?hs dbnary:hyponym dbnarylan:" + linkedConcept + " .\n" +
                "?hypernym dbnary:describes ?hs .\n" +
                "?hypernym rdf:type dbnary:Page .}}\n" +
                "UNION\n" +
                "{select ?hypernym where {\n" +
                "dbnarylan:" + linkedConcept + " dbnary:describes ?dc .\n" +
                "?dc dbnary:hypernym ?hypernym.\n" +
                "?hypernym rdf:type dbnary:Page .\n" +
                "}}}";
        try {
            Query query = QueryFactory.create(queryString);

            QueryExecution queryExecution;
            if(isUseTdb) {
                queryExecution = QueryExecutionFactory.create(query, tdbDataset);
            } else {
                queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
            }
            ResultSet queryResult = queryExecution.execSelect();
            while (queryResult.hasNext()) {
                result.add(getLemmaFromURI(queryResult.next().getResource("hypernym").toString()));
            }
            queryExecution.close();
            hypernymyBuffer.put(linkedConcept + "_" + language.toWiktionaryChar3(), result);
            commit(WIKTIONARY_HYPERNYMY_BUFFER);
            return result;
        } catch (QueryParseException qpe) {
            LOGGER.info("Faild to build query for concept '" + linkedConcept + "'", qpe);
            return null;
        }
    }

    /**
     * Commit persistence.
     * @param persistence The persistence that is to be commited.
     */
    private void commit(PersistenceService.PreconfiguredPersistences persistence){
        if(persistence == null || persistenceService == null){
            return;
        }
        switch (persistence){
            case WIKTIONARY_SYNONYMY_BUFFER:
                persistenceService.commit(WIKTIONARY_SYNONYMY_BUFFER);
                return;
            case WIKTIONARY_HYPERNYMY_BUFFER:
                persistenceService.commit(WIKTIONARY_HYPERNYMY_BUFFER);
                return;
            case WIKTIONARY_ASK_BUFFER:
                persistenceService.commit(WIKTIONARY_ASK_BUFFER);
        }
    }

    /**
     * Commit data changes if active.
     */
    private void commitAll(){
        if(isDiskBufferEnabled && persistenceService != null){
            persistenceService.commit(WIKTIONARY_SYNONYMY_BUFFER);
            persistenceService.commit(WIKTIONARY_HYPERNYMY_BUFFER);
            persistenceService.commit(WIKTIONARY_ASK_BUFFER);
        }
    }

    @Override
    public LabelToConceptLinker getLinker() {
        return this.linker;
    }

    @Override
    public String getName() {
        return "Wiktionary";
    }
}
