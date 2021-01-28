package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wikidata;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WikidataLinkerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikidataLinkerTest.class);

    @BeforeAll
    static void setup() {
        deletePersistenceDirectory();
    }

    @AfterAll
    static void tearDown() {
        deletePersistenceDirectory();
    }

    /**
     * Delete the persistence directory.
     */
    private static void deletePersistenceDirectory() {
        File result = new File(PersistenceService.PERSISTENCE_DIRECTORY);
        if (result.exists() && result.isDirectory()) {
            try {
                FileUtils.deleteDirectory(result);
            } catch (IOException e) {
                LOGGER.error("Failed to remove persistence directory.", e);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void linkToSingleConcept(boolean isDiskBufferEnabled) {
        WikidataLinker linker = new WikidataLinker(isDiskBufferEnabled);
        assertEquals(isDiskBufferEnabled, linker.isDiskBufferEnabled());
        linker.setRunAllStringModifications(true);
        assertTrue(linker.isRunAllStringModifications());

        // default test
        String result1 = linker.linkToSingleConcept("financial services");
        assertNotNull(result1);

        // quickly check buffer
        result1 = linker.linkToSingleConcept("financial services");
        assertNotNull(result1);

        // checking for concrete instances
        Set<String> individualLinks1 = linker.getUris(result1);
        assertTrue(individualLinks1.contains("http://www.wikidata.org/entity/Q837171"));

        String result3 = linker.linkToSingleConcept("financial_services");
        assertNotNull(result3);

        String result4 = linker.linkToSingleConcept("FinancialServices");
        assertNotNull(result4);

        String result5 = linker.linkToSingleConcept("Contingent Convertible Bonds");
        assertTrue(linker.getUris(result5).contains("http://www.wikidata.org/entity/Q1104031"));

        String result6 = linker.linkToSingleConcept("Contingent convertible bonds");
        assertTrue(linker.getUris(result6).contains("http://www.wikidata.org/entity/Q1104031"));

        String result7 = linker.linkToSingleConcept("Options");
        assertTrue(linker.getUris(result7).contains("http://www.wikidata.org/entity/Q187860"));

        // null tests
        assertNull(linker.linkToSingleConcept("Some Concept That Does not Exist"));
        assertNull(linker.linkToSingleConcept("Some Concept That Does not Exist"));
        assertNull(linker.linkToSingleConcept(""));
        assertNull(linker.linkToSingleConcept(null));


        linker.setRunAllStringModifications(false);
        assertFalse(linker.isRunAllStringModifications());

        // now let's re-run some concepts linked before using another strategy option:
        result3 = linker.linkToSingleConcept("financial_services");
        assertNotNull(result3);

        result4 = linker.linkToSingleConcept("FinancialServices");
        assertNotNull(result4);

        // sanity checks
        assertNull(linker.linkToSingleConcept("Some Concept That Does not Exist"));
        assertNull(linker.linkToSingleConcept("Some Concept That Does not Exist"));
        assertNull(linker.linkToSingleConcept(""));
        assertNull(linker.linkToSingleConcept(null));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void linkToPotentiallyMultipleConcepts(boolean isDiskBufferEnabled) {
        WikidataLinker linker = new WikidataLinker(isDiskBufferEnabled);
        assertEquals(isDiskBufferEnabled, linker.isDiskBufferEnabled());

        // case 1: direct link test
        HashSet<String> links1 = linker.linkToPotentiallyMultipleConcepts("cocktail party");
        assertNotNull(links1);
        assertTrue(links1.size() > 0);

        // checking for concrete instances
        HashSet<String> individualLinks1 = linker.getUris(links1);
        assertTrue(individualLinks1.contains("http://www.wikidata.org/entity/Q1105365"));
        assertFalse(individualLinks1.contains("http://www.wikidata.org/entity/Q837171"));

        // case 2: multi link test with stopwords
        HashSet<String> links2 = linker.linkToPotentiallyMultipleConcepts("peak of the Mount Everest");
        assertNotNull(links2);
        assertTrue(links2.size() > 0);
        HashSet<String> individualLinks2 = linker.getUris(links2);
        assertTrue(individualLinks2.contains("http://www.wikidata.org/entity/Q513"));
        assertTrue(individualLinks2.contains("http://www.wikidata.org/entity/Q207326"));

        // case 3: multi link test with other Writing
        HashSet<String> links3 = linker.linkToPotentiallyMultipleConcepts("peakOfTheMountEverest");
        assertNotNull(links3);
        assertTrue(links3.size() > 0);
        HashSet<String> individualLinks3 = linker.getUris(links2);
        assertTrue(individualLinks3.contains("http://www.wikidata.org/entity/Q513"));
        assertTrue(individualLinks3.contains("http://www.wikidata.org/entity/Q207326"));
    }

}