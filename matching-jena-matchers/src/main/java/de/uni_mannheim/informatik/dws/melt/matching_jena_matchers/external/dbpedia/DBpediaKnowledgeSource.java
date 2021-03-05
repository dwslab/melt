package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.sparql.SparqlServices;
import org.apache.jena.query.*;
import org.apache.jena.tdb.TDBFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService.PreconfiguredPersistences.*;

public class DBpediaKnowledgeSource extends SemanticWordRelationDictionary {


    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DBpediaKnowledgeSource.class);

    /**
     * The public SPARQL endpoint.
     */
    private static final String ENDPOINT_URL = "https://dbpedia.org/sparql";

    private String name = "DBpedia";

    private DBpediaLinker linker;

    /**
     * Buffer for repeated synonymy requests.
     */
    ConcurrentMap<String, Set<String>> synonymyBuffer;

    /**
     * Buffer for repeated hypernymy requests.
     */
    ConcurrentMap<String, Set<String>> hypernymyBuffer;

    /**
     * DBpedia annotates quite a lot of hypernyms. Some of them may be misleading for some analyses such as
     * "http://www.w3.org/2002/07/owl#Thing".
     */
    private Set<String> excludedHypernyms = new HashSet<>();

    /**
     * Service responsible for disk buffers.
     */
    private PersistenceService persistenceService;

    /**
     * If the disk-buffer is disabled, no buffers are read/written from/to the disk.
     * Default: true.
     */
    private boolean isDiskBufferEnabled = true;

    /**
     * True if a tdb source shall be used rather than an on-line SPARQL endpoint.
     */
    private boolean isUseTdb = false;

    public static String getEndpointUrl() {
        return ENDPOINT_URL;
    }

    /**
     * The TDB dataset into which the DBpedia data set was loaded.
     */
    private Dataset tdbDataset;

    /**
     * Default constructor. SPARQL endpoint will be queried.
     * Disk buffer is enabled by default.
     */
    public DBpediaKnowledgeSource(){
        this(true);
    }

    /**
     * Constructor for SPARQL access.
     * @param isDiskBufferEnabled True if a disk buffer shall be enabled by default.
     */
    public DBpediaKnowledgeSource(boolean isDiskBufferEnabled){
        this.isDiskBufferEnabled = isDiskBufferEnabled;
        initializeMembers();
    }

    /**
     * Constructor for DBpedia TDB access.
     * @param tdbDirectoryPath The path to the TDB directory.
     */
    public DBpediaKnowledgeSource(String tdbDirectoryPath){
        this(tdbDirectoryPath, true);
    }

    /**
     * Constructor for DBpedia TDB access.
     * @param tdbDirectoryPath The path to the TDB directory.
     * @param isDiskBufferEnabled True if the disk buffer shall be enabled.
     */
    public DBpediaKnowledgeSource(String tdbDirectoryPath, boolean isDiskBufferEnabled){
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
        tdbDataset = TDBFactory.createDataset(tdbDirectoryPath);
        tdbDataset.begin(ReadWrite.READ);

        this.isDiskBufferEnabled = isDiskBufferEnabled;
        initializeMembers();
    }

    /**
     * Initializations that have to be performed.
     */
    private void initializeMembers(){
        initializeBuffers();
        initializeLinker();
        initializeHypernymExclusion();
    }

    private void initializeHypernymExclusion(){
        // hypernym exclusion default set
        excludedHypernyms.add("http://www.w3.org/2002/07/owl#Thing");
    }

    /**
     * Helper method to initialize the linker.
     */
    private void initializeLinker(){
        if(this.linker == null){
            this.linker = new DBpediaLinker(this);
        }
    }

    /**
     * Initialize buffers (either on-disk or memory).
     */
    private void initializeBuffers(){
        if(isDiskBufferEnabled){
            persistenceService = PersistenceService.getService();
            this.synonymyBuffer = persistenceService.getMapDatabase(DBPEDIA_SYNONYMY_BUFFER);
            this.hypernymyBuffer = persistenceService.getMapDatabase(DBPEDIA_HYPERNYMY_BUFFER);
        } else {
            this.synonymyBuffer = new ConcurrentHashMap<>();
            this.hypernymyBuffer = new ConcurrentHashMap<>();
        }
    }

    public boolean isInDictionary(String word) {
        return linker.linkToSingleConcept(word, Language.ENGLISH) != null;
    }

    @Override
    @NotNull
    public Set<String> getSynonymsLexical(String linkedConcept) {
        Set<String> result = new HashSet<>();
        if(linkedConcept == null || linkedConcept.equals("")){
            return result;
        }
        String key = linkedConcept + "_syns_lexical";
        if (synonymyBuffer.containsKey(key)) {
            return synonymyBuffer.get(key);
        }
        String queryString = getSynonymsLexicalQuery(linkedConcept);
        QueryExecution queryExecution;
        if(isUseTdb){
            queryExecution = QueryExecutionFactory.create(queryString, tdbDataset);
        } else {
            queryExecution = QueryExecutionFactory.sparqlService(getEndpointUrl(), queryString);
        }
        ResultSet resultSet = SparqlServices.safeExecution(queryExecution);
        while(resultSet.hasNext()){
            QuerySolution solution = resultSet.next();
            String label = solution.getLiteral("l").getLexicalForm();
            result.add(label);
        }
        queryExecution.close();
        result.remove("");
        synonymyBuffer.put(key, result);
        commitAll();
        return result;
    }

    /**
     * Builds a String query to obtain synonyms. The synonyms are represented by normal words/labels (not URIs).
     * @param link The link for which synonymous words shall be obtained.
     * @return A SPARQL query as String.
     */
    String getSynonymsLexicalQuery(String link) {
        Set<String> uris = linker.getUris(link);
        StringBuilder result = new StringBuilder();
        result.append("SELECT DISTINCT ?l WHERE {\n");
        boolean first = true;
        for(String uri : uris) {
            if (first) {
                first = false;
            } else {
                result.append("UNION ");
            }
            result
                    .append(getSubjectPredicateQueryLineForLabels(uri, "http://www.w3.org/2000/01/rdf-schema#label"))
                    .append("UNION ")
                    .append(getSubjectPredicateQueryLineForLabels(uri, "http://xmlns.com/foaf/0.1/name"))
                    .append("UNION ")
                    .append(getSubjectPredicateQueryLineForLabels(uri, "http://dbpedia.org/property/name"))
                    .append("UNION ")
                    .append(getSubjectPredicateQueryLineForLabels(uri, "http://dbpedia.org/property/otherNames"))
                    .append("UNION ")
                    .append(getSubjectPredicateQueryLineForLabels(uri, "http://dbpedia.org/ontology/alias"));
        }
        result.append("}");
        return result.toString();
    }

    /**
     * Helper method to build a query.
     *
     * @param subject   The subject
     * @param predicate The predicate.
     * @return A string builder.
     */
    static StringBuilder getSubjectPredicateQueryLineForLabels(String subject, String predicate) {
        StringBuilder result = new StringBuilder();
        result.append("{<")
                .append(subject)
                .append("> <")
                .append(predicate)
                .append("> ?l }\n");
        return result;
    }

    @Override
    @NotNull
    public Set<String> getHypernyms(String linkedConcept) {
        Set<String> result = new HashSet<>();
        if(linkedConcept == null){
            return result;
        }
        String key = linkedConcept;
        if(hypernymyBuffer.containsKey(key)){
            // we now need to remove the exclusion concepts:
            result.addAll(hypernymyBuffer.get(key));
            result.removeAll(getExcludedHypernyms());
            return result;
        }
        String queryString = getHypernymsQuery(linkedConcept);
        QueryExecution queryExecution;

        if(isUseTdb){
            queryExecution = QueryExecutionFactory.create(queryString, tdbDataset);
        } else {
            queryExecution = QueryExecutionFactory.sparqlService(getEndpointUrl(), queryString);
        }

        ResultSet queryResult = SparqlServices.safeExecution(queryExecution);
        while(queryResult.hasNext()){
            QuerySolution solution = queryResult.next();
            String hypernym = solution.getResource("c").getURI();
            result.add(hypernym);
        }
        queryExecution.close();

        // we add to the buffer before excluding hypernyms
        hypernymyBuffer.put(key, result);
        commitAll();

        result.removeAll(getExcludedHypernyms());
        return result;
    }

    /**
     * Construct a query for hypernyms.
     * @param linkedConcept The concept for which hypernyms shall be retrieved.
     * @return SPARQL query as String.
     */
    private String getHypernymsQuery(String linkedConcept){
        StringBuilder result = new StringBuilder();
        Set<String> uris = linker.getUris(linkedConcept);
        result.append("SELECT DISTINCT ?c WHERE {\n");
        boolean first = true;
        for(String uri : uris){
            if (first) {
                first = false;
            } else {
                result.append("UNION ");
            }
            result.append(getSubjectPredicateQueryLineForConcepts(uri, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
            result.append("UNION ");
            result.append(getSubjectPredicateQueryLineForConcepts(uri, "http://dbpedia.org/ontology/type"));
        }
        result.append("}");
        return result.toString();
    }

    /**
     * Helper method to build a query.
     *
     * @param subject   The subject
     * @param predicate The predicate.
     * @return A string builder.
     */
    static StringBuilder getSubjectPredicateQueryLineForConcepts(String subject, String predicate) {
        StringBuilder result = new StringBuilder();
        result.append("{<")
                .append(subject)
                .append("> <")
                .append(predicate)
                .append("> ?c }\n");
        return result;
    }

    /**
     * Commit data changes if active.
     */
    private void commitAll(){
        if(isDiskBufferEnabled && this.persistenceService != null) {
            persistenceService.commit(DBPEDIA_SYNONYMY_BUFFER);
            persistenceService.commit(DBPEDIA_HYPERNYMY_BUFFER);
        }
    }

    @Override
    public void close() {
        commitAll();
        if(tdbDataset != null) {
            tdbDataset.end();
            tdbDataset.close();
        }
        LOGGER.info("DBpedia TDB dataset closed.");
    }

    @Override
    public LabelToConceptLinker getLinker() {
        return this.linker;
    }

    public boolean isUseTdb() {
        return isUseTdb;
    }

    public Dataset getTdbDataset() {
        return tdbDataset;
    }

    @Override
    public String getName() {
        return name;
    }

    public Set<String> getExcludedHypernyms() {
        return excludedHypernyms;
    }

    public boolean isDiskBufferEnabled() {
        return isDiskBufferEnabled;
    }

    public void setExcludedHypernyms(Set<String> excludedHypernyms) {
        this.excludedHypernyms = excludedHypernyms;
    }
}
