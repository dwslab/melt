package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.Executor;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.MAC;

class EvaluatorCSVTest {

    private static Logger LOGGER = LoggerFactory.getLogger(EvaluatorCSVTest.class);

    /**
     * This tests uses the 2018 alignment files for DOME and ALOD2Vec and evaluates them on the OAEI Anatomy data set
     * using EvaluatorCSV. This test makes sure that something is written and that setting of the base directory works.
     */
    @Test
    @EnabledOnOs({ MAC })
    void testEvaluator(){
        ExecutionResultSet resultSet = Executor.loadFromFolder("./src/test/resources/externalAlignmentForEvaluation", TrackRepository.Anatomy.Default.getTestCases().get(0));
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(resultSet);
        File baseDirectory = new File("./testBaseDirectory");
        baseDirectory.mkdir();
        evaluatorCSV.writeToDirectory(baseDirectory);
        assertTrue(baseDirectory.listFiles().length > 0);
        assertTrue(new File("./testBaseDirectory/trackPerformanceCube.csv").exists());
        assertTrue(new File("./testBaseDirectory/testCasePerformanceCube.csv").exists());
        try {
            FileUtils.deleteDirectory(baseDirectory);
        } catch (IOException ioe){
            LOGGER.error("Could not clean up after test. Test directory 'testBaseDirectory' still exists on disk.", ioe);
        }
    }

}