package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools.TestOperations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for class {@link WordNetKnowledgeSource}
 * @author D060249
 *
 */
public class WordNetKnowledgeSourceTest {

    private static WordNetKnowledgeSource wordnet;

    @BeforeAll
    static void setup(){
        String key = "wordnetPath";
        String wordNetPath = TestOperations.getStringKeyFromResourceBundle("local_config", key);
        if(wordNetPath == null){
            wordNetPath = TestOperations.getStringKeyFromResourceBundle("config", key);
        }
        if(wordNetPath == null){
            fail("Cannot find config.properties or local_config.properties with key " + key);
        }
        wordnet = new WordNetKnowledgeSource(wordNetPath);
    }

    @Test
    public void testIsInDictionary() {
        assertTrue(wordnet.isInDictionary("car"));
        assertTrue(wordnet.isInDictionary("monkey"));
        assertTrue(wordnet.isInDictionary("milk"));
    }

    @Test
    public void testIsSynonymous() {
        assertTrue(wordnet.isSynonymous("dog", "hound"));
        assertFalse(wordnet.isSynonymous("dog", "car"));
    }

    @Test
    public void testIsStrongFormSynonymous() {
        assertTrue(wordnet.isStrongFormSynonymous("dog", "hound"));
        assertTrue(wordnet.isStrongFormSynonymous("dog", "dog"));
        assertTrue(wordnet.isStrongFormSynonymous("medulla", "bulb"));
        assertFalse(wordnet.isStrongFormSynonymous("dog", "car"));
        assertFalse(wordnet.isStrongFormSynonymous("human", "hominid"));

        // linker compatibility
        LabelToConceptLinker linker = wordnet.getLinker();
        assertTrue(wordnet.isStrongFormSynonymous(linker.linkToSingleConcept("dog"), linker.linkToSingleConcept("hound")));
        assertTrue(wordnet.isStrongFormSynonymous(linker.linkToSingleConcept("human"), linker.linkToSingleConcept("human_being")));
        assertTrue(wordnet.isStrongFormSynonymous(linker.linkToSingleConcept("human"), linker.linkToSingleConcept("human being")));
        assertFalse(wordnet.isStrongFormSynonymous(linker.linkToSingleConcept("dog"), linker.linkToSingleConcept("car")));
    }


    @Test
    public void getSynonyms() {
        assertNotEquals(wordnet.getSynonyms("dog").size(), wordnet.getSynonyms("hound").size());
    }

    @Test
    public void isInDictionary() {
        assertTrue(wordnet.isInDictionary("dog"));
        assertFalse(wordnet.isInDictionary("asdfasdfasdf"));
    }

    @Test
    public void isHypernymous(){
        assertTrue(wordnet.isHypernymous("human", "hominid"));
        assertFalse(wordnet.isHypernymous("human", "animal"));

        // linker compatibility
        LabelToConceptLinker linker = wordnet.getLinker();
        assertTrue(wordnet.isHypernymous(linker.linkToSingleConcept("human"), linker.linkToSingleConcept("hominid")));
        assertFalse(wordnet.isHypernymous(linker.linkToSingleConcept("human"), linker.linkToSingleConcept("animal")));
    }

    @Test
    public void isHypernymousOrSynonymous(){
        assertTrue(wordnet.isSynonymousOrHypernymous("human", "hominid"));
        assertTrue(wordnet.isSynonymousOrHypernymous("human", "homo"));
        assertFalse(wordnet.isSynonymousOrHypernymous("human", "dog"));

        // linker compatibility
        LabelToConceptLinker linker = wordnet.getLinker();
        assertTrue(wordnet.isSynonymousOrHypernymous(linker.linkToSingleConcept("human"), linker.linkToSingleConcept("hominid")));
        assertTrue(wordnet.isSynonymousOrHypernymous(linker.linkToSingleConcept("human"),  linker.linkToSingleConcept("homo")));
        assertFalse(wordnet.isSynonymousOrHypernymous(linker.linkToSingleConcept("human"), linker.linkToSingleConcept("dog")));

    }

}
