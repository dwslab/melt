package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.significance;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.Evaluator;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.significance.TestType.ASYMPTOTIC_CONTINUITY_CORRECTION_EXACT_FALLBACK;

/**
 * Implementation of a significance test according to information specified in:
 * <a href="https://dl.acm.org/doi/pdf/10.1145/3193573">Mohammadi, Majid; Atashin, Amir Ahooye;
 * Hofman, Wout; Tan, Yaohua. Comparison of Ontology
 * Alignment Systems Across Single Matching Task Via the McNemar's Test. 2018.</a>.
 */
public class EvaluatorMcNemarSignificance extends Evaluator {


    /**
     * Default Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorMcNemarSignificance.class);

    protected double alpha;

    // Default file names (files will be created in baseDirectory.

    public static final String FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC =
            "TestCase_McNemar_asymptotic.csv";
    public static final String FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC =
            "Track_McNemar_asymptotic.csv";
    public static final String FILE_NAME_CROSS_TRACK_MC_NEMAR_ASYMPTOTIC =
            "Cross_Track_McNemar_asymptotic.csv";

    public static final String FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK =
            "TestCase_McNemar_asymptotic_exact_fallback.csv";
    public static final String FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK =
            "Track_McNemar_asymptotic_exact_fallback.csv";
    public static final String FILE_NAME_CROSS_TRACK_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK =
            "Cross_Track_McNemar_asymptotic_exact_fallback.csv";

    public static final String FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC_CCORRECTION =
            "TestCase_McNemar_asymptotic_with_continuity_correction.csv";
    public static final String FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC_CCORRECTION =
            "Track_McNemar_asymptotic_with_continuity_correction.csv";
    public static final String FILE_NAME_CROSS_TRACK_MC_NEMAR_ASYMPTOTIC_CCORRECTION =
            "Cross_Track_McNemar_asymptotic_with_continuity_correction.csv";

    public static final String FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK =
            "TestCase_McNemar_asymptotic_with_continuity_correction_exact_fallback.csv";
    public static final String FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK =
            "Track_McNemar_asymptotic_with_continuity_correction_exact_fallback.csv";
    public static final String FILE_NAME_CROSS_TRACK_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK =
            "Cross_Track_McNemar_asymptotic_with_continuity_correction_exact_fallback.csv";


    /**
     * Constructor. It runs the test with alpha=0.05
     *
     * @param results The results of the matching process.
     */
    public EvaluatorMcNemarSignificance(ExecutionResultSet results) {
        this(results, 0.05);
    }

    /**
     * Constructor.
     *
     * @param results The results of the matching process.
     * @param alpha   The desired alpha (probability of making a type 1 error.
     */
    public EvaluatorMcNemarSignificance(ExecutionResultSet results, double alpha) {
        super(results);
        this.alpha = alpha;
    }

    /**
     * Two files will be written.
     *
     * @param baseDirectory The directory to which the result shall be written.
     */
    @Override
    public void writeResultsToDirectory(File baseDirectory) {
        // make base directory if it does not exist
        if (baseDirectory == null) {
            LOGGER.error("The given base directory does not exist. ABORT.");
            return;
        }
        if (baseDirectory.exists() && baseDirectory.isFile()) {
            LOGGER.error("The given base directory is a file, please specify a directory. ABORT.");
        }
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs();
        }

        // with continuity correction
        Map<McNemarIndividualResult, Double> pValuesAsymptoticWithContinuityCorrection = calculatePvalues(this.alpha,
                TestType.ASYMPTOTIC_CONTINUITY_CORRECTION);
        File testCaseResultFileWithContinuity = new File(baseDirectory, FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC_CCORRECTION);
        File trackResultFile = new File(baseDirectory, FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC_CCORRECTION);
        File crossTrackResultFile = new File(baseDirectory, FILE_NAME_CROSS_TRACK_MC_NEMAR_ASYMPTOTIC_CCORRECTION);
        writeTestCaseResultFile(pValuesAsymptoticWithContinuityCorrection, testCaseResultFileWithContinuity);
        writeTrackResultFile(pValuesAsymptoticWithContinuityCorrection, trackResultFile);
        writeCrossTrackResultFile(pValuesAsymptoticWithContinuityCorrection, crossTrackResultFile);

