package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.babelnet;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BabelNetKnowledgeSourceTest {


    private static BabelNetKnowledgeSource dictionary = new BabelNetKnowledgeSource();

    /**
     * Not an actual test but can be used for quick experiments.
     */
    @Test
    void synonymyPlayground(){
        String term = "European Union";
        System.out.println("Synonyms for '" + term + "'");
        for(String s: dictionary.getSynonymsLexical(dictionary.getLinker().linkToSingleConcept(term))){
            System.out.println(s);
        }
    }

    @Test
    void isInDictionary() {
        assertTrue(dictionary.isInDictionary("european Union"));
        assertTrue(dictionary.isInDictionary("European_Union"));
        assertTrue(dictionary.isInDictionary("Europe"));
    }

    @Test
    void getSynonyms() {
        Set<String> result = dictionary.getSynonymsLexical("macron");
        assertTrue(result.contains("emmanuel_macron"));
    }

    @Test
    void isSynonymous(){
        String testStringMacron_1 = "Macron";
        String testStringMacron_2 = "emmanuel macron";
        String testStringCar = "car";
        String testStringAirplane = "airplane";

        // axioms
        assertTrue(dictionary.isInDictionary(testStringMacron_1));
        assertTrue(dictionary.isInDictionary(testStringMacron_2));
        assertTrue(dictionary.isInDictionary(testStringCar));
        assertTrue(dictionary.isInDictionary(testStringAirplane));

        // actual synonymy check
        assertTrue(dictionary.isSynonymous(testStringMacron_1, testStringMacron_2));
        assertFalse(dictionary.isSynonymous(testStringCar, testStringAirplane));
    }

    @Test
    void isStrongFormSynonymous(){
        String testStringMacron_1 = "Macron";
        String testStringMacron_2 = "emmanuel macron";
        String testStringCar = "car";
        String testStringAirplane = "airplane";

        // axioms
        assertTrue(dictionary.isInDictionary(testStringMacron_1));
        assertTrue(dictionary.isInDictionary(testStringMacron_2));
        assertTrue(dictionary.isInDictionary(testStringCar));
        assertTrue(dictionary.isInDictionary(testStringAirplane));

        // standard case
        assertFalse(dictionary.isStrongFormSynonymous(testStringCar, testStringAirplane));

        // interesting case: Macron is not found as synonym b/c it is not lowercased. This is due to missing linking,
        // but also shows that the strong for synonymy does not look for overlaps in synonymy sets but requires
        // that the word if found as direct synonym.
        assertFalse(dictionary.isStrongFormSynonymous(testStringMacron_1, testStringMacron_2));

        // now it will work b/c a linking process is used that will lowercase "Macron"
        String linkedtestStringMacron_1 = dictionary.getLinker().linkToSingleConcept(testStringMacron_1);
        String linkedtestStringMacron_2 = dictionary.getLinker().linkToSingleConcept(testStringMacron_2);
        assertTrue(dictionary.isStrongFormSynonymous(linkedtestStringMacron_1, linkedtestStringMacron_2));
    }

    @Test
    void getHyponyms(){
        String testString_1 = "politician";
        assertTrue(dictionary.getHypernyms(testString_1).contains("leader"));
        assertFalse(dictionary.getHypernyms(testString_1).contains("animal"));
    }

    @Test
    void isHypernymous(){
        String testString_1 = "politician";
        String testString_2 = "leader";
        String testString_3 = "animal";
        String testString_4 = "Macron";
        String testString_5 = "emmanuel macron";
        assertTrue(dictionary.isHypernymous(testString_1, testString_2));
        assertTrue(dictionary.isHypernymous(testString_2, testString_1));
        assertFalse(dictionary.isHypernymous(testString_2, testString_3));
        assertFalse(dictionary.isHypernymous(testString_3, testString_2));
        assertFalse(dictionary.isHypernymous(testString_4, testString_5));

        // test graceful failure
        assertFalse(dictionary.isHypernymous(null, testString_5));
        assertFalse(dictionary.isHypernymous(testString_5, null));
        assertFalse(dictionary.isHypernymous(null, null));
    }

    @Test
    void isSynonymousOrHypernymyous(){
        String testString_1 = "politician";
        String testString_2 = "leader";
        String testString_3 = "animal";
        String testString_4 = "Macron";
        String testString_5 = "emmanuel macron";
        String testString_6 = "macron";

        assertTrue(dictionary.isSynonymousOrHypernymous(testString_1, testString_2));
        assertTrue(dictionary.isSynonymousOrHypernymous(testString_2, testString_1));
        assertTrue(dictionary.isSynonymousOrHypernymous(testString_5, testString_6));

        // now test again to run into buffer
        assertTrue(dictionary.isSynonymousOrHypernymous(testString_1, testString_2));
        assertTrue(dictionary.isSynonymousOrHypernymous(testString_2, testString_1));
        assertTrue(dictionary.isSynonymousOrHypernymous(testString_5, testString_6));

        // interesting case: Macron is not found as synonym b/c it is not lowercased. This is due to missing linking,
        // but also shows that the strong for synonymy does not look for overlaps in synonymy sets but requires
        // that the word if found as direct synonym.
        assertFalse(dictionary.isSynonymousOrHypernymous(testString_4, testString_5));

        assertFalse(dictionary.isSynonymousOrHypernymous(testString_2, testString_3));
        assertFalse(dictionary.isSynonymousOrHypernymous(testString_3, testString_2));

        // test graceful failure
        assertFalse(dictionary.isHypernymous(null, testString_5));
        assertFalse(dictionary.isHypernymous(testString_5, null));
        assertFalse(dictionary.isHypernymous(null, null));
    }
}