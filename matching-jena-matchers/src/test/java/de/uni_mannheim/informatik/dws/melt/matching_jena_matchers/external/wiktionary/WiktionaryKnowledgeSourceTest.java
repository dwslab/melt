package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wiktionary;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test requires a working internet connection.
 * It further requires that the public DBnary SPARQL endpoint is online:
 * <a href="http://kaiko.getalp.org/sparql">http://kaiko.getalp.org/sparql</a>
 */
public class WiktionaryKnowledgeSourceTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(WiktionaryKnowledgeSourceTest.class);

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
        File result = new File(PersistenceService.DEFAULT_PERSISTENCE_DIRECTORY);
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

        assertTrue(wiktionary.isInDictionary("Alzheimer's"));
        assertTrue(wiktionary.isInDictionary("Alzheimer's\n"));
        assertTrue(wiktionary.isInDictionary("Alzheimer's Disease"));

        // true positive with language
        assertTrue(wiktionary.isInDictionary("Ähre", Language.GERMAN ));

        // false positive check; check for stability with random signs
        assertFalse(wiktionary.isInDictionary("<"));

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
        assertTrue(wiktionary.getSynonymsLexical("dog").contains("hound"));

        // checking for non-existing synonym
        assertNull(wiktionary.getSynonymsLexical("asdfasdfasdf"));
    }

    @Test
    void testGetSynonymsStringDBNaryLanguage() {
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();

        // buffer check
        int numberOfSynonyms1 = wiktionary.getSynonymsLexical("cat").size();
        int numberOfSynonyms2 = wiktionary.getSynonymsLexical("cat").size();
        assertTrue(numberOfSynonyms1 == numberOfSynonyms2);
    }

    @Test
    void getTranslationOf(){
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();

        // run 1
        HashSet<String> result = wiktionary.getTranslationOf("bed", Language.ENGLISH);
        assertNotNull(result);
        assertTrue(result.size() > 0);
        assertTrue(result.contains("http://kaiko.getalp.org/dbnary/fra/lit"));

        // run 2 to test buffer
        result = wiktionary.getTranslationOf("bed", Language.ENGLISH);
        assertNotNull(result);
        assertTrue(result.size() > 0);
        assertTrue(result.contains("http://kaiko.getalp.org/dbnary/fra/lit"));

        // trying a french translation
        result = wiktionary.getTranslationOf("lit", Language.FRENCH);
        assertNotNull(result);
        assertTrue(result.size() > 0);
        assertTrue(result.contains("http://kaiko.getalp.org/dbnary/deu/Bett"));
    }

    @Test
    void testIsSynonymous() {
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();
        assertTrue(wiktionary.isSynonymous("dog", "hound"));
        assertTrue(wiktionary.isSynonymous("dog", "dog"));
        assertFalse(wiktionary.isSynonymous("dog\n", "cat"));
    }

    @Test
    public void testIsStrongFromSynonymous() {
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();
        assertTrue(wiktionary.isStrongFormSynonymous("dog\n", "hound"));
        assertTrue(wiktionary.isStrongFormSynonymous("dog", "dog"));
        assertFalse(wiktionary.isStrongFormSynonymous("dog", "cat"));
    }

    @Test
    public void testHypernymy() {
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();
        WiktionaryLinker linker = (WiktionaryLinker) wiktionary.getLinker();

        // using default language
        assertTrue(wiktionary.getHypernyms("cat").contains("feline"));
        assertFalse(wiktionary.getHypernyms("cat").contains("dog"));

        // assert linking process compatibility
        assertTrue(wiktionary.getHypernyms(wiktionary.getLinker().linkToSingleConcept("cat")).contains("feline"));
        assertFalse(wiktionary.getHypernyms(wiktionary.getLinker().linkToSingleConcept("cat")).contains("dog"));
    }

    @Test
    void isTranslation(){
        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();

        // English/German
        assertTrue(wiktionary.isTranslationLinked("bed", Language.ENGLISH, "Bett", Language.GERMAN));
        assertTrue(wiktionary.isTranslationLinked("conference", Language.ENGLISH, "Tagung", Language.GERMAN));
        assertTrue(wiktionary.isTranslationLinked("conference", Language.ENGLISH, "Konferenz", Language.GERMAN));
        assertFalse(wiktionary.isTranslationLinked("conference", Language.ENGLISH, "Bett", Language.GERMAN));

        assertTrue(wiktionary.isTranslationLinked("Bett", Language.GERMAN, "bed", Language.ENGLISH));
        assertTrue(wiktionary.isTranslationLinked("Tagung", Language.GERMAN, "conference", Language.ENGLISH));
        assertTrue(wiktionary.isTranslationLinked("Konferenz", Language.GERMAN, "conference", Language.ENGLISH));
        assertFalse(wiktionary.isTranslationLinked("Bett", Language.GERMAN, "conference", Language.ENGLISH));

        // Russian/French
        assertTrue(wiktionary.isTranslationLinked("критика", Language.RUSSIAN, "critique", Language.FRENCH));
    }

    /**
     * Note that for this test, all dbnary core dumps have to be added to the TDB data set.
     */
    @Test
    void getTranslation(){

        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();

        //---------------------
        // From Russian
        //---------------------

        // to French
        HashSet<String> russianFrenchTranslation = wiktionary.getTranslation("рецензия", Language.RUSSIAN, Language.FRENCH);
        assertTrue(russianFrenchTranslation.contains("critique"));

        // to French (again to test buffer)
        russianFrenchTranslation = wiktionary.getTranslation("рецензия", Language.RUSSIAN, Language.FRENCH);
        assertTrue(russianFrenchTranslation.contains("critique"));

        //---------------------
        // From Dutch
        //---------------------

        // to English
        HashSet<String> dutchSpanishTranslation = wiktionary.getTranslation("topconferentie", Language.DUTCH, Language.ENGLISH);
        assertTrue(dutchSpanishTranslation.contains("summit conference"));

        // running again to check buffer
        dutchSpanishTranslation = wiktionary.getTranslation("topconferentie", Language.DUTCH, Language.ENGLISH);
        assertTrue(dutchSpanishTranslation.contains("summit conference"));

        //---------------------
        // From German
        //---------------------

        // to Italian
        HashSet<String> germanItalianTranslation = wiktionary.getTranslation("Konferenz", Language.GERMAN, Language.ITALIAN);
        assertTrue(germanItalianTranslation.contains("conferenza"));


        //---------------------
        // From French
        //---------------------

        // to Italian
        HashSet<String> frenchItalianTranslation = wiktionary.getTranslation("conférence", Language.FRENCH, Language.ITALIAN);
        assertTrue(frenchItalianTranslation.contains("conferenza"));


        //---------------------
        // From Portugese
        //---------------------

        // to Italian
        HashSet<String> portugeseItalianTranslation = wiktionary.getTranslation("banco", Language.PORTUGUESE,
                Language.ITALIAN);
        assertTrue(portugeseItalianTranslation.contains("banca"));


        //---------------------
        // From Spanish
        //---------------------

        // to German
        HashSet<String> spanishGermanTranslation = wiktionary.getTranslation("banco", Language.SPANISH, Language.GERMAN);
        assertTrue(spanishGermanTranslation.contains("Bank"));


        //---------------------
        // From Italian
        //---------------------

        // to German
        HashSet<String> italianGermanTranslation = wiktionary.getTranslation("banca", Language.ITALIAN, Language.FRENCH);
        assertTrue(italianGermanTranslation.contains("banque"));


        //---------------------
        // From English
        //---------------------

        // to German
        HashSet<String> germanTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.GERMAN);
        assertTrue(germanTranslation.contains("Konferenz"));

        // to Dutch
        HashSet<String> dutchTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.DUTCH);
        assertTrue(dutchTranslation.contains("conferentie"));

        // to Arabic
        HashSet<String> arabicTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.ARABIC);
        assertTrue(arabicTranslation.contains("مُؤْتَمَر"));

        // to Chinese
        HashSet<String> chineseTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.CHINESE);
        assertTrue(chineseTranslation.contains("會議"));

        // to French
        HashSet<String> frenchTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.FRENCH);
        assertTrue(frenchTranslation.contains("conférence"));

        // to Italian
        HashSet<String> italianTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.ITALIAN);
        assertTrue(italianTranslation.contains("conferenza"));

        // to Portugese
        HashSet<String> portugeseTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.PORTUGUESE);
        assertTrue(portugeseTranslation.contains("conferência"));

        // to Russian
        HashSet<String> russianTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.RUSSIAN);
        assertTrue(russianTranslation.contains("конфере́нция"));

        // to Spanish
        HashSet<String> spanishTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.SPANISH);
        assertTrue(spanishTranslation.contains("conferencia"));
    }

    @Test
    void isTranslationOf(){

        WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();

        // German Word
        HashSet<String> translationPartnerForGermanBank = wiktionary.getTranslationOf("Bank", Language.GERMAN);
        boolean engTranslationForGermanBankAppears = false;
        assertNotNull(translationPartnerForGermanBank);
        for(String uris : translationPartnerForGermanBank){
            if(uris.equals("http://kaiko.getalp.org/dbnary/eng/bank")) engTranslationForGermanBankAppears = true;
        }
        assertTrue(engTranslationForGermanBankAppears);


        // Czech Word
        HashSet<String> translationPartnerForCzechBank = wiktionary.getTranslationOf("banka", Language.CZECH);
        boolean engTranslationForCzechankAppears = false;
        assertNotNull(translationPartnerForCzechBank);
        for(String uris : translationPartnerForCzechBank){
            if(uris.equals("http://kaiko.getalp.org/dbnary/eng/bank")) engTranslationForCzechankAppears = true;
        }
        assertTrue(engTranslationForCzechankAppears);

        /*
        // Chinese Word
        HashSet<String> translationPartnerForChineseFather = wiktionary.getTranslationOf("爸爸", Language.CHINESE);
        boolean engTranslationForChineseFatherAppears = false;
        assertNotNull(translationPartnerForChineseFather);
        for(String uris : translationPartnerForChineseFather){
            if(uris.equals("http://kaiko.getalp.org/dbnary/eng/father")) engTranslationForChineseFatherAppears = true;
        }
        assertTrue(engTranslationForChineseFatherAppears);
*/
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
