package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.sparql.SparqlServices;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools.TestOperations.deletePersistenceDirectory;
import static org.junit.jupiter.api.Assertions.*;

class DBpediaLinkerTest {


    @BeforeAll
    @AfterAll
    static void setupAndTearDown() {
        deletePersistenceDirectory();
    }

    @Test
    void checkEndpointIsLive(){
        String sparqlQueryString = "ASK {?a ?b ?c}";
        assertTrue(SparqlServices.safeAsk(sparqlQueryString, DBpediaLinker.getEndpointUrl()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void linkToSingleConcept(boolean isDiskBufferEnabled) {
        DBpediaLinker linker = new DBpediaLinker(isDiskBufferEnabled);
        assertEquals(isDiskBufferEnabled, linker.isDiskBufferEnabled());

        // default test
        String sapLink = linker.linkToSingleConcept("SAP");
        assertNotNull(sapLink);
        Set<String> sapUris = linker.getUris(sapLink);
        assertTrue(sapUris.contains("http://dbpedia.org/resource/SAP"));
        assertTrue(sapUris.contains("http://dbpedia.org/resource/Shina_Peller")); // dbo:alias SAP@en

        // error test
        assertNull(linker.linkToSingleConcept("THIS_CONCEPT_DOES NOT_EXIST_404"));

        // space separation
        String schumannLink = linker.linkToSingleConcept("Robert Schuman");
        assertNotNull(schumannLink);
        Set<String> schumanUris = linker.getUris(schumannLink);
        assertTrue(schumanUris.contains("http://dbpedia.org/resource/Robert_Schuman"));

        // camel case
        String schumannLink2 = linker.linkToSingleConcept("RobertSchuman");
        assertNotNull(schumannLink2);
        Set<String> schumanUris2 = linker.getUris(schumannLink2);
        assertTrue(schumanUris2.contains("http://dbpedia.org/resource/Robert_Schuman"));

        // concepts with domain specification in brackets
        String swapLink = linker.linkToSingleConcept("swaps");
        assertNotNull(swapLink);
        Set<String> swapUris = linker.getUris(swapLink);
        assertTrue(swapUris.contains("http://dbpedia.org/resource/Swap_(finance)"));
    }

    @Test
    void getDisambiguationUris(){
        DBpediaLinker linker = new DBpediaLinker(false);
        Set<String> uris = linker.getDisambiguationUris("http://dbpedia.org/resource/Swap");
        assertNotNull(uris);
        assertTrue(uris.contains("http://dbpedia.org/resource/Swap_(finance)"));
        assertNotNull(linker.getDisambiguationUris(null));
    }

    @Test
    void getNameOfLinker(){
        DBpediaLinker linker = new DBpediaLinker(false);
        assertNotNull(linker.getNameOfLinker());
    }

    @Test
    void getLinkerQueryString(){
        Set<String> concepts = new HashSet<>();
        concepts.add("SAP");
        String result = DBpediaLinker.getLinkerQueryString(concepts, Language.ENGLISH);
        assertNotNull(result);
        assertTrue(result.contains("SAP"));
        System.out.println(result + "\n\n");

        // test with 2 concepts
        concepts.add("BASF");
        result = DBpediaLinker.getLinkerQueryString(concepts, Language.ENGLISH);
        assertNotNull(result);
        assertTrue(result.contains("SAP"));
        assertTrue(result.contains("BASF"));
        System.out.println(result);
    }

    @Test
    void getEndpointUrl() {
        assertNotNull(DBpediaLinker.getEndpointUrl());
    }
}