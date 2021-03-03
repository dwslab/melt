package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordNetKnowledgeSourceTest {


    @Test
    void testIsSynonymous() {
        WordNetKnowledgeSource wordnet = new WordNetKnowledgeSource();
        assertTrue(wordnet.isSynonymous("dog", "hound"));
        assertFalse(wordnet.isSynonymous("dog", "car"));
    }

    @Test
    void synonymyPlayground(){
        WordNetKnowledgeSource wordnet = new WordNetKnowledgeSource();
        String word = "equity";
        System.out.println("Synonyms for '" + word + "'");
        for (String synonym : wordnet.getSynonymsLexical(wordnet.getLinker().linkToSingleConcept(word))){
            System.out.println(synonym);
        }
    }

    @Test
    void testIsStrongFormSynonymous() {
        WordNetKnowledgeSource wordnet = new WordNetKnowledgeSource();
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
    void testIsInDictionary() {
        WordNetKnowledgeSource wordnet = new WordNetKnowledgeSource();
        assertTrue(wordnet.isInDictionary("car"));
        assertTrue(wordnet.isInDictionary("monkey"));
        assertTrue(wordnet.isInDictionary("milk"));
    }

    @Test
    void getSynonyms() {
        WordNetKnowledgeSource wordnet = new WordNetKnowledgeSource();
        assertNotEquals(wordnet.getSynonymsLexical("dog").size(), wordnet.getSynonymsLexical("hound").size());
    }

    @Test
    void isInDictionary() {
        WordNetKnowledgeSource wordnet = new WordNetKnowledgeSource();
        assertTrue(wordnet.isInDictionary("dog"));
        assertFalse(wordnet.isInDictionary("asdfasdfasdf"));
    }

    @Test
    void isHypernymous(){
        WordNetKnowledgeSource wordnet = new WordNetKnowledgeSource();

        assertTrue(wordnet.isHypernymous("human", "hominid"));
        assertFalse(wordnet.isHypernymous("human", "animal"));

        // linker compatibility
        LabelToConceptLinker linker = wordnet.getLinker();
        assertTrue(wordnet.isHypernymous(linker.linkToSingleConcept("human"), linker.linkToSingleConcept("hominid")));
        assertFalse(wordnet.isHypernymous(linker.linkToSingleConcept("human"), linker.linkToSingleConcept("animal")));
    }

    @Test
    void isHypernymousOrSynonymous(){
        WordNetKnowledgeSource wordnet = new WordNetKnowledgeSource();

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