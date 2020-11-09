package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;

import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking.SameConfidenceRanking;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * An evaluator that calculates rank metrics on an per-element basis for each element of a specified source ontology.
 * An example is provided below. If you look for a plain general implementation of NDCG and DCG (not grouped per source node), you have to
 * use {@link EvaluatorRank}.
 * Example:
 * <p><br/></p>
 * <pre>
 * O1:a----------O2:a (FP)
 *        |
 *        ------O2:b (TP)
 *
 * O1:b----------O2:b (FP)
 *       |
 *       ------O2:c (TP)
 *       |
 *       ------O2:d (TP)
 * </pre>
 * <p>
 * <pre>
 * Reference Alignment:
 * (O1:a, O2:b, =)
 * (O1:b, O2:c, =)
 * (O1:b, O2:d, =)
 * </pre>
 * <p>
 * <pre>
 * Given a HITS@2 configuration with source=O1 (examples of many KPIs calculated):
 * P@2 = (1/2 + 1/2)/2 = 1/2
 * HITS@2 = 2
 * F1@2 = (2 * P@2 * R@2)/(P@2 + R@2)
 * </pre>
 */
public class EvaluatorGroupRank extends Evaluator {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorGroupRank.class);

    /**
     * The strategies to be evaluated for cases in which two correspondences carry the same confidence.
     */
    private SameConfidenceRanking[] sameConfidenceRankingList;

    /**
     * Constructor
     *
     * @param results                   The results to be evaluated.
     * @param sameConfidenceRankingList The confidence ranking strategies to be applied in case of multiple correspondences
     *                                  with the same confidence. If multiple strategies are given, all will be evaluated
     *                                  and appear in the resulting CSV file (for comparison).
     */
    public EvaluatorGroupRank(ExecutionResultSet results, SameConfidenceRanking... sameConfidenceRankingList) {
        super(results);
        this.sameConfidenceRankingList = sameConfidenceRankingList;
    }

    /**
     * Convenience Constructor. Clashes with multiple correspondences carrying the same score will be resolved by
     * alphabetical ordering.
     *
     * @param results The results of the matching process.
     */
    public EvaluatorGroupRank(ExecutionResultSet results) {
        this(results, SameConfidenceRanking.ALPHABETICALLY);
    }

    @Override
    protected void writeResultsToDirectory(File baseDirectory) {

        try {
            if (!baseDirectory.exists()) {
                baseDirectory.mkdirs();
            } else if (baseDirectory.isFile()) {
                LOGGER.error("The base directory needs to be a directory, not a file. ABORTING writing process.");
                return;
            }

            File fileToBeWritten = new File(baseDirectory, "resultsRanking.csv");
            CSVPrinter printer = new CSVPrinter(new FileWriter(fileToBeWritten, false), CSVFormat.DEFAULT);

            for (ExecutionResult executionResult : results.getUnrefinedResults()) {
                //GroupRankingMetric metric = new GroupRankingMetric();
                //RankingResult result = metric.get(executionResult);

                // TODO some printing

            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while writing the results.", e);
        }

    }
}
