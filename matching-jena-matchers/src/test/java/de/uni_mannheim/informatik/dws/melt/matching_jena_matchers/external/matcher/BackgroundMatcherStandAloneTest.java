package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet.WordNetKnowledgeSource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet.WordNetLinker;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class BackgroundMatcherStandAloneTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundMatcherStandAloneTest.class);

    @Test
    void match() {
        TestCase tc1 = TrackRepository.Conference.V1.getTestCase(0);
        LOGGER.info("Test Case Name: " + tc1.getName());
        LOGGER.info("Source: " + tc1.getSource().toString());
        LOGGER.info("Target: " + tc1.getTarget().toString());
        try {
            BackgroundMatcherStandAlone backgroundMatcher = new BackgroundMatcherStandAlone(new WordNetKnowledgeSource(),
                    ImplementedBackgroundMatchingStrategies.SYNONYMY, 0.0);
            assertNotNull(backgroundMatcher.getLinker());
            assertTrue(backgroundMatcher.getLinker() instanceof WordNetLinker);

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
            assertTrue(bkResult.size() > stringResult.size(), "BK result size: " + bkResult.size() + "\n" +
                    "String result size: " + stringResult.size() + "\n");
        } catch (Exception e){
            fail(e);
        }
    }

    @Test
    void getSetName(){
        BackgroundMatcherStandAlone backgroundMatcher = new BackgroundMatcherStandAlone(new WordNetKnowledgeSource(),
                ImplementedBackgroundMatchingStrategies.SYNONYMY, 0.0);

        String matcherName1 = "My Super Matcher";
        backgroundMatcher.setName(matcherName1);
        assertEquals(matcherName1, backgroundMatcher.getName());

        String matcherName2 = "My Super Matcher 2";
        backgroundMatcher.setName(matcherName2);
        assertEquals(matcherName2, backgroundMatcher.getName());
    }
}