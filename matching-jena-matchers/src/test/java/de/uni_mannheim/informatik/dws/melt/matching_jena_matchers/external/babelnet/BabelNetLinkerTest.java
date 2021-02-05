package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.babelnet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BabelNetLinkerTest {


    private static BabelNetLinker linker;

    @BeforeAll
    static void setUp(){
        linker = new BabelNetLinker(new BabelNetKnowledgeSource());
    }

    @Test
    void linkToSingleConcept() {
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietal cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietal_cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietal_Cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietalCortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("ParietalCortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("Parietal_Cortex"));
        assertEquals("house of parliament", linker.linkToSingleConcept("HouseOfParliament"));
        assertEquals("house of parliament", linker.linkToSingleConcept("houseOfParliament"));
        assertEquals("hiv", linker.linkToSingleConcept("hiv"));
        assertEquals("hiv", linker.linkToSingleConcept("HIV"));

        // no do everything again to test buffers
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietal cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietal_cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietal_Cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietalCortex"));
        assertEquals("house of parliament", linker.linkToSingleConcept("HouseOfParliament"));
        assertEquals("house of parliament", linker.linkToSingleConcept("houseOfParliament"));
        assertEquals("hiv", linker.linkToSingleConcept("hiv"));
        assertEquals("hiv", linker.linkToSingleConcept("HIV"));
    }

    @Test
    void linkToPotentiallyMultipleConcepts() {
        Set<String> result = linker.linkToPotentiallyMultipleConcepts("european union usa");
        assertTrue(result.size() == 2);
        assertTrue(result.contains("european union"));
        assertTrue(result.contains("usa"));

        result = linker.linkToPotentiallyMultipleConcepts("EuroPean uNion USA");
        assertTrue(result.size() == 2);
        assertTrue(result.contains("european union"));
        assertTrue(result.contains("usa"));

        result = linker.linkToPotentiallyMultipleConcepts("EuroPean_uNion_USA");
        assertTrue(result.size() == 2);
        assertTrue(result.contains("european union"));
        assertTrue(result.contains("usa"));

        // now do everything again to test buffers
        result = linker.linkToPotentiallyMultipleConcepts("european union usa");
        assertTrue(result.size() == 2);
        assertTrue(result.contains("european union"));
        assertTrue(result.contains("usa"));

        result = linker.linkToPotentiallyMultipleConcepts("EuroPean uNion USA");
        assertTrue(result.size() == 2);
        assertTrue(result.contains("european union"));
        assertTrue(result.contains("usa"));

        result = linker.linkToPotentiallyMultipleConcepts("EuroPean_uNion_USA");
        assertTrue(result.size() == 2);
        assertTrue(result.contains("european union"));
        assertTrue(result.contains("usa"));

        result = linker.linkToPotentiallyMultipleConcepts("EuroPean_uNion_USA_ASDFASDFSADF");
        assertNull(result);

        result = linker.linkToPotentiallyMultipleConcepts("european union asdfddd");
        assertNull(result);

        result = linker.linkToPotentiallyMultipleConcepts("european asdfddd union");
        assertNull(result);

        result = linker.linkToPotentiallyMultipleConcepts("asdfddd european union");
        assertNull(result);
    }

    @Test
    void normalizeForBabelnetLookup(){
        BabelNetLinker linker = new BabelNetLinker(new BabelNetKnowledgeSource());
        assertEquals("parietal cortex", linker.normalizeForBabelnetLookupWithTokenization("parietal cortex"));
        assertEquals("parietal cortex", linker.normalizeForBabelnetLookupWithTokenization("parietal_cortex"));
        assertEquals("parietal cortex", linker.normalizeForBabelnetLookupWithTokenization("parietal_Cortex"));
        assertEquals("parietal cortex", linker.normalizeForBabelnetLookupWithTokenization("parietalCortex"));
    }
}
