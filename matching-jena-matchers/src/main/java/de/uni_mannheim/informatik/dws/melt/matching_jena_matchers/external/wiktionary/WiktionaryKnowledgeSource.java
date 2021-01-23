package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wiktionary;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import org.apache.jena.query.*;
//import org.apache.jena.tdb.TDB2Factory;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class utilizing DBnary.
 * DBnary endpoint for tests:
 */
public class WiktionaryKnowledgeSource extends SemanticWordRelationDictionary {

    /**
     * Logger for this class.
     */
    private static Logger LOGGER = LoggerFactory.getLogger(WiktionaryKnowledgeSource.class);

    /**
     * directory where the TDB database with the wiktionary files lies
     */
    public String tdbDirectoryPath;

    /**
     * Buffer for synonyms.
     */
    private HashMap<String, HashSet<String>> synonymyBuffer;

    /**
     * Buffer for hypernymy.
     */
    private HashMap<String, HashSet<String>> hypernymyBuffer;

    /**
     * The TDB dataset into which the dbnary data set was loaded.
     */
    private Dataset tdbDataset;

    /**
     * The linker that links input strings to terms.
     */
    private WiktionaryLinker linker;

    /**
     * Constructor
     * @param tdbDirectoryPath Path to the Wiktionary <a href="https://jena.apache.org/documentation/tdb/index.html">TDB</a>
     *                         directory.
     */
    public WiktionaryKnowledgeSource(String tdbDirectoryPath){
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

            synonymyBuffer = new HashMap<>();
            hypernymyBuffer = new HashMap<>();

            // dataset and model creation
            //tdbDataset = TDB2Factory.connectDataset(tdbDirectoryPath);
            tdbDataset = TDBFactory.createDataset(tdbDirectoryPath);
            tdbDataset.begin(ReadWrite.READ);

            linker = new WiktionaryLinker(this);
    }


    /**
     * De-constructor; call before ending the program.
     */
    public void close() {
        tdbDataset.end();
        tdbDataset.close();
        LOGGER.info("Dataset closed.");
    }


    @Override
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
        String queryString =
                "PREFIX lexvo: <http://lexvo.org/id/iso639-3/>\r\n" +
                        "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\r\n" +
                        "ASK {  <http://kaiko.getalp.org/dbnary/" + language.toWiktionaryChar3() + "/" + word + "> ?p ?o . }";
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, tdbDataset);
        return queryExecution.execAsk();
    }


    @Override
    public HashSet<String> getSynonyms(String linkedConcept) {
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
        QueryExecution queryExecution = QueryExecutionFactory.create(query, tdbDataset);
        ResultSet queryResult = queryExecution.execSelect();
        while (queryResult.hasNext()) {
            result.add(getLemmaFromURI(queryResult.next().getResource("synonym").toString()));
        }
        synonymyBuffer.put(word + "_" + language.toWiktionaryChar3(), result);
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
    private static String encodeWord(String word) {
        word = word.replace(" ", "_");
        word = word.replace(".", "%2E");
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
     * @param language The desired language of the hypernyms.
     * @return A set of hypernyms.
     */
    public HashSet<String> getHypernyms(String linkedConcept, Language language) {
        linkedConcept = encodeWord(linkedConcept);
        if (hypernymyBuffer.containsKey(linkedConcept + "_" + linkedConcept.toString())) {
            return hypernymyBuffer.get(linkedConcept + "_" + linkedConcept.toString());
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
                "?hs dbnary:hyponym dbnarylan:" + linkedConcept +" .\n" +
                "?hypernym dbnary:describes ?hs .\n" +
                "?hypernym rdf:type dbnary:Page .}}\n" +
                "UNION\n" +
                "{select ?hypernym where {\n" +
                "dbnarylan:" + linkedConcept +" dbnary:describes ?dc .\n" +
                "?dc dbnary:hypernym ?hypernym.\n" +
                "?hypernym rdf:type dbnary:Page .\n" +
                "}}}";
        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution = QueryExecutionFactory.create(query, tdbDataset);
            ResultSet queryResult = queryExecution.execSelect();
            while (queryResult.hasNext()) {
                result.add(getLemmaFromURI(queryResult.next().getResource("hypernym").toString()));
            }
            hypernymyBuffer.put(linkedConcept + "_" + language.toWiktionaryChar3(), result);
            return result;
        } catch (QueryParseException qpe){
            LOGGER.info("Faild to build query for concept '" + linkedConcept + "'", qpe);
            return null;
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
