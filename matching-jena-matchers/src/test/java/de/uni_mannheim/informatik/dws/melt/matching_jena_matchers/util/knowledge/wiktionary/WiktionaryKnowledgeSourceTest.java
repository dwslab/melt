package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.wiktionary;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.testTools.TestOperations;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WiktionaryKnowledgeSourceTest {

    public static WiktionaryKnowledgeSource wiktionary;

    @BeforeAll
    public static void prepare() {
        String key = "wiktionaryTdbDirectory";
        String tdbpath = TestOperations.getStringKeyFromResourceBundle("config", key);
        if(tdbpath == null){
            tdbpath = TestOperations.getStringKeyFromResourceBundle("local_config", key);
        }
        if(tdbpath == null){
            fail("Cannot find config.properties or local_config.properties with key " + key);
        }
        wiktionary = new WiktionaryKnowledgeSource(tdbpath);
    }

    @AfterAll
    public static void shutDown() { wiktionary.close(); }

    @Test
    public void testIsInDictionaryString() {
        // true positive check
        assertTrue(wiktionary.isInDictionary("dog"));

        // true positive check; check for correct encoding of spaces
        assertTrue(wiktionary.isInDictionary("seminal fluid"));

        // false positive check
        assertFalse(wiktionary.isInDictionary("asdfasdfasdf"));
    }

    @Test
    public void testIsInDictionaryStringDBNaryLanguage() {
        // true positive check
        assertTrue(wiktionary.isInDictionary("cat", Language.ENGLISH));

        // true positive check; check for correct encoding of spaces
        assertTrue(wiktionary.isInDictionary("seminal fluid", Language.ENGLISH));

        // false positive check
        assertFalse(wiktionary.isInDictionary("asdfasdfasdf", Language.ENGLISH));
    }

    @Test
    public void testGetSynonymsString() {
        // just checking that there are synonyms
        assertTrue(wiktionary.getSynonyms("cat").size() > 0);

        // second test for buffer
        assertTrue(wiktionary.getSynonyms("cat").size() > 0);

        // checking for one specific synonym
        assertTrue(wiktionary.getSynonyms("temporal muscle").contains("temporalis"));

        // checking for non-existing synonym
        assertNull(wiktionary.getSynonyms("asdfasdfasdf"));

    }

    @Test
    public void testGetSynonymsStringDBNaryLanguage() {
        // buffer check
        int numberOfSynonyms1 = wiktionary.getSynonyms("cat").size();
        int numberOfSynonyms2 = wiktionary.getSynonyms("cat").size();
        assertTrue(numberOfSynonyms1 == numberOfSynonyms2);
    }

    @Test
    public void testIsSynonymous() {
        assertTrue(wiktionary.isSynonymous("dog", "hound"));
        assertTrue(wiktionary.isSynonymous("dog", "dog"));
        assertFalse(wiktionary.isSynonymous("dog", "cat"));
    }

    @Test
    public void testIsStrongFromSynonymous(){
        assertTrue(wiktionary.isStrongFormSynonymous("dog", "hound"));
        assertTrue(wiktionary.isStrongFormSynonymous("dog", "dog"));
        assertFalse(wiktionary.isStrongFormSynonymous("dog", "cat"));
    }

    @Test
    public void testHypernymy(){
        assertTrue(wiktionary.getHypernyms("cat").contains("feline"));
        assertFalse(wiktionary.getHypernyms("cat").contains("dog"));

        // assert linking process compatibility
        assertTrue(wiktionary.getHypernyms(wiktionary.getLinker().linkToSingleConcept("cat")).contains("feline"));
        assertFalse(wiktionary.getHypernyms(wiktionary.getLinker().linkToSingleConcept("cat")).contains("dog"));
    }

    @Test
    void isSynonymousOrHypernymyous(){
        assertTrue(wiktionary.isSynonymousOrHypernymous("cat", "feline"));
        assertTrue(wiktionary.isSynonymousOrHypernymous("dog", "hound"));
        assertFalse(wiktionary.isSynonymousOrHypernymous("dog", "cat"));

        // linking process compatibility
        assertTrue(wiktionary.isSynonymousOrHypernymous(wiktionary.getLinker().linkToSingleConcept("cat"), wiktionary.getLinker().linkToSingleConcept("feline")));
        assertTrue(wiktionary.isSynonymousOrHypernymous(wiktionary.getLinker().linkToSingleConcept("dog"), wiktionary.getLinker().linkToSingleConcept("hound")));
        assertFalse(wiktionary.isSynonymousOrHypernymous(wiktionary.getLinker().linkToSingleConcept("dog"), wiktionary.getLinker().linkToSingleConcept("cat")));
    }

}
