package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.classic;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the WebIsAlodLinker.
 * Requires a working internet connection.
 */
class WebIsAlodClassicLinkerTest {

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