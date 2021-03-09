package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking;

import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.Metric;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A metric which computes multiple rank metrics such as the NDCG and average precision for an execution result.
 */
public class RankingMetric extends Metric<RankingResult> {


    private static final Logger LOGGER = LoggerFactory.getLogger(RankingMetric.class);

    private static final double LOG_OF_2 = Math.log(2);

    /**
     * The strategy to use in case there are multiple correspondences with the same confidence.
     */
    protected SameConfidenceRanking sameConfidenceRanking;

    /**
     * An indicator that determines whether {@link RankingResult#kOfHitsAtK} has been manually set.
     * This is important because if it has not been manually set, the value of K for all HITS@K-based KPIs will
     * determined according to the size of the reference alignment.
     */
    private boolean isKofHitsAtKmanuallySet = false;

    /**
     * The K of HITS@K. The hits are saved in {@link RankingResult#hitsAtK}
     */
    private int kOfHitsAtK;

    /**
     * Constructor
     * @param sameConfidenceRanking The strategy to use in case there are multiple correspondences with the same confidence.
     * @param kOfHitsAtK The X of HITS@X.
     */
    public RankingMetric(SameConfidenceRanking sameConfidenceRanking, int kOfHitsAtK) {
        this.sameConfidenceRanking = sameConfidenceRanking;
        this.kOfHitsAtK = kOfHitsAtK;
        this.isKofHitsAtKmanuallySet = true;
    }

    /**
     * Constructor
     * @param sameConfidenceRanking The strategy to use in case there are multiple correspondences with the same confidence.
     */
    public RankingMetric(SameConfidenceRanking sameConfidenceRanking) {
        this.sameConfidenceRanking = sameConfidenceRanking;
    }

    @Override
    protected RankingResult compute(ExecutionResult executionResult) {

        Alignment systemAlignment = executionResult.getSystemAlignment();
        if (executionResult.getTestCase() != null &&
                executionResult.getTestCase().getGoldStandardCompleteness() != GoldStandardCompleteness.COMPLETE) {
            systemAlignment = getSystemResultReducedToGoldStandardEntities(executionResult);
        }
        Alignment referenceAlignment = executionResult.getReferenceAlignment();

        // determine kOfHitsAtK
        if(this.isKofHitsAtKmanuallySet == false) {
            if (referenceAlignment != null && referenceAlignment.size() > 0) {
                this.kOfHitsAtK = referenceAlignment.size();
                LOGGER.info("Inferring K for HITS@K as size of the reference alignment: " + this.kOfHitsAtK);
            } else {
                LOGGER.warn("Could not determine the K for HITS@K as size of the reference alignment. Therefore, using 10" +
                        "as fallback X.");
                this.kOfHitsAtK = 10;
            }
        }

        List<Correspondence> correspondenceRanking = sameConfidenceRanking.sortAlignment(systemAlignment, referenceAlignment);

        if (correspondenceRanking.isEmpty()) {
            LOGGER.info("List of System result is empty. Rank metrics are zero.");
            return new RankingResult(0, 0, 0, 0, 0, 0, 0, 0, this.kOfHitsAtK);
        }

        //average precision
        List<Double> precision = new ArrayList<>();
        int truePositive = 0;

        //NDCG
        double dcg = 0;
        double idcg = computeIDCG(correspondenceRanking.size());

        // HITS@K
        double hitsAtK = 0.0;

        // Reciprocal Rank
        int firstCorrectOccurrence = 1;
        boolean firstCorrectOccurrenceAppeared = false;

        // R-Precision
        int cutOffForRprecision = referenceAlignment.size();
        int numberOfTPatCutOffForRprecision = 0;

        // actual calculation
        for (int i = 0; i < correspondenceRanking.size(); i++) {
            Correspondence correspondence = correspondenceRanking.get(i);
            if (!referenceAlignment.contains(correspondence))
                continue;
            truePositive++;

            // Reciprocal Rank
            if(firstCorrectOccurrenceAppeared == false) {
                firstCorrectOccurrence = i + 1;
                firstCorrectOccurrenceAppeared = true;
            }

            // R Precision
            if(i < cutOffForRprecision){
                numberOfTPatCutOffForRprecision++;
            }

            // average precision
            precision.add((double) truePositive / (double) (i + 1));

            // DCG/NDCG
            dcg += LOG_OF_2 / Math.log(i + 2); //because rank = i + 1;

            // HITS@K
            if(i < this.kOfHitsAtK){
                hitsAtK++;
            }
        }

        // Reciprocal Rank
        double reciprocalRank = 1.0 / (double) firstCorrectOccurrence;

        // NDCG
        double ndcg = dcg / idcg;

        // P@K
        double precisionAtK = hitsAtK / kOfHitsAtK;

        // R@K
        double recallAtK = hitsAtK / referenceAlignment.size();

        // R Precision
        double rPrecision = (double) numberOfTPatCutOffForRprecision / cutOffForRprecision;

        RankingResult result = new RankingResult();
        result.dcg = dcg;
        result.ndcg = ndcg;
        result.averagePrecision = getAverage(precision);
        result.reciprocalRank = reciprocalRank;
        result.rPrecision = rPrecision;
        result.hitsAtK = hitsAtK;
        result.precisionAtK = precisionAtK;
        result.kOfHitsAtK = this.kOfHitsAtK;
        result.recallAtK = recallAtK;
        return result;
    }


    protected double computeIDCG(int n) {
        double idcg = 0;
        for (int i = 0; i < n; i++) {
            idcg += LOG_OF_2 / Math.log(i + 2);
        }
        return idcg;
    }


    /**
     * Return the system alignment but only with correspondences where the source or the target appear also in the
     * gold standard.
     *
     * @param executionResult execution result to use
     * @return reduced system alignment
     */
    protected Alignment getSystemResultReducedToGoldStandardEntities(ExecutionResult executionResult) {
        Alignment systemAlignment = executionResult.getSystemAlignment();
        Alignment referenceAlignment = executionResult.getReferenceAlignment();

        Set<String> referenceSources = makeSet(referenceAlignment.getDistinctSources());
        Set<String> referenceTargets = makeSet(referenceAlignment.getDistinctTargets());

        Alignment reducedSystemAlignment = new Alignment(systemAlignment);
        for (Correspondence c : systemAlignment) {
            if (referenceSources.contains(c.getEntityOne()) == false && referenceTargets.contains(c.getEntityTwo())) {
                reducedSystemAlignment.remove(c);
            }
        }
        return reducedSystemAlignment;
    }


    protected static <T> Set<T> makeSet(Iterable<T> iterable) {
        Set<T> set = new HashSet<>();
        for (T element : iterable) {
            set.add(element);
        }
        return set;
    }

    protected double getAverage(List<Double> list) {
        double sum = 0.0;
        for (Double d : list)
            sum += d;
        return sum / (double) list.size();
    }

}
