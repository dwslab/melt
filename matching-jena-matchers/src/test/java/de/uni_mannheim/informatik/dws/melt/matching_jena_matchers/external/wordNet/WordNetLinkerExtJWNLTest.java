package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class WordNetLinkerExtJWNLTest {

    @Test
    void linkToSingleConcept() {
        WordNetLinkerExtJWNL linker = new WordNetLinkerExtJWNL(new WordNetKnowledgeSourceExtJWNL());
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietal cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietal_cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietal_Cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietalCortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("Parietal_Cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("ParietalCortex"));
        assertEquals("european union", linker.linkToSingleConcept("EuropeanUnion"));
        assertEquals("european union", linker.linkToSingleConcept("EuroPEan UNION"));
        assertEquals("dog", linker.linkToSingleConcept("dog"));
        assertEquals("hound", linker.linkToSingleConcept("hound"));
        assertEquals("hiv", linker.linkToSingleConcept("hiv"));
        assertEquals("hiv", linker.linkToSingleConcept("HIV"));
        assertNull(linker.linkToSingleConcept("hair_bulb"));
        assertNull(linker.linkToSingleConcept("hairBulb"));
        assertNull(linker.linkToSingleConcept("adfasdfaölkj"));

        // do everything again to test whether buffers work correctly
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietal cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietal_cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietal_Cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("parietalCortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("Parietal_Cortex"));
        assertEquals("parietal cortex", linker.linkToSingleConcept("ParietalCortex"));
        assertEquals("hiv", linker.linkToSingleConcept("hiv"));
        assertEquals("hiv", linker.linkToSingleConcept("HIV"));
        assertNull(linker.linkToSingleConcept("hair_bulb"));
        assertNull(linker.linkToSingleConcept("hairBulb"));
        assertNull(linker.linkToSingleConcept("adfasdfaölkj"));

        // null test
        assertNull(linker.linkToSingleConcept(""));
        assertNull(linker.linkToSingleConcept(null));
    }

    @Test
    void normalizeForWordnetLookup(){
        WordNetLinkerExtJWNL linker = new WordNetLinkerExtJWNL(new WordNetKnowledgeSourceExtJWNL());
        assertEquals("parietal cortex", linker.normalizeForWordnetLookupWithTokenization("parietal cortex"));
        assertEquals("parietal cortex", linker.normalizeForWordnetLookupWithTokenization("parietal_cortex"));
        assertEquals("parietal cortex", linker.normalizeForWordnetLookupWithTokenization("parietal_Cortex"));
        assertEquals("parietal cortex", linker.normalizeForWordnetLookupWithTokenization("parietalCortex"));
    }

    @Test
    void linkToPotentiallyMultipleConcepts(){
        WordNetLinkerExtJWNL linker = new WordNetLinkerExtJWNL(new WordNetKnowledgeSourceExtJWNL());
        HashSet<String> result1 = linker.linkToPotentiallyMultipleConcepts("hair medulla");
        assertTrue(result1.size() == 2);
        assertTrue(result1.contains("hair"));
        assertTrue(result1.contains("medulla"));

        HashSet<String> result2 = linker.linkToPotentiallyMultipleConcepts("hair bulb");
        assertTrue(result2.size() == 2);
        assertTrue(result2.contains("hair"));
        assertTrue(result2.contains("bulb"));

        HashSet<String> result3 = linker.linkToPotentiallyMultipleConcepts("Hair Bulb");
        assertTrue(result2.size() == 2);
        assertTrue(result3.contains("hair"));
        assertTrue(result3.contains("bulb"));

        HashSet<String> result4 = linker.linkToPotentiallyMultipleConcepts("Hair_Bulb");
        assertTrue(result2.size() == 2);
        assertTrue(result4.contains("hair"));
        assertTrue(result4.contains("bulb"));

        // null match
        assertNull(linker.linkToPotentiallyMultipleConcepts("Hair_Bulb_ACDFDSDF"));
        assertNull(linker.linkToPotentiallyMultipleConcepts(null));
        assertNull(linker.linkToPotentiallyMultipleConcepts(""));
    }

}