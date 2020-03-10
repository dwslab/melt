package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.visualization.dashboard;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class DashboardBuilderTest {

    @Test
    void writeResultsToDirectory() {

        // test conference loader
        String conferencePath = this.getClass().getClassLoader().getResource("2019_conference_shortened").getPath();
        assertTrue(conferencePath != null, "Missing test files.");
        ExecutionResultSet conferenceResults = Executor.loadFromConferenceResultsFolder(conferencePath);
        assertTrue(conferenceResults != null, "The results were not properly loaded.");

        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(conferenceResults);
        DashboardBuilder builder = new DashboardBuilder(evaluatorCSV);
        builder.writeToFile("./dashboard.html");
        File resultFile = new File("./dashboard.html");
        assertTrue(resultFile.exists(), "No file was written.");

        // clean up
        resultFile.delete();
    }
}