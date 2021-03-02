package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.sparql.SparqlServices;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService.PreconfiguredPersistences.*;

public class DBpediaKnowledgeSource extends SemanticWordRelationDictionary {


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

    public static String getEndpointUrl() {
        return ENDPOINT_URL;
    }

    /**
     * Default constructor.
     * Disk buffer is enabled by default.
     */
    public DBpediaKnowledgeSource(){
        this(true);
    }

    /**
     *
     * @param isDiskBufferEnabled True if a disk buffer shall be enabled by default
     */
    public DBpediaKnowledgeSource(boolean isDiskBufferEnabled){
        this.isDiskBufferEnabled = isDiskBufferEnabled;
        initializeBuffers();

        if(this.linker == null){
            this.linker = new DBpediaLinker(isDiskBufferEnabled);
        }

        // hypernym exclusion default set
        excludedHypernyms.add("http://www.w3.org/2002/07/owl#Thing");
    }

    /**
     * Initialize buffers (either on-disk or memory).
     */
    private void initializeBuffers(){
        persistenceService = PersistenceService.getService();
        if(isDiskBufferEnabled){
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
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(getEndpointUrl(), queryString);
        ResultSet resultSet = SparqlServices.safeExecution(queryExecution);
        while(resultSet.hasNext()){
            QuerySolution solution = resultSet.next();
            String label = solution.getLiteral("l").getLexicalForm();
            result.add(label);
        }
        queryExecution.close();
        result.remove("");
        synonymyBuffer.put(key, result);
        commit();
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
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(getEndpointUrl(), queryString);
        ResultSet queryResult = SparqlServices.safeExecution(queryExecution);
        while(queryResult.hasNext()){
            QuerySolution solution = queryResult.next();
            String hypernym = solution.getResource("c").getURI();
            result.add(hypernym);
        }
        queryExecution.close();

        // we add to the buffer before excluding hypernyms
        hypernymyBuffer.put(key, result);
        commit();

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
    private void commit(){
        if(isDiskBufferEnabled){
            persistenceService.commit(DBPEDIA_SYNONYMY_BUFFER);
            persistenceService.commit(DBPEDIA_HYPERNYMY_BUFFER);
        }
    }

    @Override
    public void close() {
        // nothing to close
    }

    @Override
    public LabelToConceptLinker getLinker() {
        return this.linker;
    }

    @Override
    public String getName() {
        return name;
    }

    public Set<String> getExcludedHypernyms() {
        return excludedHypernyms;
    }

    public void setExcludedHypernyms(Set<String> excludedHypernyms) {
        this.excludedHypernyms = excludedHypernyms;
    }
}
