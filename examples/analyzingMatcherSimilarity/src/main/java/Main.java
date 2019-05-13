import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.Executor;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.resultsSimilarity.MatcherSimilarity;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.resultsSimilarity.MatcherSimilarityMetric;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.visualization.MatcherSimilarityLatexPlotWriter;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.visualization.MatcherSimilarityLatexHeatMapWriter;

import java.io.PrintWriter;

/**
 * This code was used to analyze the Anatomy and Conference Track using MELT.
 */
public class Main {

    public static void main(String[] args) {

        // download the results folders and set the path here:
        final String pathToAnatomyResultFiles = "";
        final String pathToConferenceResultFiles = "";

        //--------------------------------------------------------
        // Heat Map Evaluation Anatomy
        //--------------------------------------------------------
        ExecutionResultSet executionResultSetAnatomy = Executor.loadFromAnatomyResultsFolder(pathToAnatomyResultFiles);
        MatcherSimilarityMetric metric = new MatcherSimilarityMetric();
        MatcherSimilarity similarity = metric.get(executionResultSetAnatomy, TrackRepository.Anatomy.Default.getTestCases().get(0));
        PrintWriter writer = new PrintWriter(System.out);
        MatcherSimilarityLatexHeatMapWriter.write(similarity, writer);
        System.out.println("\nMedian: " + similarity.getMedianSimiarity());
        System.out.println("Median (no self reference): " + similarity.getMedianSimilariyWithoutSelfSimilarity() + "\n\n\n");

        //--------------------------------------------------------
        // MAD Calculation Anatomy
        //--------------------------------------------------------
        MatcherSimilarityLatexPlotWriter plotWriter = new MatcherSimilarityLatexPlotWriter();
        plotWriter.write(similarity, writer);

        //--------------------------------------------------------
        // Heat Map Evaluation Conference
        //--------------------------------------------------------
        ExecutionResultSet executionResultSetConference = Executor.loadFromConferenceResultsFolder(pathToConferenceResultFiles);
        metric = new MatcherSimilarityMetric();
        similarity = metric.get(MatcherSimilarityMetric.CalculationMode.MICRO, executionResultSetConference, TrackRepository.Conference.V1);
        MatcherSimilarityLatexHeatMapWriter.write(similarity, writer);
        System.out.println("\n\n\nMedian: " + similarity.getMedianSimiarity());
        System.out.println("Median (no self reference): " + similarity.getMedianSimilariyWithoutSelfSimilarity() + "\n\n\n");

        //--------------------------------------------------------
        // MAD Calculation Conference
        //--------------------------------------------------------
        plotWriter.write(similarity, writer);

        //--------------------------------------------------------
        // Correspondence CSV Evaluation
        //--------------------------------------------------------
        ExecutionResultSet allExecutionResults = new ExecutionResultSet();
        allExecutionResults.addAll(executionResultSetAnatomy);
        allExecutionResults.addAll(executionResultSetConference);
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(allExecutionResults);
        evaluatorCSV.write();
    }
}
