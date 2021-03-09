package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.resultsSimilarity;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.refinement.Refiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This metric allows to compare system results similarity by calculating the jaccard overlap between
 * alignment results.
 *
 * @author Jan Portisch
 */
public class MatcherSimilarityMetric {


    /**
     * Default Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(MatcherSimilarityMetric.class);

    /**
     * Obtain the matcher similarity for the given {@link ExecutionResultSet}.
     *
     * @param resultSet The result set for which the matcher similarity shall be calculated.
     * @param testCase  The test case on whose basis the evaluation shall be performed.
     * @param refiners The refiners for the comparison operation.
     * @return The similarity between matchers.
     */
    public MatcherSimilarity get(ExecutionResultSet resultSet, TestCase testCase, Refiner... refiners) {
        if(refiners == null) refiners = new Refiner[0];
        MatcherSimilarity result = new MatcherSimilarity();
        for (String outerMatcherName : resultSet.getDistinctMatchers(testCase)) {
            ExecutionResult outerResult = resultSet.get(testCase, outerMatcherName, refiners);
            for (String innerMatcherName : resultSet.getDistinctMatchers(testCase)) {
                ExecutionResult innerResult = resultSet.get(testCase, innerMatcherName, refiners);
                if (outerResult != null && innerResult != null) {
                    result.add(outerResult, innerResult, computeSimilarity(outerResult, innerResult));
                }
            }
        }
        return result;
    }


    /**
     * Obtain the aggregated matcher similarity for the given {@link ExecutionResultSet}.
     * Results will be averaged in a micro-average fashion.
     *
     * @param mode The mode for the calculation (i.e., micro or macro).
     * @param resultSet The result set to be used.
     * @param track The track for which the matcher similarity shall be aggregated.
     * @param refiners Refiners that apply.
     * @return The similarity between matchers.
     */
    public MatcherSimilarity get(CalculationMode mode, ExecutionResultSet resultSet, Track track, Refiner... refiners) {
        switch (mode) {
            case MICRO:
                return microAverageMatcherSimilarity(resultSet, track, refiners);
            case MACRO:
                return macroAverageMatcherSimilarity(resultSet, track, refiners);
        }
        return null;
    }


    /**
     * Obtain the aggregated matcher similarity for the given {@link ExecutionResultSet}.
     * Results will be averaged in a micro-average fashion.
     *
     * @param resultSet The result set to be used.
     * @param track    The track for which the matcher similarity shall be aggregated.
     * @param refiners Refiners that apply.
     * @return The similarity between matchers.
     */
    private MatcherSimilarity macroAverageMatcherSimilarity(ExecutionResultSet resultSet, Track track, Refiner... refiners) {
        MatcherSimilarity result = new MatcherSimilarity();

        for (String outerMatcherName : resultSet.getDistinctMatchers(track)) {
            for (String innerMatcherName : resultSet.getDistinctMatchers(track)) {

                ExecutionResult outerResultDummy = null;
                ExecutionResult innerResultDummy = null;
                double similarity = 0.0;
                int comparisonCount = 0;

                // loop over individual test cases
                loopOverTestCases:
                for (TestCase testCase : track.getTestCases()) {
                    ExecutionResult outerExecutionResult = resultSet.get(testCase, outerMatcherName, refiners);
                    ExecutionResult innerExecutionResult = resultSet.get(testCase, innerMatcherName, refiners);

                    // making sure there are results for the test case
                    if (outerExecutionResult == null && innerExecutionResult == null) continue loopOverTestCases;
                    if (outerExecutionResult == null && innerExecutionResult != null) {
                        LOGGER.info(outerMatcherName + " was run on test case " + testCase.getName() + " but " +
                                innerMatcherName + " was not. This test case will be excluded from the similarity calculation.");
                        continue loopOverTestCases;
                    }
                    if (outerExecutionResult != null && innerExecutionResult == null) {
                        LOGGER.info(outerMatcherName + " was not run on test case " + testCase.getName() + " but " +
                                innerMatcherName + " was. This test case will be excluded from the similarity calculation.");
                        continue loopOverTestCases;
                    }

                    // set dummy data structures if they have not been yet set
                    if (outerResultDummy == null) outerResultDummy = outerExecutionResult;
                    if (innerResultDummy == null) innerResultDummy = innerExecutionResult;

                    similarity = similarity + computeSimilarity(outerExecutionResult.getSystemAlignment(), innerExecutionResult.getSystemAlignment());
                    comparisonCount++;
                }
                result.add(outerResultDummy, innerResultDummy, (similarity / comparisonCount));
            }
        }
        return result;
    }


