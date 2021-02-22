package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia;

public class DBpediaKnowledgeSource {


    /**
     * The public SPARQL endpoint.
     */
    private static final String ENDPOINT_URL = "https://dbpedia.org/sparql";

    public static String getEndpointUrl() {
        return ENDPOINT_URL;
    }
}
