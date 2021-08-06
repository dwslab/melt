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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class offers static functionality to analyze and optimize matchers in terms of their confidences (and
 * confidence thresholds).
 */
public class ConfidenceFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfidenceFinder.class);

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
    
    /**
     * Given two alignments, this method determines the best cutting point (main confidence in correspondences) in order to optimize the F1-score.
     * @param reference the reference alignment to use
     * @param systemAlignment the system alignment
     * @param gsCompleteness what gold standard completeness is given - 
     * if reference alignment is a subset of the overall reference alignment, use {@link GoldStandardCompleteness#PARTIAL_SOURCE_INCOMPLETE_TARGET_INCOMPLETE}.
     * @return The optimal confidence threshold for an optimal F1 measure. All correspondences with a confidence
     * LOWER than the result should be discarded. You can directly use
     * {@link de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter}
     * to cut correspondences LESS than the optimal threshold determined by this method.
     */
    public static double getBestConfidenceForFmeasure(Alignment reference, Alignment systemAlignment,
                                                      GoldStandardCompleteness gsCompleteness){
        if(reference.isEmpty()) {
            return systemAlignment.getMinimalConfidence();
        }

        ConfusionMatrix m = new ConfusionMatrixMetric().compute(reference, systemAlignment, gsCompleteness);
        LOGGER.info("Search for best confidence (optimizing F-Measure) given {} reference and {} system " +
                        "correspondences. Without thresholding: tp: {} fp: {} fn: {}",
                reference.size(), systemAlignment.size(), m.getTruePositiveSize(), m.getFalsePositiveSize(),
                m.getFalseNegativeSize());
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
        int tpSize = m.getTruePositive().cut(bestConf).size();
        int fpSize = m.getFalsePositive().cut(bestConf).size();
        int fnSize = m.getFalseNegativeSize() + (m.getTruePositiveSize() - tpSize);
        LOGGER.info("Found best confidence of {} which leads to F-Measure of {} (tp: {} fp: {} fn: {})",
            bestConf, bestValue, tpSize, fpSize, fnSize);
        
        return bestConf;
    }
    
    /**
     * Given an ExecutionResult, this method determines the best cutting point in order 
     * to optimize the F_beta-score (beta is given as a parameter).
     * @param executionResult The execution result for which the optimal confidence threshold shall be determined.
     * @param beta the beta value for F-beta measure
     * @return The optimal confidence threshold for an optimal F_beta measure. All correspondences with a confidence
     * LOWER than the result should be discarded. You can directly use
     * {@link de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter}
     * to cut correspondences LESS than the optimal threshold determined by this method.
     */
    public static double getBestConfidenceForFmeasureBeta(ExecutionResult executionResult, double beta){
        return getBestConfidenceForFmeasureBeta(executionResult.getReferenceAlignment(),
                executionResult.getSystemAlignment(),
                executionResult.getTestCase().getGoldStandardCompleteness(),
                beta);
    }
    
    /**
     * Given two alignments, this method determines the best cutting point (main confidence in correspondences)
     * in order to optimize the F_beta-score (beta is given as a parameter).
     * @param reference the reference alignment to use
     * @param systemAlignment the system alignment
     * @param gsCompleteness what gold standard completeness is given - 
     * if reference alignment is a subset of the overall reference alignment, use {@link GoldStandardCompleteness#PARTIAL_SOURCE_INCOMPLETE_TARGET_INCOMPLETE}.
     * @param beta the beta value for F-beta measure
     * @return The optimal confidence threshold for an optimal F_beta measure. All correspondences with a confidence
     * LOWER than the result should be discarded. You can directly use
     * {@link de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter}
     * to cut correspondences LESS than the optimal threshold determined by this method.
     */
    public static double getBestConfidenceForFmeasureBeta(Alignment reference, Alignment systemAlignment,
                                                      GoldStandardCompleteness gsCompleteness, double beta){
        if(reference.isEmpty()) {
            return systemAlignment.getMinimalConfidence();
        }

        ConfusionMatrix m = new ConfusionMatrixMetric().compute(reference, systemAlignment, gsCompleteness);
        LOGGER.info("Search for best confidence (optimizing F_{}) given {} reference and {} system correspondences. Without thresholding: tp: {} fp: {} fn: {}",
                beta, reference.size(), systemAlignment.size(), m.getTruePositiveSize(), m.getFalsePositiveSize(), m.getFalseNegativeSize());
        List<Double> systemConfidences = new ArrayList<>(getOccurringConfidences(systemAlignment, 2));
        Collections.sort(systemConfidences);
        double bestConf = 1.0d;
        double bestValue = 0.0d;
        for(Double conf : systemConfidences){
            int tpSize = m.getTruePositive().cut(conf).size();
            int fpSize = m.getFalsePositive().cut(conf).size();
            int fnSize = m.getFalseNegativeSize() + (m.getTruePositiveSize() - tpSize);
            
            double precision = divideWithTwoDenominators(tpSize, tpSize, fpSize);
            double recall = divideWithTwoDenominators(tpSize, tpSize, fnSize);
            double fbeta = getFbetaMeasure(precision, recall, beta);

            if(fbeta >= bestValue){
                bestConf = conf;
                bestValue = fbeta;
            }
        }
        int tpSize = m.getTruePositive().cut(bestConf).size();
        int fpSize = m.getFalsePositive().cut(bestConf).size();
        int fnSize = m.getFalseNegativeSize() + (m.getTruePositiveSize() - tpSize);
        LOGGER.info("Found best confidence of {} which leads to F_{} of {} (tp: {} fp: {} fn: {})",
            bestConf, beta, bestValue, tpSize, fpSize, fnSize);
        
        return bestConf;
    }
    
    /**
     * Given an ExecutionResult, this method determines the best cutting point in order to optimize the precision.
     * @param executionResult The execution result for which the optimal confidence threshold shall be determined.
     * @return The optimal confidence threshold for an optimal precision. All correspondences with a confidence
     * LOWER than the result should be discarded. You can directly use
     * {@link de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter}
     * to cut correspondences LESS than the optimal threshold determined by this method.
     */
    public static double getBestConfidenceForPrecision(ExecutionResult executionResult){
        return getBestConfidenceForPrecision(executionResult.getReferenceAlignment(),
                executionResult.getSystemAlignment(),
                executionResult.getTestCase().getGoldStandardCompleteness());
    }
    
    /**
     * Given two alignments, this method determines the best cutting point (main confidence in correspondences) in
     * order to optimize the precision.
     * @param reference the reference alignment to use
     * @param systemAlignment the system alignment
     * @param gsCompleteness what gold standard completeness is given - 
     * if reference alignment is a subset of the overall reference alignment, use
     * {@link GoldStandardCompleteness#PARTIAL_SOURCE_INCOMPLETE_TARGET_INCOMPLETE}.
     * @return The optimal confidence threshold for an optimal precision. All correspondences with a confidence
     * LOWER than the result should be discarded. You can directly use
     * {@link de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter}
     * to cut correspondences LESS than the optimal threshold determined by this method.
     */
    public static double getBestConfidenceForPrecision(Alignment reference, Alignment systemAlignment,
                                                      GoldStandardCompleteness gsCompleteness){
        if(reference.isEmpty()) {
            return systemAlignment.getMinimalConfidence();
        }

        ConfusionMatrix m = new ConfusionMatrixMetric().compute(reference, systemAlignment, gsCompleteness);
        LOGGER.info("Search for best confidence (optimizing precision) given {} reference and {} system correspondences. Without thresholding: tp: {} fp: {} fn: {}",
                reference.size(), systemAlignment.size(), m.getTruePositiveSize(), m.getFalsePositiveSize(), m.getFalseNegativeSize());
        List<Double> systemConfidences = new ArrayList<>(getOccurringConfidences(systemAlignment, 2));
        Collections.sort(systemConfidences);
        double bestConf = 1.0d;
        double bestValue = 0.0d;
        for(Double conf : systemConfidences){
            int tpSize = m.getTruePositive().cut(conf).size();
            int fpSize = m.getFalsePositive().cut(conf).size();
            double precision = divideWithTwoDenominators(tpSize, tpSize, fpSize);

            if(precision >= bestValue){
                bestConf = conf;
                bestValue = precision;
            }
        }
        int tpSize = m.getTruePositive().cut(bestConf).size();
        int fpSize = m.getFalsePositive().cut(bestConf).size();
        LOGGER.info("Found best confidence of {} which leads to precision of {} (tp: {} fp: {})",
                bestConf, bestValue, tpSize, fpSize);
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
    
    private static double getFbetaMeasure(double precision, double recall, double beta){
        double betaSquared = Math.pow(beta, 2);
        double numerator = (1 + betaSquared) * (precision * recall);
        double denominator = ((betaSquared * precision) + recall);
        if(denominator == 0){
            return 0;
        }else{
            return numerator / denominator;
        }
    }
    
}
