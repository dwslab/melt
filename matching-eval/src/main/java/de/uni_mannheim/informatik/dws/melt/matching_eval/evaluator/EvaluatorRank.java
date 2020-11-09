package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking.RankingMetric;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking.RankingResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking.SameConfidenceRanking;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
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

        // TODO

        RankingMetric random = new RankingMetric(SameConfidenceRanking.RANDOM);
        RankingMetric alphabetically = new RankingMetric(SameConfidenceRanking.ALPHABETICALLY);
        RankingMetric top = new RankingMetric(SameConfidenceRanking.TOP);
        
        try {
            if(!baseDirectory.exists()){
                baseDirectory.mkdirs();
            } else if (baseDirectory.isFile()) {
                LOGGER.error("The base directory needs to be a directory, not a file. ABORTING writing process.");
                return;
            }

            File fileToBeWritten = new File(baseDirectory, "resultsRanking.csv");
            CSVPrinter printer = new CSVPrinter(new FileWriter(fileToBeWritten, false), CSVFormat.DEFAULT);
            printer.printRecord(Arrays.asList("Track", "Test Case", "Matcher", "Type", 
                    "DCG-random", "NDCG-random", "Average Precision-random", 
                    "DCG-alphabetically", "NDCG-alphabetically", "Average Precision-alphabetically", 
                    "DCG-top", "NDCG-top", "Average Precision-top"));
            for (ExecutionResult executionResult : results.getUnrefinedResults()) {
                RankingResult randomResult = random.get(executionResult);
                RankingResult alphabeticallyResult = alphabetically.get(executionResult);
                RankingResult topResult = top.get(executionResult);
                
                printer.printRecord(Arrays.asList(executionResult.getTestCase().getTrack().getName(), executionResult.getTestCase().getName(),
                        executionResult.getMatcherName(), executionResult.getRefinements(),
                        randomResult.getDcg(), randomResult.getNdcg(),randomResult.getAveragePrecision(),
                        alphabeticallyResult.getDcg(), alphabeticallyResult.getNdcg(),alphabeticallyResult.getAveragePrecision(),
                        topResult.getDcg(), topResult.getNdcg(), topResult.getAveragePrecision()
                        
                ));
            }
            printer.flush();
            printer.close();
        } catch (IOException ioe){
            LOGGER.error("Problem with results writer.", ioe);
        }
    }
}
