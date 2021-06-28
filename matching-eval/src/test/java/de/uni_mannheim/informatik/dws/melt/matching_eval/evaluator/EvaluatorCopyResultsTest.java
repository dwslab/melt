package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.SimpleStringMatcher;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorCopyResultsTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorCopyResultsTest.class);

    @Test
    void writeResultsToDirectory() {
        File result = new File("myCopyResults");
        ExecutionResultSet ers = Executor.run(TrackRepository.Conference.V1, new SimpleStringMatcher());
        EvaluatorCopyResults evaluatorCopyResults = new EvaluatorCopyResults(ers);
        evaluatorCopyResults.writeResultsToDirectory(result);
        assertTrue(result.exists());
    }

    @AfterAll
    static void cleanUp(){
        try {
            FileUtils.deleteDirectory(new File("myCopyResults"));
        } catch (IOException ioe){
            LOGGER.warn("Could not delete a directory.", ioe);
        }
    }

}