    /**
     * Obtain the aggregated matcher similarity for the given {@link ExecutionResultSet}.
     * Results will be averaged in a micro-average fashion.
     *
     * @param resultSet The result set to be used.
     * @param track The track for which the matcher similarity shall be aggregated.
     * @param refiners Refiners that apply.
     * @return The similarity between matchers.
     */
    private MatcherSimilarity microAverageMatcherSimilarity(ExecutionResultSet resultSet, Track track, Refiner... refiners) {
        MatcherSimilarity result = new MatcherSimilarity();

        for (String outerMatcherName : resultSet.getDistinctMatchers(track)) {
            for (String innerMatcherName : resultSet.getDistinctMatchers(track)) {

                Alignment outerAlignment = new Alignment();
                Alignment innerAlignment = new Alignment();
                ExecutionResult outerResultDummy = null;
                ExecutionResult innerResultDummy = null;

                // loop over individual test cases
                loopOverTestCases:
                for (TestCase testCase : track.getTestCases()) {
                    ExecutionResult outerExecutionResult = resultSet.get(testCase, outerMatcherName, refiners);
                    ExecutionResult innerExecutionResult = resultSet.get(testCase, innerMatcherName, refiners);

                    // making sure there are results for the test case
                    if (outerExecutionResult == null && innerExecutionResult == null) continue loopOverTestCases;
                    if (outerExecutionResult == null && innerExecutionResult != null) {
                        LOGGER.info(outerMatcherName + " was run on test case " + testCase.getName() + " but " +
                                innerMatcherName + " was not. This test case will be excluded from the similarity calculation.");
                        continue loopOverTestCases;
                    }
                    if (outerExecutionResult != null && innerExecutionResult == null) {
                        LOGGER.info(outerMatcherName + " was not run on test case " + testCase.getName() + " but " +
                                innerMatcherName + " was. This test case will be excluded from the similarity calculation.");
                        continue loopOverTestCases;
                    }

                    // set dummy data structures if they have not been yet set
                    if (outerResultDummy == null) outerResultDummy = outerExecutionResult;
                    if (innerResultDummy == null) innerResultDummy = innerExecutionResult;

                    outerAlignment.addAll(outerExecutionResult.getSystemAlignment());
                    innerAlignment.addAll(innerExecutionResult.getSystemAlignment());
                }
                result.add(outerResultDummy, innerResultDummy, computeSimilarity(outerAlignment, innerAlignment));
            } // inner
        } // outer
        return result;
    }

    /**
     * Computes the Jaccard overlap between two system results.
     *
     * @param executionResult_1 Result 1.
     * @param executionResult_2 Result 2.
     * @return Similarity as double.
     */
    public static double computeSimilarity(ExecutionResult executionResult_1, ExecutionResult executionResult_2) {
        if (executionResult_1 == null || executionResult_2 == null) return 0;
        return computeSimilarity(executionResult_1.getSystemAlignment(), executionResult_2.getSystemAlignment());
    }


    /**
     * Computes the Jaccard overlap between two alignments.
     *
     * @param alignment_1 alignment 1.
     * @param alignment_2 alignment 2.
     * @return Similarity as double.
     */
    public static double computeSimilarity(Alignment alignment_1, Alignment alignment_2) {
        if (alignment_1 == null || alignment_2 == null) return 0;
        if (alignment_1 == null || alignment_2 == null) return 0;
        double numberOfSharedCorrespondences = Alignment.intersection(alignment_1, alignment_2).size();
        double numberOfUniqueCorrespondences = Alignment.union(alignment_1, alignment_2).size();
        if(numberOfUniqueCorrespondences == 0) return 0.0; // (denominator must not be 0)
        return numberOfSharedCorrespondences / numberOfUniqueCorrespondences;
    }


    /**
     * Indicator on whether Micro or Macro average shall be used for aggregation operations.
     */
    public enum CalculationMode {
        MICRO, MACRO;
    }
}
