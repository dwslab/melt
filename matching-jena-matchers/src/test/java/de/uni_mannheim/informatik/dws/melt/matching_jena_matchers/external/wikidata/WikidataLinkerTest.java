package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wikidata;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WikidataLinkerTest {

    @Test
    void linkToSingleConcept() {
        WikidataLinker linker = new WikidataLinker();
        String result1 = linker.linkToSingleConcept("financial services");
        assertNotNull(result1);

        // checking for concrete instances
        Set<String> individualLinks1 = linker.getLinks(result1);
        assertTrue(individualLinks1.contains("http://www.wikidata.org/entity/Q837171"));

        String result3 = linker.linkToSingleConcept("financial_services");
        assertNotNull(result3);

        String result4 = linker.linkToSingleConcept("FinancialServices");
        assertNotNull(result4);

        // null tests
        assertNull(linker.linkToSingleConcept("Some Concept That Does not Exist"));
        assertNull(linker.linkToSingleConcept("Some Concept That Does not Exist"));
        assertNull(linker.linkToSingleConcept(""));
        assertNull(linker.linkToSingleConcept(null));
    }

    @Test
    void linkToPotentiallyMultipleConcepts() {
        WikidataLinker linker = new WikidataLinker();

        // case 1: direct link test
        HashSet<String> links1 = linker.linkToPotentiallyMultipleConcepts("cocktail party");
        assertNotNull(links1);
        assertTrue(links1.size() > 0);

        // checking for concrete instances
        HashSet<String> individualLinks1 = linker.getLinks(links1);
        assertTrue(individualLinks1.contains("http://www.wikidata.org/entity/Q1105365"));
        assertFalse(individualLinks1.contains("http://www.wikidata.org/entity/Q837171"));

        // case 2: multi link test with stopwords
        HashSet<String> links2 = linker.linkToPotentiallyMultipleConcepts("peak of the Mount Everest");
        assertNotNull(links2);
        assertTrue(links2.size() > 0);
        HashSet<String> individualLinks2 = linker.getLinks(links2);
        assertTrue(individualLinks2.contains("http://www.wikidata.org/entity/Q513"));
        assertTrue(individualLinks2.contains("http://www.wikidata.org/entity/Q207326"));

        // case 3: multi link test with other Writing
        HashSet<String> links3 = linker.linkToPotentiallyMultipleConcepts("peakOfTheMountEverest");
        assertNotNull(links3);
        assertTrue(links3.size() > 0);
        HashSet<String> individualLinks3 = linker.getLinks(links2);
        assertTrue(individualLinks3.contains("http://www.wikidata.org/entity/Q513"));
        assertTrue(individualLinks3.contains("http://www.wikidata.org/entity/Q207326"));
    }

}