package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.wiktionary;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.testTools.TestOperations;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class WiktionaryLinkerTest {

    public static WiktionaryLinker linker;
    public static WiktionaryKnowledgeSource wiktionary;


    @BeforeAll
    public static void prepare() {
        String key = "wiktionaryTdbDirectory";
        String tdbpath = TestOperations.getStringKeyFromResourceBundle("local_config", key);
        if(tdbpath == null){
            tdbpath = TestOperations.getStringKeyFromResourceBundle("config", key);
        }
        if(tdbpath == null){
            fail("Cannot find config.properties or local_config.properties with key " + key);
        }
        wiktionary = new WiktionaryKnowledgeSource(tdbpath);
        linker = new WiktionaryLinker(wiktionary);
    }

    @AfterAll
    public static void destruct() {
        wiktionary.close();
    }


    @Test
    void testLinkToSingleConcept() {
        assertTrue(linker.linkToSingleConcept("dog").equals("dog"));
        assertEquals("European_Union", linker.linkToSingleConcept("European_Union"));
        assertEquals("European_Union", linker.linkToSingleConcept("European Union"));
        assertEquals("European_Union", linker.linkToSingleConcept("EuropeanUnion"));
        assertEquals("European_Union", linker.linkToSingleConcept("europeanUnion"));
        assertEquals("European_Union", linker.linkToSingleConcept("european_union"));
        assertEquals("parietal_lobe", linker.linkToSingleConcept("parietal lobe"));
        assertEquals("EU", linker.linkToSingleConcept("EU"));
        assertEquals("lumbar_puncture", linker.linkToSingleConcept("lumbar puncture"));
        assertEquals("lumbar_puncture", linker.linkToSingleConcept("lumbar_puncture"));
        assertEquals("lumbar_puncture", linker.linkToSingleConcept("lumbar_Puncture"));
        assertEquals("lumbar_puncture", linker.linkToSingleConcept("lumbarPuncture"));
        assertEquals("dog", linker.linkToSingleConcept("dog"));
        assertEquals("hound", linker.linkToSingleConcept("hound"));
    }


    @Test
    void testLinkToPotentiallyMultipleConcepts() {

        // example 1: underscores
        HashSet<String> result1 = linker.linkToPotentiallyMultipleConcepts("House_of_Lords_dog");
        assertEquals(2, result1.size());
        assertTrue(result1.contains("House_of_Lords"));
        assertTrue(result1.contains("dog"));

        // example 2: spaces
        HashSet<String> result2 = linker.linkToPotentiallyMultipleConcepts("House of Lords dog");
        assertEquals(2, result2.size());
        assertTrue(result2.contains("House_of_Lords"));
        assertTrue(result2.contains("dog"));

        // example 3: null result due to incomplete linking
        HashSet<String> result3 = linker.linkToPotentiallyMultipleConcepts("House of Lords dog asdfffffffffff");
        assertNull(result3);
        result3 = linker.linkToPotentiallyMultipleConcepts("asdfffffffffff House of Lords dog");
        assertNull(result3);
        result3 = linker.linkToPotentiallyMultipleConcepts("House asdfffffffffff Lords dog");
        assertNull(result3);
    }

}
