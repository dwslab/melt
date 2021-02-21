package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.classic;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.WebIsAlodSPARQLservice;
import it.uniroma1.lcl.jlt.util.Files;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the WebIsAlodLinker.
 * Requires a working internet connection.
 */
class WebIsAlodClassicLinkerTest {


    @AfterAll
    @BeforeAll
    static void deleteBuffers(){
        WebIsAlodSPARQLservice.closeAllServices();
        PersistenceService.getService().closePersistenceService();
        File buffer = new File(PersistenceService.PERSISTENCE_DIRECTORY);
        if(buffer.exists() && buffer.isDirectory()) {
            Files.deleteDirectory(buffer);
        }
    }

    @Test
    void linkToSingleConcept() {
        WebIsAlodClassicLinker linker = new WebIsAlodClassicLinker();
        assertEquals("http://webisa.webdatacommons.org/concept/_car_", linker.linkToSingleConcept("car"));
        assertEquals("http://webisa.webdatacommons.org/concept/_car_", linker.linkToSingleConcept("Car"));
        assertEquals("http://webisa.webdatacommons.org/concept/_car_", linker.linkToSingleConcept("CaR"));
        assertEquals("http://webisa.webdatacommons.org/concept/_president_", linker.linkToSingleConcept("president"));
        assertEquals("http://webisa.webdatacommons.org/concept/luxury_goods_", linker.linkToSingleConcept("luxury goods"));
        assertEquals("http://webisa.webdatacommons.org/concept/european_union_", linker.linkToSingleConcept("european union"));
        assertEquals("http://webisa.webdatacommons.org/concept/european_union_", linker.linkToSingleConcept("europeanUnion"));
        assertEquals("http://webisa.webdatacommons.org/concept/european_union_", linker.linkToSingleConcept("european_Union"));
        assertEquals("http://webisa.webdatacommons.org/concept/european_union_", linker.linkToSingleConcept("European_Union"));
        assertEquals("http://webisa.webdatacommons.org/concept/european_union_", linker.linkToSingleConcept("EUropean_UniOn"));
        assertNull(linker.linkToSingleConcept(""));
        assertNull(linker.linkToSingleConcept(null));
    }

    @Test
    void linkToPotentiallyMultipleConcepts() {
        WebIsAlodClassicLinker linker = new WebIsAlodClassicLinker();
        HashSet<String> result = linker.linkToPotentiallyMultipleConcepts("luxury goods");
        assertTrue(result.contains("http://webisa.webdatacommons.org/concept/luxury_goods_"));
        assertTrue(result.size() == 1);

        assertNull(linker.linkToPotentiallyMultipleConcepts("luxury goods ölkjölkjölkj"));
        assertNull(linker.linkToPotentiallyMultipleConcepts(null));
        assertNull(linker.linkToPotentiallyMultipleConcepts(""));
    }

}