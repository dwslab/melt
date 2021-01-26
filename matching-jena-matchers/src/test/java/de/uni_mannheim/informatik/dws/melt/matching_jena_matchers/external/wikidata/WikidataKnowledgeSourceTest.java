package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wikidata;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class WikidataKnowledgeSourceTest {

    @Test
    void isInDictionary() {
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        assertTrue(wikidata.isInDictionary("derivative"));
        assertTrue(wikidata.isInDictionary("Munich"));
        assertFalse(wikidata.isInDictionary("adsfasdfasdfasdfadsfasdfadsfasd"));

        // try with language codes
        assertTrue(wikidata.isInDictionary("derivative", Language.ENGLISH));
        assertFalse(wikidata.isInDictionary("adsfasdfasdfasdfadsfasdfadsfasd", Language.ENGLISH));
        assertFalse(wikidata.isInDictionary("Privates Gymnasium Sank Paulusheim", Language.ENGLISH));
        assertTrue(wikidata.isInDictionary("St. Paulusheim", Language.GERMAN));
        assertTrue(wikidata.isInDictionary("Privatgymnasium Sankt Paulusheim", Language.GERMAN));
    }

    @Test
    void getSynonyms() {
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        HashSet<String> result1 = wikidata.getSynonyms(wikidata.getLinker().linkToSingleConcept("financial services"), Language.ENGLISH); // Q837171
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("FS industry"));

        // re-run to trigger buffer
        result1 = wikidata.getSynonyms(wikidata.getLinker().linkToSingleConcept("financial services"), Language.ENGLISH); // Q837171
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("FS industry"));

        // try in another language
        HashSet<String> result2 = wikidata.getSynonyms(wikidata.getLinker().linkToSingleConcept("financial services"), Language.GERMAN); // Q837171
        assertTrue(result2.size() > 0);
        assertTrue(result2.contains("Finanzbranche"));
    }

    @Test
    void getHypernymsLexical() {
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        HashSet<String> result1 = wikidata.getHypernymsLexical(wikidata.getLinker().linkToSingleConcept("financial services"));
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("business service")); // label of Q25351891
        assertTrue(result1.contains("business services")); // alternative label of Q25351891

        // re-test to check buffer functionality
        result1 = wikidata.getHypernymsLexical(wikidata.getLinker().linkToSingleConcept("financial services"));
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("business service")); // label of Q25351891
        assertTrue(result1.contains("business services")); // alternative label of Q25351891

        // try in another language to check multi lingual capabilities
        HashSet<String> result2 = wikidata.getHypernymsLexical(wikidata.getLinker().linkToSingleConcept("financial services"), Language.GERMAN);
        assertTrue(result2.size() > 0);
        assertTrue(result2.contains("Unternehmensdienstleistung")); // label of Q25351891
        assertTrue(result2.contains("Unternehmensbezogene Dienstleistung")); // alternative label of Q25351891
    }

    @Test
    void getHypernyms() {
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        HashSet<String> result1 = wikidata.getHypernyms(wikidata.getLinker().linkToSingleConcept("financial services"));
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("http://www.wikidata.org/entity/Q268592"));
    }

    @Test
    void getlabelsForLink() {
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        HashSet<String> result1 = wikidata.getLabelsForLink(wikidata.getLinker().linkToSingleConcept("financial services"), Language.GERMAN);
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("Finanzgewerbe"));

        // re-test to check buffer functionality
        result1 = wikidata.getLabelsForLink(wikidata.getLinker().linkToSingleConcept("financial services"), Language.GERMAN);
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("Finanzgewerbe"));

        // try in another language to check multi-language feature
        HashSet<String> result2 = wikidata.getLabelsForLink(wikidata.getLinker().linkToSingleConcept("financial services"), Language.ENGLISH);
        assertTrue(result2.size() > 0);
        assertTrue(result2.contains("financial services"));
    }
}