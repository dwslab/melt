package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.MAC;

class EvaluatorBasicTest {

    private static Logger LOGGER = LoggerFactory.getLogger(EvaluatorCSVTest.class);

    @EnabledOnOs({ MAC })
    @Test
    void writeToDirectory() {
        ExecutionResultSet resultSet = Executor.loadFromFolder("./src/test/resources/externalAlignmentForEvaluation", TrackRepository.Anatomy.Default.getTestCases().get(0));
        EvaluatorBasic evaluatorUnderTest = new EvaluatorBasic(resultSet);
        File baseDirectory = new File("./testBaseDirectory");
        baseDirectory.mkdir();
        evaluatorUnderTest.writeToDirectory(baseDirectory);
        assertTrue(baseDirectory.listFiles().length > 0);
        assertTrue(new File("./testBaseDirectory/" + evaluatorUnderTest.getResultFileName()).exists());
        try {
            FileUtils.deleteDirectory(baseDirectory);
        } catch (IOException ioe){
            LOGGER.error("Could not clean up after test. Test directory 'testBaseDirectory' still exists on disk.", ioe);
        }
    }

    @EnabledOnOs({ MAC })
    @Test
    void writeToDirectoryConvenience() {
        ExecutionResultSet resultSet = Executor.loadFromFolder("./src/test/resources/externalAlignmentForEvaluation", TrackRepository.Anatomy.Default.getTestCases().get(0));
        EvaluatorBasic evaluatorUnderTest = new EvaluatorBasic(resultSet);
        File baseDirectory = new File("./testBaseDirectory");
        evaluatorUnderTest.writeToDirectory("./testBaseDirectory");
        assertTrue(baseDirectory.listFiles().length > 0);
        assertTrue(new File("./testBaseDirectory/" + evaluatorUnderTest.getResultFileName()).exists());
        try {
            FileUtils.deleteDirectory(baseDirectory);
        } catch (IOException ioe){
            LOGGER.error("Could not clean up after test. Test directory 'testBaseDirectory' still exists on disk.", ioe);
        }
    }

}