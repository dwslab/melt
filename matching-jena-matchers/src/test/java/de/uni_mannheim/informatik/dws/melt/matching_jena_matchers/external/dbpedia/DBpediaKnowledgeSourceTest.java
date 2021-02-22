package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.sparql.SparqlServices;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DBpediaKnowledgeSourceTest {


    @Test
    void checkEndpointIsLive(){
        String sparqlQueryString = "ASK {?a ?b ?c}";
        assertTrue(SparqlServices.safeAsk(sparqlQueryString, DBpediaKnowledgeSource.getEndpointUrl()));
    }

    @Test
    void getEndpointUrl() {
        assertNotNull(DBpediaKnowledgeSource.getEndpointUrl());
    }
}