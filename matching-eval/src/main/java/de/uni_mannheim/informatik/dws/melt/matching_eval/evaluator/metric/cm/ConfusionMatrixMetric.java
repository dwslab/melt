package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm;

import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;


/**
 * Confusion Matrix Metric.
 * Can handle full and partial gold standards as well as explict null mappings (there is no match for one entity).
 * @see <a href="https://github.com/DanFaria/OAEI_SealsClient/blob/020d97bbfb6816dcca55de5ce178c716da15b711/SealsClientSource/src/main/java/eu/sealsproject/omt/client/Client.java#L322">https://github.com/DanFaria/OAEI_SealsClient/blob/020d97bbfb6816dcca55de5ce178c716da15b711/SealsClientSource/src/main/java/eu/sealsproject/omt/client/Client.java#L322</a>
 * @see <a href="https://github.com/DanFaria/OAEI_SealsClient/blob/master/SealsClientSource/src/main/java/eu/sealsproject/omt/client/HashAlignment.java">https://github.com/DanFaria/OAEI_SealsClient/blob/master/SealsClientSource/src/main/java/eu/sealsproject/omt/client/HashAlignment.java</a>
 * @see <a href="http://www.cs.ox.ac.uk/isg/projects/SEALS/oaei/2017/oaei2017_umls_reference.html">http://www.cs.ox.ac.uk/isg/projects/SEALS/oaei/2017/oaei2017_umls_reference.html</a>
 * @author Sven Hertling, Jan Portisch
 */
public class ConfusionMatrixMetric extends Metric<ConfusionMatrix> {


    /**
     * Default Logger
     */
    private Logger LOGGER = LoggerFactory.getLogger(ConfusionMatrixMetric.class);

    @Override
    public ConfusionMatrix compute(ExecutionResult executionResult) {
        if(executionResult.getTestCase().getGoldStandardCompleteness().isGoldStandardComplete()){
            return computeForCompleteGoldStandard(executionResult);
        }
        else{
            return computeForPartialGoldStandard(executionResult);
        }
    }


