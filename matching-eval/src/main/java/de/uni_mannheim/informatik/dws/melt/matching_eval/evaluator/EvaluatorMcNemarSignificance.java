package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

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
    private static Logger LOGGER = LoggerFactory.getLogger(EvaluatorMcNemarSignificance.class);
    
    protected double alpha;

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
     * @param alpha The desired alpha (probability of making a type 1 error.
     */
    public EvaluatorMcNemarSignificance(ExecutionResultSet results, double alpha) {
        super(results);
        this.alpha = alpha;
    }


    /**
     * Two files will be written.
     * @param baseDirectory The directory to which the result shall be written.
     */
    @Override
    public void writeResultsToDirectory(File baseDirectory) {
        try {

            // with continuity correction
            File resultFile = new File(baseDirectory, "McNemar_asymptotic_with_continuity_correction.csv");
            BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile));
            writer.write("Track,Test Case,Matcher Name 1,Matcher Name 2,Alpha,p,Significant?\n");
            for(Map.Entry<McNemarIndividualResult, Double> entry : calculatePvalues(this.alpha, TestType.ASYMPTOTIC_TEST_WITH_CONTINUITY_CORRECTION).entrySet()){
                writer.write(entry.getKey().toString() + "," + entry.getValue() + "," + (entry.getValue() < this.alpha) + "\n");
            }
            writer.flush();
            writer.close();

            // without continuity correction
            resultFile = new File(baseDirectory, "McNemar_asymptotic.csv");
            writer = new BufferedWriter(new FileWriter(resultFile));
            writer.write("Track,Test Case,Matcher Name 1,Matcher Name 2,Alpha,p,Significant?\n");
            for(Map.Entry<McNemarIndividualResult, Double> entry : calculatePvalues(this.alpha, TestType.ASYMPTOTIC_TEST).entrySet()){
                writer.write(entry.getKey().toString() + "," + entry.getValue() + "," + (entry.getValue() < this.alpha) + "\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<McNemarIndividualResult, Double> calculatePvalues(double alpha, TestType testType){
        HashMap<McNemarIndividualResult, Double> result = new HashMap<>();
        for (ExecutionResult result1 : results) {
            for (ExecutionResult result2 : results){
                if(result1.getTestCase().getName().equals(result2.getTestCase().getName()) && result1.getTrack().getName().equals(result2.getTrack().getName())){
                    McNemarIndividualResult mr = new McNemarIndividualResult(result1.getMatcherName(), result2.getMatcherName(), result1.getTestCase().getName(), result1.getTrack().getName(), alpha);
                    double pValue = 1.0;
                    try {
                        pValue = pValueConsideringFalsePositives(result1, result2, testType);
                    } catch (ArithmeticException ae){
                        ae.printStackTrace();
                    }
                    result.put(mr, pValue);
                }
            }
        }
        return result;
    }


    /**
     * Local data structure.
     * To be used for testing.
     */
    public class McNemarIndividualResult{
        public String matcherName1;
        public String matcherName2;
        public String testCaseName;
        public String trackName;
        public double alpha;

        public McNemarIndividualResult(String matcherName1, String matcherName2, String testCaseName, String trackName, double alpha){
            this.matcherName1 = matcherName1;
            this.matcherName2 = matcherName2;
            this.testCaseName = testCaseName;
            this.trackName = trackName;
            this.alpha = alpha;
        }

        @Override
        public boolean equals(Object o){
            if (this == o) return true;
            if (!(o instanceof McNemarIndividualResult)) return false;

            McNemarIndividualResult that = (McNemarIndividualResult) o;
            return this.matcherName1.equals(that.matcherName1) &&
                    this.matcherName2.equals(that.matcherName2) &&
                    this.testCaseName.equals(that.testCaseName) &&
                    this.trackName.equals(that.trackName) &&
                    this.alpha == that.alpha;
        }

        @Override
        public String toString(){
            return this.trackName + "," + this.testCaseName + "," + this.matcherName1 + "," + this.matcherName2 + "," + alpha;
        }
    }


    /**
     * Given two execution results, it is determined whether the two results are significantly different (p &lt; alpha).
     * The execution results must be from the same test case.
     * @param executionResult1 Result 1.
     * @param executionResult2 Result 2.
     * @return p value
     */
    private double pValueConsideringFalsePositives(ExecutionResult executionResult1, ExecutionResult executionResult2){
        return pValueConsideringFalsePositives(executionResult1, executionResult2, TestType.ASYMPTOTIC_TEST_WITH_CONTINUITY_CORRECTION);
    }


        /**
         * GGiven two execution results, it is determined whether the two results are significantly different (p &lt; alpha).
         * The execution results must be from the same test case.
         * @param executionResult1 Result 1.
         * @param executionResult2 Result 2.
         * @param testType The type of test to be used.
         * @return p value
         */
    private double pValueConsideringFalsePositives(ExecutionResult executionResult1, ExecutionResult executionResult2, TestType testType){

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

        //BigInteger pValueBig;
        double pValue = 0.0;
        if(testType == TestType.ASYMPTOTIC_TEST) {
            if(n01 == 0 && n10 == 0){
                LOGGER.error("Significance cannot be determined using McNemar's Asymptotic test because" +
                        "n01 == 0 and n10 == 0.");
            }
            if(n01 + n10 < 25){
                LOGGER.warn("A sufficient number of data is required: n01 + n10 >= 25. This is not the case here.");
            }
            double chiSquare = Math.pow(n01 - n10, 2) / (n01 + n10);
            return (1.0 - distribution.cumulativeProbability(chiSquare));
        } else if (testType == TestType.ASYMPTOTIC_TEST_WITH_CONTINUITY_CORRECTION){
            if(n01 == 0 && n10 == 0){
                LOGGER.error("Significance cannot be determined using McNemar's Asymptotic test with continuity " +
                        "correction because n01 == 0 and n10 == 0.");
                return 1.0;
            }
            double chiSquare = Math.pow(Math.abs(n01 - n10) - 1, 2) / (n01 + n10);
            return (1.0 - distribution.cumulativeProbability(chiSquare));
        }

        // TODO: mathematically midp and exact are easily available, but there needs to be a "smart" shortcut b/c the factorial is too large for int
        /**
        else if (testType == TestType.EXACT_TEST){
            int n = n01 + n10;
            pValue = nCr(n, n01) * 0.25 + nCr(n, n10) * 0.25;
            //pValueBig = nCrBigInt(n, n01).divide(BigInteger.valueOf(4)).add(nCrBigInt(n, n01)).divide(BigInteger.valueOf(4));

        } else if (testType == TestType.MID_P_TEST){
            int n = n01 + n10;
            double exactPvalue = nCr(n, n01) * 0.25 + nCr(n, n10) * 0.25;
            pValue = exactPvalue - nCr(n, n01) * Math.pow(0.5, n);
        }
         **/

        // (never reached)
        return 1.0;
    }


    /**
     * From n choose r with large numbers.
     * @param N N of nCr(N,r)
     * @param R R of nCr(n,R)
     * @return nCr(n,r)
     */
    static BigInteger nCrBigInt(final int N, final int R) {
        BigInteger ret = BigInteger.ONE;
        for (int k = 0; k < R; k++) {
            ret = ret.multiply(BigInteger.valueOf(N-k))
                    .divide(BigInteger.valueOf(k+1));
        }
        return ret;
    }


    /**
     * From n choose r.
     * @param n N of nCr(N,r)
     * @param r R of nCr(n,R)
     * @return nCr(n,r)
     */
    static long nCr(int n, int r)
    {
        return fact(n) / (fact(r) *
                fact(n - r));
    }


    /**
     * Returns the factorial of n.
     * @param n Value for which the factorial shall be calculated.
     * @return Factorial of n.
     */
    static long fact(int n)
    {
        long res = 1;
        for (long i = 2; i <= n; i++)
            res = res * i;
        return res;
    }


    /**
     * The supported test types.
     */
    public enum TestType {
        //EXACT_TEST,
        //MID_P_TEST,
        ASYMPTOTIC_TEST,
        ASYMPTOTIC_TEST_WITH_CONTINUITY_CORRECTION;
    }


}
