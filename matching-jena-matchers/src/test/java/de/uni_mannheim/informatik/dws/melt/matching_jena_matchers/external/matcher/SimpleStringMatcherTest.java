package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class SimpleStringMatcherTest {


    private static Logger LOGGER = LoggerFactory.getLogger(SimpleStringMatcherTest.class);

    /**
     * Very simple test asserting that we do not get a match exception and that the alignment is not empty.
     */
    @Test
    void match() {
        TestCase tc = TrackRepository.Anatomy.Default.getFirstTestCase();
        SimpleStringMatcher matcher = new SimpleStringMatcher();
        try {
            Alignment result = matcher.match(tc.getSourceOntology(OntModel.class), tc.getTargetOntology(OntModel.class),
                    null, null);
            assertNotNull(result.size());
            assertTrue(result.size() > 10);
        } catch (Exception e){
            LOGGER.error("A match exception occurred.", e);
            fail();
        }
    }

    /**
     * Very simple test asserting that previous correspondences are kept.
     */
    @Test
    void matchWithExistingInputAlignment() {
        TestCase tc = TrackRepository.Conference.V1.getFirstTestCase();
        SimpleStringMatcher matcher = new SimpleStringMatcher();
        try {
            Alignment inputAlignment = new Alignment();
            Correspondence c = new Correspondence("ENTITY_1_TEST", "ENTITY_2_TEST");
            inputAlignment.add(c);
            Alignment result = matcher.match(tc.getSourceOntology(OntModel.class), tc.getTargetOntology(OntModel.class),
                    inputAlignment, null);
            assertNotNull(result.size());
            assertTrue(result.size() > 0);
            assertTrue(result.contains(c));
        } catch (Exception e){
            LOGGER.error("A match exception occurred.", e);
            fail();
        }
    }

    @Test
    void normalize(){
        assertTrue(SimpleStringMatcher.normalize("arm/leg").equals(SimpleStringMatcher.normalize("arm OR leg")));
        assertTrue(SimpleStringMatcher.normalize("arm/leg").equals(SimpleStringMatcher.normalize("the arm OR the leg")));
        assertTrue(SimpleStringMatcher.normalize("arm/leg").equals(SimpleStringMatcher.normalize("ArmLeg")));
        assertTrue(SimpleStringMatcher.normalize("arm///leg").equals(SimpleStringMatcher.normalize("ArmLeg")));
        assertTrue(SimpleStringMatcher.normalize("the/or ArmLeg").equals(SimpleStringMatcher.normalize("ArmLeg")));
        assertFalse(SimpleStringMatcher.normalize("arm/leg").equals(SimpleStringMatcher.normalize("arm")));
        assertFalse(SimpleStringMatcher.normalize("arm/leg").equals(SimpleStringMatcher.normalize("leg")));
        assertFalse(SimpleStringMatcher.normalize("Mary's toy").equals(SimpleStringMatcher.normalize("toy mary")));
    }
}