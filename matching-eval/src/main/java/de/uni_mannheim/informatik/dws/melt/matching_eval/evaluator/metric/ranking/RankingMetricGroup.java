package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.Metric;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;


public class RankingMetricGroup extends Metric<RankingResult> {


    /**
     * The strategy to resolve conflicts in case correspondences carry the same confidence.
     */
    private SameConfidenceRanking sameConfidenceRanking;

    /**
     * K of HITS@K / P@K.
     */
    private int kOfHitsAtK;

    /**
     * Constructor
     * @param sameConfidenceRanking The ranking to be chosen in case correspondences carry the same confidence.
     * @param kOfHitsAtK K of HITS@K.
     */
    public RankingMetricGroup(SameConfidenceRanking sameConfidenceRanking, int kOfHitsAtK){
        this.sameConfidenceRanking = sameConfidenceRanking;
        this.kOfHitsAtK = kOfHitsAtK;
    }

    @Override
    protected RankingResult compute(ExecutionResult executionResult) {

        Alignment systemAlignment = executionResult.getSystemAlignment();
        Alignment referenceAlignment = executionResult.getReferenceAlignment();
        RankingResult overallResult = new RankingResult();
        overallResult.kOfHitsAtK = this.kOfHitsAtK;
        int numberOfDistinctSources = 0;

        for (String sourceUri : referenceAlignment.getDistinctSources()) {
            // Build ExecutionResult subset for rank evaluation
            Alignment referenceAlignmentSubset = new Alignment(referenceAlignment.getCorrespondencesSource(sourceUri));
            Alignment systemAlignmentSubset = new Alignment(systemAlignment.getCorrespondencesSource(sourceUri));

            // We also create a subset test case so that various levels of completeness can also be evaluated
            // We copy the testcase so that we can set a new name for better differentiation when debugging.
            TestCase executionResultTestCase = executionResult.getTestCase();
            TestCase localSubsetTestCase = new TestCase(executionResultTestCase.getName() + "_SUBSET",
                    executionResultTestCase.getSource(),
                    executionResultTestCase.getTarget(),
                    executionResultTestCase.getReference(),
                    executionResultTestCase.getTrack(),
                    executionResultTestCase.getInputAlignment(),
                    executionResultTestCase.getGoldStandardCompleteness(),
                    executionResultTestCase.getParameters()
            );

            ExecutionResult executionResultSubset = new ExecutionResult(localSubsetTestCase, executionResult.getMatcherName(),
                    systemAlignmentSubset, referenceAlignmentSubset);

            // let's add the scores to our overall result
            RankingMetric singleMetric = new RankingMetric(this.sameConfidenceRanking, this.kOfHitsAtK);
            RankingResult singleResult = singleMetric.get(executionResultSubset);
            overallResult.addScores(singleResult);
            numberOfDistinctSources++;
        }

        // lets normalize the overall result by the number of distinct sources
        overallResult.normalizeAllScores(numberOfDistinctSources);
        return overallResult;
    }
}
