package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.webIsAlod.xl;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class WebIsAlodXLLinkerTest {

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