        // with continuity correction and exact fallback
        Map<McNemarIndividualResult, Double> pValuesAsymptoticContinuityCorrectionExactFallback =
                calculatePvalues(alpha, ASYMPTOTIC_CONTINUITY_CORRECTION_EXACT_FALLBACK);
        File testCaseResultFileContinuityCorrectExactFallback = new File(baseDirectory,
                FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK);
        File trackResultFileContinuityCorrectExactFallback = new File(baseDirectory,
                FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK);
        File crossTrackResultFileContinuityCorrectExactFallback = new File(baseDirectory,
                FILE_NAME_CROSS_TRACK_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK);
        writeTestCaseResultFile(pValuesAsymptoticContinuityCorrectionExactFallback, testCaseResultFileContinuityCorrectExactFallback);
        writeTrackResultFile(pValuesAsymptoticContinuityCorrectionExactFallback, trackResultFileContinuityCorrectExactFallback);
        writeCrossTrackResultFile(pValuesAsymptoticContinuityCorrectionExactFallback, crossTrackResultFileContinuityCorrectExactFallback);

        // without continuity correction
        Map<McNemarIndividualResult, Double> pValuesAsymptotic = calculatePvalues(this.alpha, TestType.ASYMPTOTIC);
        File testCaseAsymptoticResultFile = new File(baseDirectory, FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC);
        File trackAsymptoticResultFile = new File(baseDirectory, FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC);
        File crossTrackAsymptoticResultFile = new File(baseDirectory, FILE_NAME_CROSS_TRACK_MC_NEMAR_ASYMPTOTIC);
        writeTrackResultFile(pValuesAsymptotic, trackAsymptoticResultFile);
        writeTestCaseResultFile(pValuesAsymptotic, testCaseAsymptoticResultFile);
        writeCrossTrackResultFile(pValuesAsymptotic, crossTrackAsymptoticResultFile);

