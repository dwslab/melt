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
public class EvaluatorRank extends Evaluator{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorRank.class);
    
    private boolean partialGoldStandard;

    /**
     * Constructor which needs the results as well as the info if the gold standard for al testcases is partial or not.
     * @param results the execution result set
     * @param partialGoldStandard true if testcase in this executionresultset is a partial gold standard
     */
    public EvaluatorRank(ExecutionResultSet results, boolean partialGoldStandard) {
        super(results);
        this.partialGoldStandard = partialGoldStandard;
    }

    /**
     * Initialize the evaluator with a full gold standard (not partial).
     * @param results the execution result set
     */
    public EvaluatorRank(ExecutionResultSet results) {
        this(results, false);
    }

    @Override
    public void writeResultsToDirectory(File baseDirectory) {
        
        RankingMetric random = new RankingMetric(partialGoldStandard, SameConfidenceRanking.RANDOM);
        RankingMetric alphabetically = new RankingMetric(partialGoldStandard, SameConfidenceRanking.ALPHABETICALLY);
        RankingMetric top = new RankingMetric(partialGoldStandard, SameConfidenceRanking.TOP);
        
        try {
            if(!baseDirectory.exists()){
                baseDirectory.mkdirs();
            } else if (baseDirectory.isFile()) {
                LOGGER.error("The baseDirectory needs to be a directory, not a file. ABORTING writing process.");
                return;
            }

            File fileToBeWritten = new File(baseDirectory, "resultsRanking.csv");
            CSVPrinter printer = new CSVPrinter(new FileWriter(fileToBeWritten, false), CSVFormat.DEFAULT);
            printer.printRecord(Arrays.asList("Track", "Test Case", "Matcher", "Type", 
                    "DCG-random", "NDCG-random", "Average Precision-random", 
                    "DCG-alphabetically", "NDCG-alphabetically", "Average Precision-alphabetically", 
                    "DCG-top", "NDCG-top", "Average Precision-top"));
            for (ExecutionResult er : results.getUnrefinedResults()) {
                RankingResult randomResult = random.get(er);
                RankingResult alphabeticallyResult = alphabetically.get(er);
                RankingResult topResult = top.get(er);
                
                printer.printRecord(Arrays.asList(er.getTestCase().getTrack().getName(), er.getTestCase().getName(), er.getMatcherName(), er.getRefinements(),
                        randomResult.getDcg(), randomResult.getNdcg(),randomResult.getAveragePrecision(),
                        alphabeticallyResult.getDcg(), alphabeticallyResult.getNdcg(),alphabeticallyResult.getAveragePrecision(),
                        topResult.getDcg(), topResult.getNdcg(),topResult.getAveragePrecision()
                        
                ));
            }
            printer.flush();
            printer.close();
        } catch (IOException ioe){
            LOGGER.error("Problem with results writer.", ioe);
        }
    }
}
