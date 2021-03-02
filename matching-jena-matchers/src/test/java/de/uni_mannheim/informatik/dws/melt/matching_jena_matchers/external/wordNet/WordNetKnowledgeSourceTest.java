package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools.TestOperations;
import it.uniroma1.lcl.jlt.util.Files;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

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

    @AfterAll
    @BeforeAll
    static void deleteBuffers(){
        PersistenceService.getService().closePersistenceService();
        File buffer = new File(PersistenceService.PERSISTENCE_DIRECTORY);
        if(buffer.exists() && buffer.isDirectory()) {
            Files.deleteDirectory(buffer);
        }
    }

    @Test
    void synonymyPlayground(){
        String word = "equity";
        System.out.println("Synonyms for '" + word + "'");
        for (String synonym : wordnet.getSynonymsLexical(wordnet.getLinker().linkToSingleConcept(word))){
            System.out.println(synonym);
        }
    }

    @Test
    void testIsInDictionary() {
        assertTrue(wordnet.isInDictionary("car"));
        assertTrue(wordnet.isInDictionary("monkey"));
        assertTrue(wordnet.isInDictionary("milk"));
    }

    @Test
    void testIsSynonymous() {
        assertTrue(wordnet.isSynonymous("dog", "hound"));
        assertFalse(wordnet.isSynonymous("dog", "car"));
    }

    @Test
    void testIsStrongFormSynonymous() {
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
    void getSynonyms() {
        assertNotEquals(wordnet.getSynonymsLexical("dog").size(), wordnet.getSynonymsLexical("hound").size());
    }

    @Test
    void isInDictionary() {
        assertTrue(wordnet.isInDictionary("dog"));
        assertFalse(wordnet.isInDictionary("asdfasdfasdf"));
    }

    @Test
    void isHypernymous(){
        assertTrue(wordnet.isHypernymous("human", "hominid"));
        assertFalse(wordnet.isHypernymous("human", "animal"));

        // linker compatibility
        LabelToConceptLinker linker = wordnet.getLinker();
        assertTrue(wordnet.isHypernymous(linker.linkToSingleConcept("human"), linker.linkToSingleConcept("hominid")));
        assertFalse(wordnet.isHypernymous(linker.linkToSingleConcept("human"), linker.linkToSingleConcept("animal")));
    }

    @Test
    void isHypernymousOrSynonymous(){
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
