package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wiktionary;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools.TestOperations;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools.TestOperations.getKeyFromConfigFiles;
import static org.junit.jupiter.api.Assertions.*;

public class WiktionaryKnowledgeSourceTdbTest {


    private static WiktionaryKnowledgeSource wiktionary;

    private static final Logger LOGGER = LoggerFactory.getLogger(WiktionaryKnowledgeSourceTdbTest.class);

    @BeforeAll
    public static void prepare() {
        deletePersistenceDirectory();
        String key = "wiktionaryTdbDirectory";
        String tdbpath = getKeyFromConfigFiles("wiktionaryTdbDirectory");
        if(tdbpath == null){
            fail("Cannot find config.properties or local_config.properties with key " + key);
        }
        wiktionary = new WiktionaryKnowledgeSource(tdbpath);
    }

    @AfterAll
    public static void shutDown() {
        wiktionary.close();
        deletePersistenceDirectory();
    }

    /**
     * Delete the persistence directory.
     */
    private static void deletePersistenceDirectory() {
        File result = new File(PersistenceService.PERSISTENCE_DIRECTORY);
        if (result != null && result.exists() && result.isDirectory()) {
            try {
                FileUtils.deleteDirectory(result);
            } catch (IOException e) {
                LOGGER.error("Failed to remove persistence directory.");
            }
        }
    }

    @Test
    void encodeWord(){
        // we need this space encoding to ensure that it works on DBnary:
        assertEquals("European_Union", WiktionaryKnowledgeSource.encodeWord("European Union"));
    }

    @Test
    public void testIsInDictionaryString() {
        // true positive check
        assertTrue(wiktionary.isInDictionary("dog"));

        // true positive check; check for correct encoding of spaces
        assertTrue(wiktionary.isInDictionary("seminal fluid"));

        // true positive check; check for correct encoding of %
        assertTrue(wiktionary.isInDictionary("%"));

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
        assertTrue(wiktionary.getSynonymsLexical("cat").size() > 0);

        // second test for buffer
        assertTrue(wiktionary.getSynonymsLexical("cat").size() > 0);

        // checking for one specific synonym
        assertTrue(wiktionary.getSynonymsLexical("temporal muscle").contains("temporalis"));

        // checking for non-existing synonym
        assertNull(wiktionary.getSynonymsLexical("asdfasdfasdf"));
    }

    @Test
    public void testGetSynonymsStringDBNaryLanguage() {
        // buffer check
        int numberOfSynonyms1 = wiktionary.getSynonymsLexical("cat").size();
        int numberOfSynonyms2 = wiktionary.getSynonymsLexical("cat").size();
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
