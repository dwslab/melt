package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.dataStructures.StringString;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import org.apache.jena.query.*;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMapUnsafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService.PreconfiguredPersistences.*;
import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.sparql.SparqlServices.safeAsk;
import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.sparql.SparqlServices.safeExecution;


/**
 * This class performs SPARQL queries for the WebIsALOD data set.
 */
public class WebIsAlodSPARQLservice {

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebIsAlodSPARQLservice.class);

    /**
     * Synonymy Buffer
     */
    private ConcurrentMap<StringString, Boolean> synonymyAskBuffer;

    /**
     * Hypernymy Buffer (for ask queries)
     */
    private ConcurrentMap<StringString, Boolean> hypernymyAskBuffer;

    /**
     * Hypernym Buffer (for isa queries)
     */
    private ConcurrentMap<String, Set<String>> hypernymBuffer;

    /**
     * Label2URI buffer (linking buffer)
     */
    private ConcurrentMap<String, String> labelUriBuffer;


    /**
     * If the disk-buffer is disabled, no buffers are read/written from/to the disk.
     * Default: true.
     */
    private boolean isDiskBufferEnabled = true;

    /**
     * Service responsible for disk buffers.
     */
    private PersistenceService persistenceService;

    /**
     * Instance Endpoint.
     */
    private WebIsAlodEndpoint webIsAlodEndpoint;

    /**
     * Singleton instances per endpoint.
     */
    private static HashMap<WebIsAlodEndpoint, WebIsAlodSPARQLservice> instances;

    private static final String CLASSIC_CONFIDENCE = "<http://webisa.webdatacommons.org/ontology#>";

    private static final String XL_CONFIDENCE = "<http://webisa.webdatacommons.org/ontology/>";

    /**
     * Singleton Pattern to get Sparql service instance.
     *
     * @param sparqlWebIsAlodEndpoint Desired webIsAlodEndpoint.
     * @return Instance.
     */
    public static WebIsAlodSPARQLservice getInstance(WebIsAlodEndpoint sparqlWebIsAlodEndpoint) {
        return getInstance(sparqlWebIsAlodEndpoint, true);
    }

    /**
     * Singleton Pattern to get Sparql service instance.
     *
     * @param sparqlWebIsAlodEndpoint Desired webIsAlodEndpoint.
     * @param isDiskBufferEnabled     Indicator whether the disk buffer shall be used.
     * @return Instance.
     */
    public static WebIsAlodSPARQLservice getInstance(WebIsAlodEndpoint sparqlWebIsAlodEndpoint, boolean isDiskBufferEnabled) {
        WebIsAlodSPARQLservice webIsAlodSPARQLservice;
        if (instances == null) {
            instances = new HashMap<>();
            webIsAlodSPARQLservice = new WebIsAlodSPARQLservice(sparqlWebIsAlodEndpoint, isDiskBufferEnabled);
            instances.put(sparqlWebIsAlodEndpoint, webIsAlodSPARQLservice);
        } else {
            webIsAlodSPARQLservice = instances.get(sparqlWebIsAlodEndpoint);
            if (webIsAlodSPARQLservice != null) {
                webIsAlodSPARQLservice.setDiskBufferEnabled(isDiskBufferEnabled);
                return webIsAlodSPARQLservice;
            } else {
                webIsAlodSPARQLservice = new WebIsAlodSPARQLservice(sparqlWebIsAlodEndpoint, isDiskBufferEnabled);
                instances.put(sparqlWebIsAlodEndpoint, webIsAlodSPARQLservice);
            }
        }
        return webIsAlodSPARQLservice;
    }

    /**
     * Private Constructor (singleton pattern).
     *
     * @param sparqlEnpoint       The SPARQL endpoint URI.
     * @param isDiskBufferEnabled True if disk buffer is enabled.
     */
    private WebIsAlodSPARQLservice(WebIsAlodEndpoint sparqlEnpoint, boolean isDiskBufferEnabled) {
        this.webIsAlodEndpoint = sparqlEnpoint;
        this.isDiskBufferEnabled = isDiskBufferEnabled;
        initializeBuffers();
    }

    /**
     * Initializes local database.
     */
    private void initializeBuffers() {
        persistenceService = PersistenceService.getService();
        if (isDiskBufferEnabled) {
            if (this.webIsAlodEndpoint.isClassic()) {
                this.labelUriBuffer = persistenceService.getMapDatabase(ALOD_CLASSIC_LABEL_URI_BUFFER);
                this.synonymyAskBuffer = persistenceService.getMapDatabase(ALOD_CLASSIC_SYONYMY_BUFFER);
                this.hypernymyAskBuffer = persistenceService.getMapDatabase(ALOD_CLASSIC_HYPERNYMY_ASK_BUFFER);
                this.hypernymBuffer = persistenceService.getMapDatabase(ALOD_CLASSIC_HYPERNYM_BUFFER);
            } else {
                this.labelUriBuffer = persistenceService.getMapDatabase(ALOD_XL_LABEL_URI_BUFFER);
                this.synonymyAskBuffer = persistenceService.getMapDatabase(ALOD_XL_SYONYMY_BUFFER);
                this.hypernymyAskBuffer = persistenceService.getMapDatabase(ALOD_XL_HYPERNYMY_ASK_BUFFER);
                this.hypernymBuffer = persistenceService.getMapDatabase(ALOD_XL_HYPERNYM_BUFFER);

            }
        } else {
            this.labelUriBuffer = new ConcurrentHashMap<>();
            this.synonymyAskBuffer = new ConcurrentHashMap<>();
            this.hypernymBuffer = new ConcurrentHashMap<>();
            this.hypernymyAskBuffer = new ConcurrentHashMapUnsafe<>();
        }
    }

    /**
     * Checks whether the concept behind the given URI is available on WebIsALOD.
     * Availability is defined as "has at least one broader concept".
     *
     * @param uri The URI that shall be looked up.
     * @return True if found, else false.
     */
    public boolean isConceptOnDataSet(String uri) {
        return hasBroaderConcepts(uri);
    }

    /**
     * Determines whether a concept has a hypernym.
     *
     * @param uri the URI for which hypernymy has to be checked.
     * @return True if a hypernym exists, else false.
     */
    public boolean hasBroaderConcepts(String uri) {
        uri = StringOperations.convertToTag(uri);
        String queryString =
                "ASK {\n" +
                        uri + " <http://www.w3.org/2004/02/skos/core#skos:broader> ?hypernym .\n" +
                        "}";
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.sparqlService(this.webIsAlodEndpoint.toString(), query);
        boolean result = safeAsk(qe);
        qe.close();
        return result;
    }

    /**
     * Checks whether the two URIs are synonymous. If one or both URIs cannot be found in the data set, the default
     * answer is false.
     *
     * @param uri1 URI as String.
     * @param uri2 URI as String.
     * @return True if synonymous according to ALOD, else false.
     */
    public boolean isSynonymous(String uri1, String uri2) {
        if (uri1 == null || uri2 == null) {
            return false;
        }
        return isSynonymous(webIsAlodEndpoint, uri1, uri2);
    }

    /**
     * Checks whether the two URIs are synonymous. If one or both URIs cannot be found in the data set, the default
     * answer is false.
     *
     * @param uri1              URI as String.
     * @param uri2              URI as String.
     * @param minimumConfidence The required minimum confidence.
     * @return True if synonymous according to ALOD, else false.
     */
    public boolean isSynonymous(String uri1, String uri2, double minimumConfidence) {
        if (uri1 == null || uri2 == null) {
            return false;
        }
        return isSynonymous(webIsAlodEndpoint, uri1, uri2, minimumConfidence);
    }

    public boolean isHypernymous(String uri1, String uri2, double minimumConfidence) {
        if (uri1 == null || uri2 == null) {
            return false;
        }
        return isHypernymous(webIsAlodEndpoint, uri1, uri2, minimumConfidence);
    }

    /**
     * Assumed minimum confidence: 0.0.
     *
     * @param uri1 URI 1
     * @param uri2 URI 2
     * @return True if hypernymy relation exists.
     */
    public boolean isHypernymous(String uri1, String uri2) {
        if (uri1 == null || uri2 == null) {
            return false;
        }
        return isHypernymous(webIsAlodEndpoint, uri1, uri2, 0.0);
    }

    /**
     * Internal method that can execute a synonymy ask query with a given minimum confidence threshold.
     *
     * @param sparqlWebIsAlodEndpoint SPARQL endpoint.
     * @param uri1                    URI as String.
     * @param uri2                    URI as String.
     * @param minimumConfidence       Minimum confidence
     * @return True if synonymous according to ALOD, else false.
     */
    private boolean isSynonymous(WebIsAlodEndpoint sparqlWebIsAlodEndpoint, String uri1, String uri2, double minimumConfidence) {
        // buffer lookup
        StringString uriTuple = new StringString(uri1 + minimumConfidence, uri2 + minimumConfidence);
        if (synonymyAskBuffer.get(uriTuple) != null) {
            return synonymyAskBuffer.get(uriTuple);
        }
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlWebIsAlodEndpoint.toString(), getIsSynonymousAskQueryClassic(uri1, uri2, minimumConfidence, this.webIsAlodEndpoint.isClassic()));
        boolean result = safeAsk(qe);
        synonymyAskBuffer.put(uriTuple, result);
        qe.close();
        if (sparqlWebIsAlodEndpoint.equals(WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT)) {
            commit(ALOD_CLASSIC_SYONYMY_BUFFER);
        } else {
            commit(ALOD_XL_SYONYMY_BUFFER);
        }
        return result;
    }

    /**
     * Obtain isa concepts for the given uri.
     *
     * @param uri        The URI for which hypernyms shall be found.
     * @param confidence Minimum confidence.
     * @return A set of hypernyms.
     */
    public Set<String> getHypernyms(String uri, double confidence) {
        if (uri == null || uri.equals("")) {
            return null;
        }
        if (confidence < 0) {
            confidence = 0.0;
        }
        uri = StringOperations.removeTag(uri);
        String key = uri + "_" + confidence;
        if (hypernymBuffer.get(key) != null) {
            return hypernymBuffer.get(key);
        }
        boolean isClassic = this.webIsAlodEndpoint.equals(WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT);

        String queryString;
        if (confidence != 0.0) {
            String confidencePrefix = "";
            if (isClassic) confidencePrefix = CLASSIC_CONFIDENCE;
            else confidencePrefix = XL_CONFIDENCE;
            queryString =
                    "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                            "PREFIX isaont: " + confidencePrefix + "\n" +
                            //"select distinct ?hypernym ?minConfidence where\n" + // dropped b/c we do not need minConfidence here
                            "select distinct ?hypernym where\n" +
                            "{\n" +
                            "GRAPH ?g {\n" +
                            "<" + uri + ">  skos:broader ?hypernym .\n" +
                            "}\n" +
                            "?g isaont:hasConfidence ?minConfidence .\n" +
                            "FILTER(?minConfidence > " + confidence + ")\n" +
                            "}";
        } else {
            queryString = "SELECT DISTINCT ?hypernym WHERE\n" +
                    "{ <" + uri + "> <http://www.w3.org/2004/02/skos/core#broader> ?hypernym .}";

        }
        QueryExecution qe = QueryExecutionFactory.sparqlService(this.webIsAlodEndpoint.toString(), queryString);
        Set<String> result = new HashSet<>();
        ResultSet queryResult = safeExecution(qe);
        while (queryResult.hasNext()) {
            QuerySolution solution = queryResult.next();
            result.add(solution.get("?hypernym").toString());
        }
        qe.close();
        hypernymBuffer.put(key, result);
        if (this.webIsAlodEndpoint.equals(WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT)) {
            commit(ALOD_CLASSIC_HYPERNYM_BUFFER);
        } else {
            commit(ALOD_XL_HYPERNYM_BUFFER);
        }
        return result;
    }

    /**
     * Internal method that can execute a synonymy ask query without minimum confidence threshold.
     *
     * @param sparqlWebIsAlodEndpoint SPARQL endpoint.
     * @param uri1                    URI as String.
     * @param uri2                    URI as String.
     * @return True if synonymous according to ALOD, else false.
     */
    private boolean isSynonymous(WebIsAlodEndpoint sparqlWebIsAlodEndpoint, String uri1, String uri2) {
        StringString uriTuple = new StringString(uri1, uri2);
        if (synonymyAskBuffer.get(uriTuple) != null) {
            return synonymyAskBuffer.get(uriTuple);
        }
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlWebIsAlodEndpoint.toString(), getIsSynonymousAskQueryClassic(uri1, uri2));
        boolean result = safeAsk(qe);
        synonymyAskBuffer.put(uriTuple, result);
        qe.close();
        if (sparqlWebIsAlodEndpoint.equals(WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT)) {
            commit(ALOD_CLASSIC_SYONYMY_BUFFER);
        } else {
            commit(ALOD_XL_SYONYMY_BUFFER);
        }
        return result;
    }

    /**
     * Returns an ASK query that can be used to derive synonymy from the WebIsALOD data set.
     *
     * @param uri1 URI 1 as String.
     * @param uri2 URI 2 as String.
     * @return The query in String representation.
     */
    private static String getIsSynonymousAskQueryClassic(String uri1, String uri2) {
        uri1 = StringOperations.convertToTag(uri1);
        uri2 = StringOperations.convertToTag(uri2);
        return "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "ASK " +
                "{ " +
                uri1 + " skos:broader " + uri2 + ". " +
                uri2 + " skos:broader " + uri1 + ". " +
                " }";
    }

    /**
     * Internal method that can execute a hypernymy ask query with a given minimum confidence threshold.
     *
     * @param sparqlWebIsAlodEndpoint SPARQL endpoint.
     * @param uri1                    URI as String.
     * @param uri2                    URI as String.
     * @param minimumConfidence       Minimum confidence
     * @return True if hypernymous according to ALOD, else false.
     */
    private boolean isHypernymous(WebIsAlodEndpoint sparqlWebIsAlodEndpoint, String uri1, String uri2, double minimumConfidence) {
        // buffer lookup
        StringString uriTuple = new StringString(uri1 + minimumConfidence, uri2 + minimumConfidence);
        if (hypernymyAskBuffer.get(uriTuple) != null) {
            return hypernymyAskBuffer.get(uriTuple);
        }
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlWebIsAlodEndpoint.toString(), getIsHypernymousAskQueryClassic(uri1, uri2, minimumConfidence, this.webIsAlodEndpoint.isClassic()));
        boolean result = safeAsk(qe);
        hypernymyAskBuffer.put(uriTuple, result);
        if (sparqlWebIsAlodEndpoint.equals(WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT)) {
            commit(ALOD_CLASSIC_HYPERNYMY_ASK_BUFFER);
        } else {
            commit(ALOD_XL_HYPERNYMY_ASK_BUFFER);
        }
        qe.close();
        return result;
    }

    /**
     * Obtain a query to check for synonymy.
     *
     * @param uri1              URI of concept 1.
     * @param uri2              URI of concept 2.
     * @param minimumConfidence The required minimum confidence. Must be in the range [0, 1].
     * @param isClassic         Indicator whether the query is for the classic data set. False is interpreted as XL endpoint.
     * @return Query in String representation.
     */
    private static String getIsSynonymousAskQueryClassic(String uri1, String uri2, double minimumConfidence, boolean isClassic) {
        uri1 = StringOperations.convertToTag(uri1);
        uri2 = StringOperations.convertToTag(uri2);

        String confidencePrefix = "";
        if (isClassic) confidencePrefix = CLASSIC_CONFIDENCE;
        else confidencePrefix = XL_CONFIDENCE;

        String query =
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                        "PREFIX isaont: " + confidencePrefix + "\n" +
                        "ASK\n" +
                        "{\n" +
                        "GRAPH ?g1 {\n" +
                        uri1 + " skos:broader " + uri2 + ".\n" +
                        "}\n" +
                        "?g1 isaont:hasConfidence ?minConfidence1 .\n" +
                        "GRAPH ?g2 {\n" +
                        uri2 + " skos:broader " + uri1 + ".\n" +
                        "}\n" +
                        "?g2 isaont:hasConfidence ?minConfidence2 .\n" +
                        "FILTER( ?minConfidence1> " + minimumConfidence + " && ?minConfidence2 > " + minimumConfidence + ")\n" + // && required, "AND" won't be compiled by jena
                        "}";
        return query;
    }

    /**
     * Obtain a query to check for hypernymy.
     *
     * @param uri1              URI of concept 1.
     * @param uri2              URI of concept 2.
     * @param minimumConfidence The minimum confidence that shall apply.
     * @param isClassic         True if it is classic, else false.
     * @return Query in String format. False is interpreted as XL endpoint.
     */
    private static String getIsHypernymousAskQueryClassic(String uri1, String uri2, double minimumConfidence, boolean isClassic) {
        uri1 = StringOperations.convertToTag(uri1);
        uri2 = StringOperations.convertToTag(uri2);
        String confidencePrefix = "";
        if (isClassic) confidencePrefix = CLASSIC_CONFIDENCE;
        else confidencePrefix = XL_CONFIDENCE;
        return "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX isaont: " + confidencePrefix + "\n" +
                "ASK\n" +
                "{\n" +
                "GRAPH ?g {\n" +
                uri1 + " skos:broader " + uri2 + " .\n" +
                "}\n" +
                "?g isaont:hasConfidence ?minConfidence .\n" +
                "FILTER(?minConfidence > " + minimumConfidence + ")\n" +
                "}";
    }

    /**
     * Checks whether the URI in question is a concept of the ALOD XL webIsAlodEndpoint.
     *
     * @param uri The URI which shall be checked.
     * @return true if URI is XL webIsAlodEndpoint concept.
     */
    public static boolean isALODxlEndpointConcept(String uri) {
        try {
            uri = StringOperations.convertToTag(uri);
            if (uri.endsWith("_>")) {
                return false;
            } else {
                return true;
            }
        } catch (NullPointerException npe) {
            System.out.println(uri);
        }
        return false;
    }

    /**
     * Returns the URI to a given label under the premises that the label can be linked to a resource.
     * lse null will be returned.
     *
     * @param label The label that shall be used for the lookup.
     * @return null if not found, else URI as String.
     */
    public String getUriUsingLabel(String label) {
        return getUriUsingLabel(this.webIsAlodEndpoint, label);
    }

    /**
     * Returns the URI to a given label under the premises that the label can be linked to a resource.
     * Else null will be returned.
     *
     * @param webIsAlodEndpoint The SPARQL webIsAlodEndpoint.
     * @param label             The label that shall be used for the lookup.
     * @return null if not found, else URI as String.
     */
    public String getUriUsingLabel(WebIsAlodEndpoint webIsAlodEndpoint, String label) {
        String key = label;
        boolean isClassic = webIsAlodEndpoint.equals(WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT);

        // buffer check
        if (labelUriBuffer.containsKey(key)) {
            String retrieved = labelUriBuffer.get(key);
            if (retrieved.equals("null")) {
                return null;
            } else return retrieved;
        }

        String queryString =
                "select distinct ?c where {\n" +
                        "?c <http://www.w3.org/2000/01/rdf-schema#label> \"" + label + "\"\n" +
                        "}";
        //LOGGER.info("Running the following query:\n" + queryString);
        QueryExecution qe = QueryExecutionFactory.sparqlService(webIsAlodEndpoint.toString(), queryString);
        ResultSet results = safeExecution(qe);
        //LOGGER.info("Completed.");

        if (!results.hasNext()) {
            // Query was not successful.
            labelUriBuffer.put(key, "null");
            if (isClassic) {
                commit(ALOD_CLASSIC_LABEL_URI_BUFFER);
            } else {
                commit(ALOD_XL_LABEL_URI_BUFFER);
            }
            qe.close();
            return null;
        }

        String resource;
        while (results.hasNext()) {
            QuerySolution solution = results.next();
            if (solution.getResource("c") != null && !solution.getResource("c").equals("")) {
                resource = solution.getResource("c").toString();
                labelUriBuffer.put(key, resource);
                qe.close();
                if (isClassic) {
                    commit(ALOD_CLASSIC_LABEL_URI_BUFFER);
                } else {
                    commit(ALOD_XL_LABEL_URI_BUFFER);
                }
                return resource;
            }
        }
        // Nothing could be found.
        labelUriBuffer.put(key, "null");
        if (isClassic) {
            commit(ALOD_CLASSIC_LABEL_URI_BUFFER);
        } else {
            commit(ALOD_XL_LABEL_URI_BUFFER);
        }
        qe.close();
        return null;
    }

    /**
     * Checks whether there is a resource with the given URI available at the given endpoint.
     *
     * @param uri The URI to be looked for.
     * @return True if URI exists, else false.
     */
    public boolean isURIinDictionary(String uri) {
        return safeAsk("ASK {" + StringOperations.convertToTag(uri) + " ?p ?o}", this.webIsAlodEndpoint.toString());
    }

    public void commit(PersistenceService.PreconfiguredPersistences persistences) {
        if (!isDiskBufferEnabled) return;
        switch (persistences) {
            case ALOD_XL_HYPERNYMY_ASK_BUFFER:
            case ALOD_XL_SYONYMY_BUFFER:
            case ALOD_XL_LABEL_URI_BUFFER:
            case ALOD_XL_HYPERNYM_BUFFER:
            case ALOD_CLASSIC_SYONYMY_BUFFER:
            case ALOD_CLASSIC_HYPERNYMY_ASK_BUFFER:
            case ALOD_CLASSIC_HYPERNYM_BUFFER:
            case ALOD_CLASSIC_LABEL_URI_BUFFER:
                persistenceService.commit(persistences);
        }
    }

    /**
     * Clean-up and close db. This method will also shut down the WebIsAlodSPARQL service.
     */
    public void close() {
        boolean isClassic = this.webIsAlodEndpoint.equals(WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT);
        if (persistenceService != null) {
            if (isClassic) {
                persistenceService.closeDatabase(ALOD_CLASSIC_SYONYMY_BUFFER);
                persistenceService.closeDatabase(ALOD_CLASSIC_HYPERNYMY_ASK_BUFFER);
                persistenceService.closeDatabase(ALOD_CLASSIC_HYPERNYM_BUFFER);
                persistenceService.closeDatabase(ALOD_CLASSIC_LABEL_URI_BUFFER);
            } else if(! (instances.containsKey(WebIsAlodEndpoint.ALOD_XL_NO_PROXY) && instances.containsKey(WebIsAlodEndpoint.ALOD_XL_ENDPOINT)) ) {
                // only close XL if there are no remaining XL endpoints (multiple exist so we cannot close the XL buffer if there is another open XL SPARQL service)
                persistenceService.closeDatabase(ALOD_XL_SYONYMY_BUFFER);
                persistenceService.closeDatabase(ALOD_XL_HYPERNYMY_ASK_BUFFER);
                persistenceService.closeDatabase(ALOD_XL_HYPERNYM_BUFFER);
                persistenceService.closeDatabase(ALOD_XL_LABEL_URI_BUFFER);
            }
        }
        instances.remove(this.webIsAlodEndpoint);
    }

    public static void closeAllServices() {
        if (instances == null) return;
        HashSet<WebIsAlodSPARQLservice> mySet = new HashSet<>(instances.values());
        for (WebIsAlodSPARQLservice service : mySet) {
            service.close();
        }
    }


    //---------------------------------------------------------------------------------
    // Data Structures
    //---------------------------------------------------------------------------------

    /**
     * Enumeration of the two available endpoints.
     */
    public enum WebIsAlodEndpoint {
        ALOD_CLASSIC_ENDPOINT, ALOD_XL_ENDPOINT, ALOD_XL_NO_PROXY;

        @Override
        public String toString() {
            switch (this) {
                case ALOD_XL_ENDPOINT:
                    return "http://webisxl.webdatacommons.org/sparql";
                case ALOD_XL_NO_PROXY:
                    return "http://134.155.108.124:8890/sparql";
                case ALOD_CLASSIC_ENDPOINT:
                    return "http://webisa.webdatacommons.org/sparql";
                default:
                    return "";
            }
        }

        /**
         * Indicator whether the endpoint is ALOD classic or XL.
         *
         * @return true if classic endpoint, false if xl endpoint
         */
        public boolean isClassic() {
            if (this == ALOD_CLASSIC_ENDPOINT) {
                return true;
            } else return false;
        }
    }

    //---------------------------------------------------------------------------------
    // Data Structures
    //---------------------------------------------------------------------------------

    public boolean isDiskBufferEnabled() {
        return isDiskBufferEnabled;
    }

    /**
     * Note that when you disable your buffer during runtime, the buffer will be reinitialized.
     *
     * @param diskBufferEnabled True for enablement, else false.
     */
    public void setDiskBufferEnabled(boolean diskBufferEnabled) {

        // do nothing if already enabled and set enabled
        if (diskBufferEnabled && this.isDiskBufferEnabled) return;

        // do nothing if already enabled and set enabled
        if (!diskBufferEnabled && !this.isDiskBufferEnabled) return;

        // re-initialize buffers
        this.isDiskBufferEnabled = diskBufferEnabled;
        initializeBuffers();

        //if(!diskBufferEnabled && persistenceService != null) {
        //    persistenceService.closePersistenceService();
        //}
    }
}
