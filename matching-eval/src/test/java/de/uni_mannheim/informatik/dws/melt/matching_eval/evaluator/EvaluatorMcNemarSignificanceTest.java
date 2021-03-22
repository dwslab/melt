package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorMcNemarSignificanceTest {


    @AfterAll
    static void cleanUp() {
        try {
            FileUtils.deleteDirectory(new File("./mc_nemar_base_dir"));
        } catch (IOException ioe){
            // we do not act here
        }
    }

    @Test
    void calculateSignificance() {
        ExecutionResultSet ers = Executor.loadFromAnatomyResultsFolder("./src/test/resources/2016_anatomy/");
        EvaluatorMcNemarSignificance evaluator = new EvaluatorMcNemarSignificance(ers);
        Map<EvaluatorMcNemarSignificance.McNemarIndividualResult, Double> result = evaluator.calculatePvalues(0.05,
                EvaluatorMcNemarSignificance.TestType.ASYMPTOTIC_CONTINUITY_CORRECTION);
        for(Map.Entry<EvaluatorMcNemarSignificance.McNemarIndividualResult, Double> entry : result.entrySet()) {
            EvaluatorMcNemarSignificance.McNemarIndividualResult individualResult = entry.getKey();
            if (individualResult.matcherName1.equals(individualResult.matcherName2)) {
                assertTrue(entry.getValue() > individualResult.alpha);
            } else if (individualResult.matcherName1.equals("AML") || individualResult.matcherName2.equals("AML")) {
                assertTrue(entry.getValue() < individualResult.alpha, "Error for set: " + individualResult.matcherName1 + "  ||  " + individualResult.matcherName2);
            }
        }
    }

    /**
     * Simple test making sure that there is no error when writing and that something is written.
     * Uses the anatomy track (contains 1 test case).
     */
    @Test
    void writeResultsToDirectoryAnatomy(){
        ExecutionResultSet ers = Executor.loadFromAnatomyResultsFolder("./src/test/resources/2016_anatomy/");
        EvaluatorMcNemarSignificance evaluator = new EvaluatorMcNemarSignificance(ers);
        File mcNemarBaseDirectory = new File("./mc_nemar_base_dir");
        mcNemarBaseDirectory.deleteOnExit();
        evaluator.writeResultsToDirectory(mcNemarBaseDirectory);
        checkFiles(mcNemarBaseDirectory);

        // null test (should fail gracefully)
        evaluator.writeResultsToDirectory(null);
        mcNemarBaseDirectory.delete();
    }

    /**
     * Simple test making sure that there is no error when writing and that something is written.
     * Uses the conference track (multiple test cases).
     */
    @Test
    void writeResultsToDirectoryConference(){
        ExecutionResultSet ers = Executor.loadFromConferenceResultsFolder("./src/test/resources" +
                "/2019_conference_shortened/");
        EvaluatorMcNemarSignificance evaluator = new EvaluatorMcNemarSignificance(ers);
        File mcNemarBaseDirectory = new File("./mc_nemar_base_dir");
        mcNemarBaseDirectory.deleteOnExit();
        evaluator.writeResultsToDirectory(mcNemarBaseDirectory);
        checkFiles(mcNemarBaseDirectory);

        // null test (should fail gracefully)
        evaluator.writeResultsToDirectory(null);
        mcNemarBaseDirectory.delete();
    }

    /**
     * Re-usable test snippet.
     * The check covers:
     * <ul>
     *     <li>Check if correct number of files written.</li>
     *     <li>Check if track/testcase files have the correct header.</li>
     * </ul>
     * @param baseDirectory The base directory for which a check shall be executed.
     */
    void checkFiles(File baseDirectory){
        assertTrue(baseDirectory.exists());

        // make sure that 8 files were written:
        assertEquals(8, baseDirectory.listFiles().length);

        List<String> trackMcNemarAsymptotic = StringOperations.readListFromFile(new File(baseDirectory,
                EvaluatorMcNemarSignificance.FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC));
        assertFalse(trackMcNemarAsymptotic.get(0).contains("Test Case"));

        List<String> testCaseMcNemarAsymptotic =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC));
        assertTrue(testCaseMcNemarAsymptotic.get(0).contains("Test Case"));

        List<String> trackMcNemarAsymptoticExactFallback =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK));
        assertFalse(trackMcNemarAsymptoticExactFallback.get(0).contains("Test Case"));
        assertTrackFileIsNotUndecided(trackMcNemarAsymptoticExactFallback);

        List<String> testCaseMcNemarAsymptoticExactFallback =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK));
        assertTrue(testCaseMcNemarAsymptoticExactFallback.get(0).contains("Test Case"));
        assertTestCaseFileIsNotUndecided(testCaseMcNemarAsymptoticExactFallback);

        List<String> trackMcNemarAsymptoticCcorrection =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC_CCORRECTION));
        assertFalse(trackMcNemarAsymptoticCcorrection.get(0).contains("Test Case"));

        List<String> testCaseMcNemarAsymptoticCcorrection =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC_CCORRECTION));
        assertTrue(testCaseMcNemarAsymptoticCcorrection.get(0).contains("Test Case"));

        List<String> trackMcNemarAsymptoticCcorrectionExactFallback =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK));
        assertFalse(trackMcNemarAsymptoticCcorrectionExactFallback.get(0).contains("Test Case"));
        assertTrackFileIsNotUndecided(trackMcNemarAsymptoticCcorrectionExactFallback);

        List<String> testCaseMcNemarAsymptoticCcorrectionExactFallback =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK));
        assertTrue(testCaseMcNemarAsymptoticCcorrectionExactFallback.get(0).contains("Test Case"));
        assertTestCaseFileIsNotUndecided(testCaseMcNemarAsymptoticCcorrectionExactFallback);
    }

    /**
     * Check that the specified file content is always defined (must be true for fallback strategies).
     */
    void assertTestCaseFileIsNotUndecided(List<String> fileContent){
        boolean firstLine = true;
        for(String line : fileContent){
            if(firstLine){
                firstLine = false;
                continue;
            }
            String significance = line.split(",")[line.split(",").length-1];
            assertTrue(significance.equalsIgnoreCase("true") || significance.equalsIgnoreCase("false"));
        }
    }

    /**
     * Check that the specified file content is always defined (must be true for fallback strategies).
     */
    void assertTrackFileIsNotUndecided(List<String> fileContent){
        boolean firstLine = true;
        for(String line : fileContent){
            if(firstLine){
                firstLine = false;
                continue;
            }
            String significance = line.split(",")[line.split(",").length-1];
            assertTrue(significance.equalsIgnoreCase("0"));
        }
    }

    @Test
    void testApacheCommonsLibrary(){
        ChiSquaredDistribution d = new ChiSquaredDistribution(1);
        System.out.println(1.0 - d.cumulativeProbability(4));
    }

    @Test
    void nCr(){
        assertEquals(2, EvaluatorMcNemarSignificance.nCr(2, 1));
        assertEquals(3, EvaluatorMcNemarSignificance.nCr(3, 2));
    }

}