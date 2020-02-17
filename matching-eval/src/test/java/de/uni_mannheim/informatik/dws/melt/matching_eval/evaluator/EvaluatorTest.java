package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.MAC;

class EvaluatorTest {

    /**
     * Development remark: Note that a network connection is required and the SEALS repository must be up and running.
     */
    @Test
    @EnabledOnOs({ MAC })
    void getResultsDirectoryTrackMatcher() {
        try {
            ExecutionResult result = new ExecutionResult(TrackRepository.Conference.V1.getTestCases().get(0), "myMatcher", null, null);
            ExecutionResultSet results = new ExecutionResultSet();
            results.add(result);
            EvaluatorCSV evaluatorCSV = new EvaluatorCSV(results);
            File solution = new File("./results/conference_conference-v1/aggregated");
            assertEquals(evaluatorCSV.getResultsDirectoryTrackMatcher(new File("./results"), TrackRepository.Conference.V1).getCanonicalPath(), solution.getCanonicalPath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

}