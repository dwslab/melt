package de.uni_mannheim.informatik.dws.melt.matching_owlapi_matchers;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_owlapi.OntologyCacheOwlApi;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class AlcomoFilterTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(AlcomoFilterTest.class);

    @Test
    void serializeAlignmentToTemporaryFile() {
        try {
            assertNotNull(AlcomoFilter.serializeAlignmentToTemporaryFile(new Alignment()));
            assertNotNull(AlcomoFilter.serializeAlignmentToTemporaryFile(null));
        } catch (AlcomoException e) {
            LOGGER.error("AlcomoException occurred.", e);
            fail("Exception must not be thrown.");
        }
    }

    @Test
    void serializeOntologyToTemporaryFile() {
        TestCase tc = TrackRepository.Conference.V1.getFirstTestCase();
        try {
            String o1path = AlcomoFilter.serializeOntologyToTemporaryFile(tc.getSourceOntology(OWLOntology.class));
            String o2path = AlcomoFilter.serializeOntologyToTemporaryFile(tc.getTargetOntology(OWLOntology.class));
            assertNotNull(o1path);
            assertNotNull(o2path);
            assertNotEquals(o1path, o2path);
        } catch (AlcomoException e) {
            LOGGER.error("AlcomoException occurred.", e);
            fail("Exception must not be thrown.");
        }
    }

}