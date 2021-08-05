package de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning;

import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.refinement.ConfidenceRefiner;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrix;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrixMetric;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * This class offers static functionality to analyze and optimize matchers in terms of their confidences (and
 * confidence thresholds).
 */
public class ConfidenceFinder {


    public static Set<Double> getSteps(double start, double end, double stepWidth){
        Set<Double> set = new HashSet<>();
        for(double d = start; d <= end; d += stepWidth){
            set.add(d);
        }
        return set;
    }
    
    public static Set<Double> getOccurringConfidences(Alignment a){
        Set<Double> set = new HashSet<>();
        for(Double c : a.getDistinctConfidences()){
            set.add(c);
        }
        return set;
    }
    
    public static Set<Double> getOccurringConfidences(Alignment a, double begin, double end){
        Set<Double> set = new HashSet<>();
        for(Double c : a.getDistinctConfidences()){
            if(c >= begin && c <= end){
                set.add(c);
            }
        }
        return set;
    }
    
    public static Set<Double> getOccurringConfidences(Alignment a, int decimalPrecision){
        Set<Double> set = new HashSet<>();
        for(Double c : a.getDistinctConfidences()){
            BigDecimal bd = new BigDecimal(c);
            bd = bd.setScale(decimalPrecision, RoundingMode.HALF_UP);
            set.add(bd.doubleValue());
        }
        return set;
    }
    
    public static Set<Double> getOccurringConfidences(Alignment a, int decimalPrecision, double begin, double end){
        Set<Double> set = new HashSet<>();
        for(Double c : a.getDistinctConfidences()){
            BigDecimal bd = new BigDecimal(c);
            bd = bd.setScale(decimalPrecision, RoundingMode.HALF_UP);
            double d = bd.doubleValue();
            if(d >= begin && d <= end){
                set.add(d);
            }
        }
        return set;
    }

    /**
     * Given an ExecutionResult, this method determines the best cutting point in order to optimize the F1-score.
     * @param executionResult The execution result for which the optimal confidence threshold shall be determined.
     * @return The optimal confidence threshold for an optimal F1 measure. All correspondences with a confidence
     * LOWER than the result should be discarded. You can directly use
     * {@link de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter}
     * to cut correspondences LESS than the optimal threshold determined by this method.
     */
    public static double getBestConfidenceForFmeasure(ExecutionResult executionResult){
        return getBestConfidenceForFmeasure(executionResult.getReferenceAlignment(),
                executionResult.getSystemAlignment(),
                executionResult.getTestCase().getGoldStandardCompleteness());
    }

    public static double getBestConfidenceForFmeasure(Alignment reference, Alignment systemAlignment,
                                                      GoldStandardCompleteness gsCompleteness){
        if(reference.isEmpty()) {
            return systemAlignment.getMinimalConfidence();
        }

        ConfusionMatrix m = new ConfusionMatrixMetric().compute(reference, systemAlignment, gsCompleteness);

        List<Double> systemConfidences = new ArrayList<>(getOccurringConfidences(systemAlignment, 2));
        Collections.sort(systemConfidences);
        double bestConf = 1.0d;
        double bestValue = 0.0d;
        for(Double conf : systemConfidences){
            int tpSize = m.getTruePositive().cut(conf).size();
            int fpSize = m.getFalsePositive().cut(conf).size();
            int fnSize = m.getFalseNegativeSize() + (m.getTruePositiveSize() - tpSize);

            /*
            ExecutionResult er = new ExecutionResult(executionResult.getTestCase(),
                    executionResult.getMatcherName(),
                    executionResult.getSystemAlignment().cut(conf),
                    executionResult.getReferenceAlignment());
            ConfusionMatrix m2 = new ConfusionMatrixMetric().compute(er);

            System.out.println("TP");
            System.out.println(m2.getTruePositiveSize());
            System.out.println(tpSize);

            System.out.println("FP");
            System.out.println(m2.getFalsePositiveSize());
            System.out.println(fpSize);

            System.out.println("FN");
            System.out.println(m2.getFalseNegativeSize());
            System.out.println(fnSize);
             */

            double precision = divideWithTwoDenominators(tpSize, tpSize, fpSize);
            double recall = divideWithTwoDenominators(tpSize, tpSize, fnSize);
            double f1measure = divideWithTwoDenominators(2*precision*recall, precision, recall);

            if(f1measure >= bestValue){
                bestConf = conf;
                bestValue = f1measure;
            }
        }
        return bestConf;
    }
    
    public static ExecutionResultSet getConfidenceResultSet(ExecutionResult executionResult){
        ExecutionResultSet s = new ExecutionResultSet();
        s.add(executionResult);
        List<Double> systemConfidences = new ArrayList<>(getOccurringConfidences(executionResult.getSystemAlignment(), 2));
        Collections.sort(systemConfidences);
        for(Double conf : systemConfidences){
            s.get(executionResult, new ConfidenceRefiner(conf));
        }
        return s;
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
