package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet.WordNetKnowledgeSource;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BackgroundMatcherStandAloneTest {


    @Test
    void match() {
        TestCase tc1 = TrackRepository.Conference.V1.getFirstTestCase();
        try {
            BackgroundMatcherStandAlone backgroundMatcher = new BackgroundMatcherStandAlone(new WordNetKnowledgeSource(),
                    ImplementedBackgroundMatchingStrategies.SYNONYMY, 0.0);

            SimpleStringMatcher stringMatcher = new SimpleStringMatcher();

            Alignment bkResult = backgroundMatcher.match(tc1.getSourceOntology(OntModel.class),
                    tc1.getTargetOntology(OntModel.class), null,
                    null);
            Alignment stringResult = stringMatcher.match(tc1.getSourceOntology(OntModel.class),
                    tc1.getTargetOntology(OntModel.class), null,
                    null);
            assertNotNull(bkResult);
            assertTrue(bkResult.size() > 1);
            assertNotNull(stringResult);
            assertTrue(stringResult.size() > 1);
            assertTrue(bkResult.size() > stringResult.size());
        } catch (Exception e){
            fail(e);
        }
    }
}