    /**
     * Calculate the confusion matrix under the premises that the gold standard is incomplete, i.e., partial.
     * @param executionResult The execution result for which the gold standard shall be calculated.
     * @return The confusion matrix.
     */
    private ConfusionMatrix computeForPartialGoldStandard(ExecutionResult executionResult){
        Alignment truePositives = new Alignment();
        Alignment falsePositives = new Alignment();
        Alignment falseNegatives = new Alignment();

        int numberOfCorrespondences = executionResult.getSystemAlignment().size();
        GoldStandardCompleteness gsCompleteness = executionResult.getTestCase().getGoldStandardCompleteness();

        for(Correspondence referenceCell : executionResult.getReferenceAlignment()){
            if(referenceCell.getRelation() == CorrespondenceRelation.UNKNOWN){
                //see http://www.cs.ox.ac.uk/isg/projects/SEALS/oaei/2017/oaei2017_umls_reference.html
                //don't add it to falsePositive because it should be silently ignored
            }
            else if(referenceCell.getRelation() == CorrespondenceRelation.INCOMPAT){
                //mapping like <"null", "http://.....", =, 1.0> or <"http://.....", "null", =, 1.0>
                //to express than one resource has no correspondence
                if(referenceCell.getEntityTwo().equals("null") || referenceCell.getEntityTwo().trim().isEmpty()){
                    for(Correspondence c : executionResult.getSystemAlignment().getCorrespondencesSourceRelation(referenceCell.getEntityOne(), CorrespondenceRelation.EQUIVALENCE))
                        falsePositives.add(c);
                } else if(referenceCell.getEntityOne().equals("null") || referenceCell.getEntityOne().trim().isEmpty()){
                    for(Correspondence c : executionResult.getSystemAlignment().getCorrespondencesTargetRelation(referenceCell.getEntityTwo(), CorrespondenceRelation.EQUIVALENCE))
                        falsePositives.add(c);
                } else {
                    //negative mapping -> this mapping should not appear in the systemAlignment alignment, otherwise this is a false positive
                    Correspondence systemCell = executionResult.getSystemAlignment().getCorrespondence(referenceCell.getEntityOne(), referenceCell.getEntityTwo(), CorrespondenceRelation.EQUIVALENCE);
                    if(systemCell != null){
                        //found something which should not be found
                        falsePositives.add(systemCell);
                    }
                }
            }
            else {
                Correspondence systemCell = executionResult.getSystemAlignment().getCorrespondence(referenceCell.getEntityOne(), referenceCell.getEntityTwo(), referenceCell.getRelation());

                if(systemCell != null){
                    truePositives.add(systemCell);
                } else {
                    // Confidence is meant to be set by matcher. Hence, it is 0 because matcher did not find this
                    // correspondence. If gold standard confidence is a requirement, this has to be achieved through an extension.
                    referenceCell.setConfidence(0.0);
                    falseNegatives.add(referenceCell);
                }

                if(gsCompleteness.isTargetComplete()){
                    for(Correspondence sameTarget : executionResult.getSystemAlignment().getCorrespondencesTargetRelation(referenceCell.getEntityTwo(), referenceCell.getRelation())){
                        if(sameTarget.equals(referenceCell) == false){
                            falsePositives.add(sameTarget);
                        }
                    }
                }

                if(gsCompleteness.isSourceComplete()){
                    for(Correspondence sameSource : executionResult.getSystemAlignment().getCorrespondencesSourceRelation(referenceCell.getEntityOne(), referenceCell.getRelation())){
                        if(sameSource.equals(referenceCell) == false){
                            falsePositives.add(sameSource);
                        }
                    }
                }
            }
        }

        // The following is required because a gold standard may have the same target or source multiple times in an alignment.
        // The reason for this might be an error or another relation than ows:sameAs.
        // Example:
        // Gold Standard: (A,B,=), (A,C,=)
        // System: (A,B,=)
        // The correspondence (A,B,=) would be TP and FP at the same time using the algorithm above. To handle such cases
        // the TPs are removed from the FPs.
        falsePositives.removeAll(truePositives);

        return calculateConfusionMatrixFromMappings(truePositives, falsePositives, falseNegatives, numberOfCorrespondences);
    }

    /**
     * Calculate the confusion matrix under the premises that the gold standard is complete.
     * @param executionResult The execution result for which the gold standard shall be calculated.
     * @return The confusion matrix.
     */
    private ConfusionMatrix computeForCompleteGoldStandard(ExecutionResult executionResult){
        //TODO: what happens when referenceAlignment is empty and systemAlignment contains 200 mappings?
        Alignment truePositives = new Alignment();
        Alignment falsePositives = new Alignment(executionResult.getSystemAlignment());
        Alignment falseNegatives = new Alignment();
        
        for(Correspondence referenceCell : executionResult.getReferenceAlignment()){
            if(referenceCell.getRelation() == CorrespondenceRelation.UNKNOWN){
                //see http://www.cs.ox.ac.uk/isg/projects/SEALS/oaei/2017/oaei2017_umls_reference.html
                falsePositives.removeCorrespondencesSourceTarget(referenceCell.getEntityOne(), referenceCell.getEntityTwo());
            }//incompat should not appear in gold standard if it is a complete gold standard
            else {
                Correspondence systemCell = executionResult.getSystemAlignment().getCorrespondence(referenceCell.getEntityOne(), referenceCell.getEntityTwo(), referenceCell.getRelation());
                if(systemCell != null){
                    truePositives.add(systemCell);
                    falsePositives.remove(systemCell);
                } else {
                    referenceCell.setConfidence(0.0);
                    falseNegatives.add(referenceCell);
                }
            }
        }
        int numberOfCorrespondences = executionResult.getSystemAlignment().size();
        return calculateConfusionMatrixFromMappings(truePositives, falsePositives, falseNegatives, numberOfCorrespondences);
    }