        // without continuity correction and exact fallback
        Map<McNemarIndividualResult, Double> pValuesAsymptoticExactFallback = calculatePvalues(this.alpha,
                TestType.ASYMPTOTIC_EXACT_FALLBACK);
        File testCaseAsymptoticResultFileExactFallback = new File(baseDirectory,
                FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK);
        File trackAsymptoticResultFileExactFallback = new File(baseDirectory, FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK);
        File crossTrackAsymptoticResultFileExactFallback = new File(baseDirectory,
                FILE_NAME_CROSS_TRACK_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK);
        writeTrackResultFile(pValuesAsymptoticExactFallback, trackAsymptoticResultFileExactFallback);
        writeTestCaseResultFile(pValuesAsymptoticExactFallback, testCaseAsymptoticResultFileExactFallback);
        writeCrossTrackResultFile(pValuesAsymptoticExactFallback, crossTrackAsymptoticResultFileExactFallback);
    }

    /**
     * Write the results file on the granularity of matchers.
     * @param pValues The p values.
     * @param fileToWrite The file that shall be written.
     */
    private void writeCrossTrackResultFile(Map<McNemarIndividualResult, Double> pValues, File fileToWrite){
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite),
                    StandardCharsets.UTF_8));
            writer.write("Matcher Name 1,Matcher Name 2,Alpha,Significantly Different,Not Significantly " +
                    "Different,Cannot be Determined\n");
            Map<McNemarCrossTrackResult, SignificanceCount> resultMap = new HashMap<>();

            for (Map.Entry<McNemarIndividualResult, Double> entry : pValues.entrySet()) {
                McNemarCrossTrackResult crossTrackResult = entry.getKey().getCrossTrackResult();
                if (resultMap.containsKey(crossTrackResult)) {
                    SignificanceCount count = resultMap.get(crossTrackResult);
                    count.increment(Significance.getSignificance(entry.getValue(), alpha));
                } else {
                    SignificanceCount count = new SignificanceCount(Significance.getSignificance(entry.getValue(), alpha));
                    resultMap.put(crossTrackResult, count);
                }
            }
            for (Map.Entry<McNemarCrossTrackResult, SignificanceCount> entry : resultMap.entrySet()) {
                writer.write(entry.getKey().toString() + "," + entry.getValue().significantlyDifferent + "," +
                        entry.getValue().notSignificantlyDifferent + "," + entry.getValue().notDefined + "\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            LOGGER.error("An error occurred while trying to write file '" + fileToWrite.getAbsolutePath() + "'.");
        }
    }

    /**
     * Write the results file on the granularity of tracks.
     * @param pValues The p values.
     * @param fileToWrite The file that shall be written.
     */
    private void writeTrackResultFile(Map<McNemarIndividualResult, Double> pValues, File fileToWrite) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite),
                    StandardCharsets.UTF_8));
            writer.write("Track,Matcher Name 1,Matcher Name 2,Alpha,Significantly Different,Not Significantly " +
                    "Different,Cannot be Determined\n");
            Map<McNemarTrackResult, SignificanceCount> trackResultMap = new HashMap<>();

            for (Map.Entry<McNemarIndividualResult, Double> entry : pValues.entrySet()) {
                McNemarTrackResult trackResult = entry.getKey().getTrackResult();
                if (trackResultMap.containsKey(trackResult)) {
                    SignificanceCount count = trackResultMap.get(trackResult);
                    count.increment(Significance.getSignificance(entry.getValue(), alpha));
                } else {
                    SignificanceCount count = new SignificanceCount(Significance.getSignificance(entry.getValue(), alpha));
                    trackResultMap.put(trackResult, count);
                }
            }
            for (Map.Entry<McNemarTrackResult, SignificanceCount> entry : trackResultMap.entrySet()) {
                writer.write(entry.getKey().toString() + "," + entry.getValue().significantlyDifferent + "," +
                        entry.getValue().notSignificantlyDifferent + "," + entry.getValue().notDefined + "\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            LOGGER.error("An error occurred while trying to write file '" + fileToWrite.getAbsolutePath() + "'.");
        }
    }

    /**
     * Write the results file on the granularity of test cases.
     * @param pValues The p values.
     * @param fileToWrite The file that shall be written.
     */
    private void writeTestCaseResultFile(Map<McNemarIndividualResult, Double> pValues, File fileToWrite) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite),
                    StandardCharsets.UTF_8));
            writer.write("Track,Test Case,Matcher Name 1,Matcher Name 2,Alpha,p,Significantly Different?\n");
            for (Map.Entry<McNemarIndividualResult, Double> entry : pValues.entrySet()) {

                String isSignificantlyDifferentString;
                if(entry.getValue().isNaN()){
                    isSignificantlyDifferentString = "<undefined>";
                } else {
                    isSignificantlyDifferentString = "" + (entry.getValue() < this.alpha);
                }
                writer.write(entry.getKey().toString() + "," + entry.getValue() + "," + isSignificantlyDifferentString + "\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            LOGGER.error("An error occurred while trying to write file '" + fileToWrite.getAbsolutePath() + "'.");
        }
    }

    public Map<McNemarIndividualResult, Double> calculatePvalues(double alpha, TestType testType) {
        Map<McNemarIndividualResult, Double> result = new HashMap<>();
        for (ExecutionResult result1 : results) {
            if(result1.getRefinements().size() > 0){
                // for now we only work with raw results
                continue;
            }
            for (ExecutionResult result2 : results) {
                if(result2.getRefinements().size() > 0){
                    // for now we only work with raw results
                    continue;
                }
                if (result1.getTestCase().getName().equals(result2.getTestCase().getName()) && result1.getTrack().getName().equals(result2.getTrack().getName())) {
                    McNemarIndividualResult mr = new McNemarIndividualResult(result1.getMatcherName(), result2.getMatcherName(), result1.getTestCase().getName(), result1.getTrack().getName(), alpha);
                    double pValue;
                    try {
                        pValue = pValueConsideringFalsePositives(result1, result2, testType);
                        result.put(mr, pValue);
                    } catch (ArithmeticException ae) {
                        ae.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    /**
     * Given two execution results, it is determined whether the two results are significantly different (p &lt; alpha).
     * The execution results must be from the same test case.
     *
     * @param executionResult1 Result 1.
     * @param executionResult2 Result 2.
     * @return p value
     */
    private double pValueConsideringFalsePositives(ExecutionResult executionResult1, ExecutionResult executionResult2) {
        return pValueConsideringFalsePositives(executionResult1, executionResult2, TestType.ASYMPTOTIC_CONTINUITY_CORRECTION);
    }

    /**
     * GGiven two execution results, it is determined whether the two results are significantly different (p &lt; alpha).
     * The execution results must be from the same test case.
     *
     * @param executionResult1 Result 1.
     * @param executionResult2 Result 2.
     * @param testType         The type of test to be used.
     * @return p value. NaN if p cannot be calculated.
     */
    private double pValueConsideringFalsePositives(ExecutionResult executionResult1, ExecutionResult executionResult2, TestType testType) {

        // initialize the chi square distribution
        ChiSquaredDistribution distribution = new ChiSquaredDistribution(1);

        // n01
        Alignment A2_intersects_R = Alignment.intersection(executionResult2.getSystemAlignment(), executionResult2.getReferenceAlignment());
        Alignment A2_intersects_R_minus_A1 = Alignment.subtraction(A2_intersects_R, executionResult1.getSystemAlignment());
        int summand_01a = A2_intersects_R_minus_A1.size();
        int summand_01b = Alignment.subtraction(Alignment.subtraction(executionResult1.getSystemAlignment(), executionResult2.getSystemAlignment()), executionResult1.getReferenceAlignment()).size();
        int n01 = summand_01a + summand_01b;

        // n10
        Alignment A1_intersects_R = Alignment.intersection(executionResult1.getSystemAlignment(), executionResult1.getReferenceAlignment());
        Alignment A1_intersects_R_minus_A2 = Alignment.subtraction(A1_intersects_R, executionResult2.getSystemAlignment());
        int summand_10a = A1_intersects_R_minus_A2.size();
        int summand_10b = Alignment.subtraction(Alignment.subtraction(executionResult2.getSystemAlignment(), executionResult1.getSystemAlignment()), executionResult1.getReferenceAlignment()).size();
        int n10 = summand_10a + summand_10b;

        if (testType == TestType.ASYMPTOTIC) {
            if (n01 == 0 && n10 == 0) {
                LOGGER.warn("Significance cannot be determined using McNemar's Asymptotic test because" +
                        "n01 == 0 and n10 == 0. [Matchers: " + executionResult1.getMatcherName() + " | " +
                        executionResult2.getMatcherName() + "]");
                // most likely this is the case for identical alignments
                return 1.0;
            }
            if (n01 + n10 < 25) {
                LOGGER.warn("A sufficient number of data is required: n01 + n10 >= 25. This is not the case here.");
                return Double.NaN;
            }
            double chiSquare = Math.pow(n01 - n10, 2) / (n01 + n10);
            return (1.0 - distribution.cumulativeProbability(chiSquare));
        } else if (testType == TestType.ASYMPTOTIC_CONTINUITY_CORRECTION) {
            if (n01 == 0 && n10 == 0) {
                LOGGER.warn("Significance cannot be determined using McNemar's Asymptotic test with continuity " +
                        "correction because n01 == 0 and n10 == 0. [Matchers: " + executionResult1.getMatcherName() +
                        " | " + executionResult2.getMatcherName() + "]");
                // most likely this is the case for identical alignments
                return 1.0;
            }
            if (n01 + n10 < 25) {
                LOGGER.warn("A sufficient number of data is required: n01 + n10 >= 25. This is not the case here.");
                return Double.NaN;
            }
            double chiSquare = Math.pow(Math.abs(n01 - n10) - 1, 2) / (n01 + n10);
            return (1.0 - distribution.cumulativeProbability(chiSquare));
        } else if (testType == TestType.EXACT) {
            int n = n01 + n10;
            int result = 0;
            for (int x = 0; x < n; x++) {
                result += nCr(n, x) * 0.25;
            }
            return result;
        } else if (testType == TestType.ASYMPTOTIC_EXACT_FALLBACK) {
            double resultAsymptotic = pValueConsideringFalsePositives(executionResult1, executionResult2,
                    TestType.ASYMPTOTIC);
            if (Double.isNaN(resultAsymptotic)) {
                return pValueConsideringFalsePositives(executionResult1, executionResult2, TestType.EXACT);
            } else return resultAsymptotic;
        } else if (testType == ASYMPTOTIC_CONTINUITY_CORRECTION_EXACT_FALLBACK){
            double resultAsymptoticCCorrection = pValueConsideringFalsePositives(executionResult1, executionResult2,
                    TestType.ASYMPTOTIC_CONTINUITY_CORRECTION);
            if (Double.isNaN(resultAsymptoticCCorrection)) {
                return pValueConsideringFalsePositives(executionResult1, executionResult2, TestType.EXACT);
            } else return resultAsymptoticCCorrection;
        }

        /*
        else if (testType == TestType.MID_P_TEST){
         int n = n01 + n10;
         double exactPvalue = nCr(n, n01) * 0.25 + nCr(n, n10) * 0.25;
         pValue = exactPvalue - nCr(n, n01) * Math.pow(0.5, n);
         }
         */

        // never reached:
        return Double.NaN;
    }

    /**
     * From n choose r with large numbers.
     *
     * @param N N of nCr(N,r)
     * @param R R of nCr(n,R)
     * @return nCr(n, r)
     */
    static BigInteger nCrBigInt(final int N, final int R) {
        BigInteger ret = BigInteger.ONE;
        for (int k = 0; k < R; k++) {
            ret = ret.multiply(BigInteger.valueOf(N - k))
                    .divide(BigInteger.valueOf(k + 1));
        }
        return ret;
    }

    /**
     * From n choose r.
     *
     * @param n N of nCr(N,r)
     * @param r R of nCr(n,R)
     * @return nCr(n, r)
     */
    static long nCr(int n, int r) {
        return fact(n) / (fact(r) * fact(n - r));
    }

    /**
     * Returns the factorial of n.
     *
     * @param n Value for which the factorial shall be calculated.
     * @return Factorial of n.
     */
    static long fact(int n) {
        long res = 1;
        for (long i = 2; i <= n; i++) {
            res = res * i;
        }
        return res;
    }
}
