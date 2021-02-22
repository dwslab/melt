package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.xl;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.WebIsAlodSPARQLservice;
import it.uniroma1.lcl.jlt.util.Files;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class WebIsAlodXLLinkerTest {


    /**
     * For exploratory testing.
     */
    @Test
    void linkToPotentiallyMultipleConceptsPlayground(){
        String concept = "CDX Emerging Markets";
        System.out.println("Linked Concepts:");
        WebIsAlodXLLinker linker = new WebIsAlodXLLinker();
        HashSet<String> result = linker.linkToPotentiallyMultipleConcepts(concept);
        if(result != null && result.size() > 0) {
            for (String s : result) {
                System.out.println("\t" + s);
            }
        } else {
            System.out.println("No concept found.");
        }
    }

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
        WebIsAlodXLLinker linker = new WebIsAlodXLLinker();
        assertEquals("http://webisa.webdatacommons.org/concept/car", linker.linkToSingleConcept("car"));
        assertEquals("http://webisa.webdatacommons.org/concept/car", linker.linkToSingleConcept("Car"));
        assertEquals("http://webisa.webdatacommons.org/concept/car", linker.linkToSingleConcept("CaR"));
        assertEquals("http://webisa.webdatacommons.org/concept/president", linker.linkToSingleConcept("president"));
        assertEquals("http://webisa.webdatacommons.org/concept/luxury%20goods", linker.linkToSingleConcept("luxury goods"));
        assertEquals("http://webisa.webdatacommons.org/concept/european%20union", linker.linkToSingleConcept("european union"));
        assertEquals("http://webisa.webdatacommons.org/concept/europeanunion", linker.linkToSingleConcept("europeanUnion"));
        assertEquals("http://webisa.webdatacommons.org/concept/european%20union", linker.linkToSingleConcept("european_Union"));
        assertEquals("http://webisa.webdatacommons.org/concept/european%20union", linker.linkToSingleConcept("European_Union"));
        assertEquals("http://webisa.webdatacommons.org/concept/european%20union", linker.linkToSingleConcept("EUropean_UniOn"));
    }

    @Test
    void linkToPotentiallyMultipleConcepts() {
        WebIsAlodXLLinker linker = new WebIsAlodXLLinker();
        HashSet<String> result = linker.linkToPotentiallyMultipleConcepts("luxury goods");
        assertTrue(result.contains("http://webisa.webdatacommons.org/concept/luxury%20goods"));
        assertTrue(result.size() == 1);

        HashSet<String> result2 = linker.linkToPotentiallyMultipleConcepts("luxury goods ölkjölkjölkj");
        assertNull(result2);
    }

}