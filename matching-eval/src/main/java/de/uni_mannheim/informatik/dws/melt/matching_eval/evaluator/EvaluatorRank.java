package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking.RankingMetric;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking.RankingMetricGroup;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking.RankingResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking.SameConfidenceRanking;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.uni_mannheim.informatik.dws.melt.matching_eval.refinement.Refiner;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rank evaluator which writes a file resultsRanking.csv.
 * It contains the rankings for each executed testcase.
 * Currently it contains DCG, nDCG, MAP in three variants:
 * random, alphabetically, top (this only comes into play when there are correspondences with the same confidence).
 */
public class EvaluatorRank extends Evaluator {


    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorRank.class);

    /**
     * The strategies to be evaluated for cases in which two correspondences carry the same confidence.
     */
    private SameConfidenceRanking[] sameConfidenceRankingList;

    /**
     * The CSV file name which will be written to the results directory.
     */
    public static final String RESULT_FILE_NAME = "resultsRanking.csv";

    /**
     * Refinement operations to be applied.
     */
    private List<Refiner[]> refinerList;

    /**
     * Constructor
     * @param results The results to be evaluated.
     * @param sameConfidenceRankingList The confidence ranking strategies to be applied in case of multiple correspondences
     *                                  with the same confidence. If multiple strategies are given, all will be evaluated
     *                                  and appear in the resulting CSV file (for comparison).
     *
     */
    public EvaluatorRank(ExecutionResultSet results, SameConfidenceRanking... sameConfidenceRankingList) {
        super(results);
        this.sameConfidenceRankingList = sameConfidenceRankingList;
    }

    /**
     * Convenience Constructor. Clashes with multiple correspondences carrying the same score will be resolved by
     * alphabetical ordering.
     * @param results The execution result set.
     */
    public EvaluatorRank(ExecutionResultSet results) {
        this(results, SameConfidenceRanking.ALPHABETICALLY);
    }

    @Override
    public void writeResultsToDirectory(File baseDirectory) {

        // initialize metrics
        RankingMetric[] metrics = new RankingMetric[sameConfidenceRankingList.length];
        for(int i = 0; i < metrics.length; i++){
            metrics[i] = new RankingMetric(sameConfidenceRankingList[i]);
        }
        
        try {
            if(!baseDirectory.exists()){
                baseDirectory.mkdirs();
            } else if (baseDirectory.isFile()) {
                LOGGER.error("The base directory needs to be a directory, not a file. ABORTING writing process.");
                return;
            }

            File fileToBeWritten = new File(baseDirectory, RESULT_FILE_NAME);
            CSVPrinter printer = new CSVPrinter(new FileWriter(fileToBeWritten, false), CSVFormat.DEFAULT);

            // print the header of the CSV file
            ArrayList<String> columnHeaders = new ArrayList<>();
            columnHeaders.addAll(Arrays.asList("Track", "Test Case", "Matcher"));
            for(SameConfidenceRanking confidenceRanking : sameConfidenceRankingList){
                columnHeaders.add(confidenceRanking.toString() + " - DCG");
                columnHeaders.add(confidenceRanking.toString() + " - NDCG");
                columnHeaders.add(confidenceRanking.toString() + " - Average Precision");
                columnHeaders.add(confidenceRanking.toString() + " - Reciprocal Rank");
                columnHeaders.add(confidenceRanking.toString() + " - rPrecision");
                columnHeaders.add(confidenceRanking.toString() + " - HITS@K");
                columnHeaders.add(confidenceRanking.toString() + " - Precision@K");
                columnHeaders.add(confidenceRanking.toString() + " - Recall@K");
                columnHeaders.add(confidenceRanking.toString() + " - F1@K");
                columnHeaders.add(confidenceRanking.toString() + " - K");
            }
            printer.printRecord(columnHeaders);

            // now calculate the performance numbers
            for (ExecutionResult executionResult : results.getUnrefinedResults()) {
                 runMetricAndPrintToFile(metrics, executionResult, "-", printer);
                if (refinerList != null && refinerList.size() > 0) {
                    for (Refiner[] refiners : refinerList) {
                        ExecutionResult refinedResult = results.get(executionResult, refiners);
                        String refinerString = Arrays.stream(refiners).map(x -> x.toString()).collect(Collectors.joining(", "));
                        runMetricAndPrintToFile(metrics, refinedResult, refinerString, printer);
                    }
                }
            }
            printer.flush();
            printer.close();
        } catch (IOException ioe){
            LOGGER.error("Problem with results writer.", ioe);
        }
    }

    /**
     * Runs the metric on the given result and prints a line to the CSV file.
     *
     * @param metrics         Metrics for evaluation.
     * @param executionResult Refined result for evaluation.
     * @param refinerString   String identifying the refinement operations that have been performed.
     * @param printer         Printer to be used for printing.
     * @throws IOException IOException may be thrown while printing.
     */
    private void runMetricAndPrintToFile(RankingMetric[] metrics, ExecutionResult executionResult, String refinerString, CSVPrinter printer) throws IOException {
        ArrayList<String> resultRow = new ArrayList<>();
        resultRow.addAll(Arrays.asList(
                executionResult.getTrack().getName(),
                executionResult.getTestCase().getName(),
                executionResult.getMatcherName(),
                refinerString));
        for (RankingMetric metric : metrics) {
            RankingResult result = metric.get(executionResult);
            resultRow.add("" + result.getDcg());
            resultRow.add("" + result.getNdcg());
            resultRow.add("" + result.getAveragePrecision());
            resultRow.add("" + result.getReciprocalRank());
            resultRow.add("" + result.getrPrecision());
            resultRow.add("" + result.getHitsAtK());
            resultRow.add("" + result.getPrecisionAtK());
            resultRow.add("" + result.getRecallAtK());
            resultRow.add("" + result.getF1AtK());
            resultRow.add("" + result.getkOfHitsAtK());
        }
        printer.printRecord(resultRow);
    }

    public List<Refiner[]> getRefinerList() {
        return refinerList;
    }

    public void setRefinerList(List<Refiner[]> refinerList) {
        this.refinerList = refinerList;
    }
}
