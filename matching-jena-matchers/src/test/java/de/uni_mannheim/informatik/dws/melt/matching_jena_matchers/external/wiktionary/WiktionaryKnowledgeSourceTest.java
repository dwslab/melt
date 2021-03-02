package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wiktionary;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test requires a working internet connection.
 */
public class WiktionaryKnowledgeSourceTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(WiktionaryKnowledgeSourceTdbTest.class);

    @BeforeAll
    @AfterAll
    public static void prepareAndTearDown() {
        deletePersistenceDirectory();
    }

    /**
     * Delete the persistence directory.
     */
    private static void deletePersistenceDirectory() {
        PersistenceService.getService().closePersistenceService();
        File result = new File(PersistenceService.PERSISTENCE_DIRECTORY);
        try {
            FileUtils.deleteDirectory(result);
        } catch (IOException e) {
            LOGGER.error("Failed to remove persistence directory.");
        }

    }

    @Test
    public void testIsInDictionaryString() {
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();

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
    void encodeWord() {
        // we need this space encoding to ensure that it works on DBnary:
        assertEquals("European_Union", WiktionaryKnowledgeSource.encodeWord("European Union"));
    }

    @Test
    public void testIsInDictionaryStringDBNaryLanguage() {
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();

        // true positive check
        assertTrue(wiktionary.isInDictionary("cat", Language.ENGLISH));

        // true positive check; check for correct encoding of spaces
        assertTrue(wiktionary.isInDictionary("seminal fluid", Language.ENGLISH));

        // false positive check
        assertFalse(wiktionary.isInDictionary("asdfasdfasdf", Language.ENGLISH));
    }

    @Test
    public void testGetSynonymsString() {
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();

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
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();

        // buffer check
        int numberOfSynonyms1 = wiktionary.getSynonymsLexical("cat").size();
        int numberOfSynonyms2 = wiktionary.getSynonymsLexical("cat").size();
        assertTrue(numberOfSynonyms1 == numberOfSynonyms2);
    }

    @Test
    public void testIsSynonymous() {
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();
        assertTrue(wiktionary.isSynonymous("dog", "hound"));
        assertTrue(wiktionary.isSynonymous("dog", "dog"));
        assertFalse(wiktionary.isSynonymous("dog", "cat"));
    }

    @Test
    public void testIsStrongFromSynonymous() {
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();
        assertTrue(wiktionary.isStrongFormSynonymous("dog", "hound"));
        assertTrue(wiktionary.isStrongFormSynonymous("dog", "dog"));
        assertFalse(wiktionary.isStrongFormSynonymous("dog", "cat"));
    }

    @Test
    public void testHypernymy() {
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();
        assertTrue(wiktionary.getHypernyms("cat").contains("feline"));
        assertFalse(wiktionary.getHypernyms("cat").contains("dog"));

        // assert linking process compatibility
        assertTrue(wiktionary.getHypernyms(wiktionary.getLinker().linkToSingleConcept("cat")).contains("feline"));
        assertFalse(wiktionary.getHypernyms(wiktionary.getLinker().linkToSingleConcept("cat")).contains("dog"));
    }

    @Test
    void isSynonymousOrHypernymyous() {
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();
        assertTrue(wiktionary.isSynonymousOrHypernymous("cat", "feline"));
        assertTrue(wiktionary.isSynonymousOrHypernymous("dog", "hound"));
        assertFalse(wiktionary.isSynonymousOrHypernymous("dog", "cat"));

        // linking process compatibility
        assertTrue(wiktionary.isSynonymousOrHypernymous(wiktionary.getLinker().linkToSingleConcept("cat"), wiktionary.getLinker().linkToSingleConcept("feline")));
        assertTrue(wiktionary.isSynonymousOrHypernymous(wiktionary.getLinker().linkToSingleConcept("dog"), wiktionary.getLinker().linkToSingleConcept("hound")));
        assertFalse(wiktionary.isSynonymousOrHypernymous(wiktionary.getLinker().linkToSingleConcept("dog"), wiktionary.getLinker().linkToSingleConcept("cat")));
    }
}