    /**
     * Calculation method which calculates the confusion matrix given three mappings: tp, fp, and fn.
     * @param truePositives True Positive (tp) mapping.
     * @param falsePositives False Positive (fp) mapping.
     * @param falseNegatives False Negative (fn) mapping.
     * @param numberOfCorrespondences The number of correspondences.
     * @return The confusion matrix.
     */
    public static ConfusionMatrix calculateConfusionMatrixFromMappings(Alignment truePositives, Alignment falsePositives, Alignment falseNegatives, int numberOfCorrespondences){
        double tpSize = truePositives.size();
        double fpSize = falsePositives.size();
        double fnSize = falseNegatives.size();
        double precision = divideWithTwoDenominators(tpSize, tpSize, fpSize);
        double recall = divideWithTwoDenominators(tpSize, tpSize, fnSize);
        return new ConfusionMatrix(truePositives, falsePositives, falseNegatives, numberOfCorrespondences, precision, recall);
    }


    /**
     * Returns aggregated confusion matrices according to the micro average.
     * The micro average sums up individual true positives, false positives and false negatives and calculates
     * one confusion matrix.
     *
     * @param resultsForCalculation The results for which an aggregated confusion matrix shall be computed.
     * @return Aggregated Confusion Matrices
     */
    public ConfusionMatrix getMicroAveragesForResults(Iterable<ExecutionResult> resultsForCalculation) {
        HashSet<ConfusionMatrix> confusionMatrices = new HashSet<>();
        for (ExecutionResult result : resultsForCalculation) {
            confusionMatrices.add(compute(result));
        }
        return getMicroAverages(confusionMatrices);
    }


    /**
     * Returns aggregated confusion matrices according to the macro average.
     *
     * @param resultsForCalculation The results for which an aggregated confusion matrix shall be computed.
     * @return Aggregated Confusion Matrices Aggregated Confusion Matrices
     */
    public ConfusionMatrix getMacroAveragesForResults(Iterable<ExecutionResult> resultsForCalculation) {
        HashSet<ConfusionMatrix> confusionMatrices = new HashSet<>();
        for (ExecutionResult result : resultsForCalculation) {
            confusionMatrices.add(compute(result));
        }
        return getMacroAverages(confusionMatrices);
    }
    
    /**
     * Returns aggregated confusion matrices according to the macro average.
     *
     * @param resultsForCalculation The results for which an aggregated confusion matrix shall be computed.
     * @param numberOfTestCases The number of testcases which should be used for the calculation
     * @return Aggregated Confusion Matrices Aggregated Confusion Matrices
     */
    public ConfusionMatrix getMacroAveragesForResults(Iterable<ExecutionResult> resultsForCalculation, int numberOfTestCases) {
        HashSet<ConfusionMatrix> confusionMatrices = new HashSet<>();
        for (ExecutionResult result : resultsForCalculation) {
            confusionMatrices.add(compute(result));
        }
        Alignment truePositive = new Alignment();
        Alignment falsePositive = new Alignment();
        Alignment falseNegative = new Alignment();

        double precision = 0.0; // dummy init
        double recall = 0.0; // dummy init

        // for aggregation:
        for (ConfusionMatrix individualConfusionMatrix : confusionMatrices) {
            truePositive.addAll(individualConfusionMatrix.getTruePositive());
            falsePositive.addAll(individualConfusionMatrix.getFalsePositive());
            falseNegative.addAll(individualConfusionMatrix.getFalseNegative());
        }
        
        double aggregatedPrecision = 0.0;
        double aggregatedRecall = 0.0;
        for (ConfusionMatrix individualConfusionMatrix : confusionMatrices) {
            aggregatedPrecision = aggregatedPrecision + individualConfusionMatrix.getPrecision();
            aggregatedRecall = aggregatedRecall + individualConfusionMatrix.getRecall();
        }
        precision = aggregatedPrecision / numberOfTestCases;
        recall = aggregatedRecall / numberOfTestCases;
                
        return new ConfusionMatrix(truePositive, falsePositive, falseNegative, precision, recall);
    }


    /**
     * Returns aggregated confusion matrices according to the micro average.
     * The micro average sums up individual true positives, false positives and false negatives and calculates
     * one confusion matrix.
     *
     * @param confusionMatrices The confusion matrices for which an aggregated confusion matrix shall be computed.
     * @return Aggregated Confusion Matrices
     */
    public ConfusionMatrix getMicroAverages(Iterable<ConfusionMatrix> confusionMatrices) {
        return executeAggregation(confusionMatrices, ConfusionMatrixAggregationMode.MICRO);
    }


    /**
     * Returns aggregated confusion matrices according to the macro average.
     *
     * @param confusionMatrices The confusion matrices for which an aggregated confusion matrix shall be computed.
     * @return Aggregated Confusion Matrices Aggregated Confusion Matrices
     */
    public ConfusionMatrix getMacroAverages(Iterable<ConfusionMatrix> confusionMatrices) {
        return executeAggregation(confusionMatrices, ConfusionMatrixAggregationMode.MACRO);
    }


    /**
     * Internal aggregation logic.
     *
     * @param confusionMatrices ConfusionMatrices that shall be used for the aggregation.
     * @param aggregationMode   The kind of aggregation that is to be performed.
     * @return Resulting confusion matrix.
     */
    private ConfusionMatrix executeAggregation(Iterable<ConfusionMatrix> confusionMatrices, ConfusionMatrixAggregationMode aggregationMode) {

        if (aggregationMode == ConfusionMatrixAggregationMode.NONE) {
            LOGGER.warn("Invalid Aggregation Mode: NONE; Fallback: Micro-Average.");
            aggregationMode = ConfusionMatrixAggregationMode.MICRO;
        }

        Alignment truePositive = new Alignment();
        Alignment falsePositive = new Alignment();
        Alignment falseNegative = new Alignment();

        double precision = 0.0; // dummy init
        double recall = 0.0; // dummy init

        // for aggregation:
        double numberOfElementsInConfusionMatrices = 0.0;

        // for number of correspondences
        int numberOfCorrespondences = 0;

        for (ConfusionMatrix individualConfusionMatrix : confusionMatrices) {
            truePositive.addAll(individualConfusionMatrix.getTruePositive());
            falsePositive.addAll(individualConfusionMatrix.getFalsePositive());
            falseNegative.addAll(individualConfusionMatrix.getFalseNegative());
            numberOfElementsInConfusionMatrices++;
            numberOfCorrespondences += individualConfusionMatrix.getNumberOfCorrespondences();
        }

        // compiling the numbers for the calculation
        double tpSize = truePositive.size();
        double fpSize = falsePositive.size();
        double fnSize = falseNegative.size();

        switch (aggregationMode) {
            case MICRO:
                precision = divideWithTwoDenominators(tpSize, tpSize, fpSize);
                recall = divideWithTwoDenominators(tpSize, tpSize, fnSize);
                break;
            case MACRO:
                double aggregatedPrecision = 0;
                double aggregatedRecall = 0;
                for (ConfusionMatrix individualConfusionMatrix : confusionMatrices) {
                    aggregatedPrecision = aggregatedPrecision + individualConfusionMatrix.getPrecision();
                    aggregatedRecall = aggregatedRecall + individualConfusionMatrix.getRecall();
                }
                precision = aggregatedPrecision / numberOfElementsInConfusionMatrices;
                recall = aggregatedRecall / numberOfElementsInConfusionMatrices;
                break;
            case NONE:
                LOGGER.error("Aggregation mode NONE not supported. Fallback: Micro-Avverage.");
                precision = divideWithTwoDenominators(tpSize, tpSize, fpSize);
                recall = divideWithTwoDenominators(tpSize, tpSize, fnSize);
        }
        return new ConfusionMatrix(truePositive, falsePositive, falseNegative, numberOfCorrespondences, precision, recall);
    }


    /**
     * Simple division that is to be performed. The two denominators will be added.
     *
     * @param numerator      Numerator of fraction
     * @param denominatorOne Denominator 1
     * @param denominatorTwo Denominator 2
     * @return Result as double.
     */
    private static double divideWithTwoDenominators(double numerator, double denominatorOne, double denominatorTwo) {
        if ((denominatorOne + denominatorTwo) > 0.0) {
            return numerator / (denominatorOne + denominatorTwo);
        } else {
            return 0.0;
        }
    }

}
