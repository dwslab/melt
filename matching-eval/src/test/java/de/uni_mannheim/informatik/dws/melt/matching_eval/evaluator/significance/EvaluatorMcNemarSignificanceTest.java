package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.significance;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorMcNemarSignificanceTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorMcNemarSignificanceTest.class);

    @AfterAll
    static void cleanUp() {
        deleteFile("./mc_nemar_base_dir");
        deleteFile("./mc_nemar_base_dir_conference");
        deleteFile("./mc_nemar_base_dir_cross_track");
    }

    static void deleteFile(File file){
        if (file == null){
            return;
        }
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException ioe) {
            LOGGER.warn("Could not delete file " + file.getAbsolutePath());
        }
    }

    static void deleteFile(String filePath){
        if(filePath == null){
            return;
        }
        deleteFile(new File(filePath));
    }

    @Test
    void calculateSignificance() {
        ExecutionResultSet ers = Executor.loadFromAnatomyResultsFolder("./src/test/resources/2016_anatomy/");
        EvaluatorMcNemarSignificance evaluator = new EvaluatorMcNemarSignificance(ers);
        Map<McNemarIndividualResult, Double> result = evaluator.calculatePvalues(0.05,
                TestType.ASYMPTOTIC_CONTINUITY_CORRECTION);
        for (Map.Entry<McNemarIndividualResult, Double> entry : result.entrySet()) {
            McNemarIndividualResult individualResult = entry.getKey();
            if (individualResult.matcherName1.equals(individualResult.matcherName2)) {
                assertTrue(entry.getValue() > individualResult.alpha);
            } else if (individualResult.matcherName1.equals("AML") || individualResult.matcherName2.equals("AML")) {
                assertTrue(entry.getValue() < individualResult.alpha,
                        "Error for set: " + individualResult.matcherName1 + "  ||  " + individualResult.matcherName2);
            }
        }
    }

    /**
     * Simple test making sure that there is no error when writing and that something is written.
     * Uses the anatomy track (contains 1 test case).
     */
    @Test
    void writeResultsToDirectoryAnatomy() {
        ExecutionResultSet ers = Executor.loadFromAnatomyResultsFolder("./src/test/resources/2016_anatomy/");
        EvaluatorMcNemarSignificance evaluator = new EvaluatorMcNemarSignificance(ers);
        File mcNemarBaseDirectory = new File("./mc_nemar_base_dir");
        mcNemarBaseDirectory.deleteOnExit();
        evaluator.writeResultsToDirectory(mcNemarBaseDirectory);
        checkFiles(mcNemarBaseDirectory, 1, 1);

        // null test (should fail gracefully)
        //evaluator.writeResultsToDirectory();
        deleteFile(mcNemarBaseDirectory);
    }

    /**
     * Simple test making sure that there is no error when writing and that something is written.
     * Uses the conference track (multiple test cases).
     */
    @Test
    void writeResultsToDirectoryConference() {
        ExecutionResultSet ers = Executor.loadFromConferenceResultsFolder("./src/test/resources" +
                "/2019_conference_shortened/");
        EvaluatorMcNemarSignificance evaluator = new EvaluatorMcNemarSignificance(ers);
        File mcNemarBaseDirectory = new File("./mc_nemar_base_dir_conference");
        mcNemarBaseDirectory.deleteOnExit();
        evaluator.writeResultsToDirectory(mcNemarBaseDirectory);
        checkFiles(mcNemarBaseDirectory, 21, 1);

        // null test (should fail gracefully)
        //evaluator.writeResultsToDirectory(null);
        deleteFile(mcNemarBaseDirectory);
    }

    @Test
    void writeResultsDirectoryCrossTrack() {
        ExecutionResultSet ers = new ExecutionResultSet();
        ers.addAll(Executor.loadFromAnatomyResultsFolder("./src/test/resources/2016_anatomy/"));
        ers.addAll(Executor.loadFromConferenceResultsFolder("./src/test/resources/2019_conference_shortened/"));
        File mcNemarBaseDirectory = new File("./mc_nemar_base_dir_cross_track");
        mcNemarBaseDirectory.deleteOnExit();
        EvaluatorMcNemarSignificance evaluatorMcNemarSignificance = new EvaluatorMcNemarSignificance(ers);
        evaluatorMcNemarSignificance.writeResultsToDirectory(mcNemarBaseDirectory);

        // overlap AML Lily
        List<String> crossTrackMcNemarAsymptotic = StringOperations.readListFromFile(new File(mcNemarBaseDirectory,
                EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TESTCASES_MC_NEMAR_ASYMPTOTIC));
        assertFalse(crossTrackMcNemarAsymptotic.get(0).contains("Test Case"));
        amlLilyCount(crossTrackMcNemarAsymptotic, 22);

        List<String> crossTrackMcNemarAsymptoticExactFallback =
                StringOperations.readListFromFile(new File(mcNemarBaseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TESTCASES_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK));
        assertFalse(crossTrackMcNemarAsymptoticExactFallback.get(0).contains("Test Case"));
        assertTrackFileIsNotUndecided(crossTrackMcNemarAsymptoticExactFallback);
        amlLilyCount(crossTrackMcNemarAsymptoticExactFallback, 22);

        List<String> crossTrackMcNemarAsymptoticCcorrection =
                StringOperations.readListFromFile(new File(mcNemarBaseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TESTCASES_MC_NEMAR_ASYMPTOTIC_CCORRECTION));
        assertFalse(crossTrackMcNemarAsymptoticCcorrection.get(0).contains("Test Case"));
        amlLilyCount(crossTrackMcNemarAsymptoticCcorrection, 22);

        List<String> crossTrackMcNemarAsymptoticCcorrectionExactFallback =
                StringOperations.readListFromFile(new File(mcNemarBaseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TESTCASES_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK));
        assertFalse(crossTrackMcNemarAsymptoticCcorrectionExactFallback.get(0).contains("Test Case"));
        assertTrackFileIsNotUndecided(crossTrackMcNemarAsymptoticCcorrectionExactFallback);
        amlLilyCount(crossTrackMcNemarAsymptoticCcorrectionExactFallback, 22);

        // check track aggregates
        List<String> aggregatedTracksMcNemarAsymptoticExactFallback =
                StringOperations.readListFromFile(new File(mcNemarBaseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TRACKS_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK));
        assertFalse(aggregatedTracksMcNemarAsymptoticExactFallback.get(0).contains("Test Case"));
        assertThatTotalSignificanceCountsAddUpTrackAggregates(aggregatedTracksMcNemarAsymptoticExactFallback, 2);

        List<String> aggregatedTracksMcNemarAsymptoticCcorrectionExactFallback =
                StringOperations.readListFromFile(new File(mcNemarBaseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TRACKS_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK));
        assertFalse(aggregatedTracksMcNemarAsymptoticCcorrectionExactFallback.get(0).contains("Test Case"));
        assertThatTotalSignificanceCountsAddUpTrackAggregates(aggregatedTracksMcNemarAsymptoticCcorrectionExactFallback, 2);

        List<String> aggregatedTracksMcNemarAsymptoticCcorrection =
                StringOperations.readListFromFile(new File(mcNemarBaseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TRACKS_MC_NEMAR_ASYMPTOTIC_CCORRECTION));
        assertFalse(aggregatedTracksMcNemarAsymptoticCcorrection.get(0).contains("Test Case"));
        assertThatTotalSignificanceCountsAddUpTrackAggregates(aggregatedTracksMcNemarAsymptoticCcorrection, 2);

        List<String> aggregatedTracksMcNemarAsymptotic =
                StringOperations.readListFromFile(new File(mcNemarBaseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TRACKS_MC_NEMAR_ASYMPTOTIC));
        assertFalse(aggregatedTracksMcNemarAsymptotic.get(0).contains("Test Case"));
        assertThatTotalSignificanceCountsAddUpTrackAggregates(aggregatedTracksMcNemarAsymptotic, 2);

        deleteFile(mcNemarBaseDirectory);
    }

    /**
     * asserts that AML and Lily have the stated significance count.
     * @param count Desired count.
     */
    static void amlLilyCount(List<String> content, int count){
        boolean found = false;
        for(String line : content){
            if(line.contains("AML") && line.contains("Lily")){
                String[] lineTokens = line.split(",");
                int significant = Integer.parseInt(lineTokens[lineTokens.length - 3]);
                int notSignificant = Integer.parseInt(lineTokens[lineTokens.length - 2]);
                int undefined = Integer.parseInt(lineTokens[lineTokens.length - 1]);
                assertEquals(count, significant + notSignificant + undefined);
                found = true;
            }
        }
        // make sure that we actually found the AML/Lily entry
        assertTrue(found);
    }

    /**
     * Re-usable test snippet.
     * The check covers:
     * <ul>
     *     <li>Check if correct number of files written.</li>
     *     <li>Check if track/testcase files have the correct header.</li>
     *     <li>Check if aggregated numbers are correct.</li>
     * </ul>
     *
     * @param baseDirectory     The base directory for which a check shall be executed.
     * @param numberOfTestCases The total number of test cases.
     * @param numberOfTracks The total number of tracks.
     */
    void checkFiles(File baseDirectory, int numberOfTestCases, int numberOfTracks) {
        assertTrue(baseDirectory.exists());

        // make sure that 16 files were written:
        assertEquals(16, Objects.requireNonNull(baseDirectory.listFiles()).length);

        List<String> trackMcNemarAsymptotic = StringOperations.readListFromFile(new File(baseDirectory,
                EvaluatorMcNemarSignificance.FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC));
        assertFalse(trackMcNemarAsymptotic.get(0).contains("Test Case"));
        assertThatTotalSignificanceCountsAddUpTestCaseAggregates(trackMcNemarAsymptotic, numberOfTestCases);

        List<String> testCaseMcNemarAsymptotic =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC));
        assertTrue(testCaseMcNemarAsymptotic.get(0).contains("Test Case"));

        List<String> trackMcNemarAsymptoticExactFallback =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK));
        assertFalse(trackMcNemarAsymptoticExactFallback.get(0).contains("Test Case"));
        assertTrackFileIsNotUndecided(trackMcNemarAsymptoticExactFallback);
        assertThatTotalSignificanceCountsAddUpTestCaseAggregates(trackMcNemarAsymptoticExactFallback, numberOfTestCases);

        List<String> testCaseMcNemarAsymptoticExactFallback =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK));
        assertTrue(testCaseMcNemarAsymptoticExactFallback.get(0).contains("Test Case"));
        assertTestCaseFileIsNotUndecided(testCaseMcNemarAsymptoticExactFallback);

        List<String> trackMcNemarAsymptoticCcorrection =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC_CCORRECTION));
        assertFalse(trackMcNemarAsymptoticCcorrection.get(0).contains("Test Case"));
        assertThatTotalSignificanceCountsAddUpTestCaseAggregates(trackMcNemarAsymptoticCcorrection, numberOfTestCases);

        List<String> testCaseMcNemarAsymptoticCcorrection =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC_CCORRECTION));
        assertTrue(testCaseMcNemarAsymptoticCcorrection.get(0).contains("Test Case"));

        List<String> trackMcNemarAsymptoticCcorrectionExactFallback =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TRACK_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK));
        assertFalse(trackMcNemarAsymptoticCcorrectionExactFallback.get(0).contains("Test Case"));
        assertTrackFileIsNotUndecided(trackMcNemarAsymptoticCcorrectionExactFallback);
        assertThatTotalSignificanceCountsAddUpTestCaseAggregates(trackMcNemarAsymptoticCcorrectionExactFallback, numberOfTestCases);

        List<String> testCaseMcNemarAsymptoticCcorrectionExactFallback =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_TEST_CASE_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK));
        assertTrue(testCaseMcNemarAsymptoticCcorrectionExactFallback.get(0).contains("Test Case"));
        assertTestCaseFileIsNotUndecided(testCaseMcNemarAsymptoticCcorrectionExactFallback);

        List<String> aggregatedTestCasesMcNemarAsymptotic = StringOperations.readListFromFile(new File(baseDirectory,
                EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TESTCASES_MC_NEMAR_ASYMPTOTIC));
        assertFalse(aggregatedTestCasesMcNemarAsymptotic.get(0).contains("Test Case"));
        assertThatTotalSignificanceCountsAddUpTestCaseAggregates(aggregatedTestCasesMcNemarAsymptotic, numberOfTestCases);

        List<String> aggregatedTestCasesMcNemarAsymptoticExactFallback =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TESTCASES_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK));
        assertFalse(aggregatedTestCasesMcNemarAsymptoticExactFallback.get(0).contains("Test Case"));
        assertTrackFileIsNotUndecided(aggregatedTestCasesMcNemarAsymptoticExactFallback);
        assertThatTotalSignificanceCountsAddUpTestCaseAggregates(aggregatedTestCasesMcNemarAsymptoticExactFallback, numberOfTestCases);

        List<String> aggregatedTestCasesMcNemarAsymptoticCcorrection =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TESTCASES_MC_NEMAR_ASYMPTOTIC_CCORRECTION));
        assertFalse(aggregatedTestCasesMcNemarAsymptoticCcorrection.get(0).contains("Test Case"));
        assertThatTotalSignificanceCountsAddUpTestCaseAggregates(aggregatedTestCasesMcNemarAsymptoticCcorrection, numberOfTestCases);

        List<String> aggregatedTestCasesMcNemarAsymptoticCcorrectionExactFallback =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TESTCASES_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK));
        assertFalse(aggregatedTestCasesMcNemarAsymptoticCcorrectionExactFallback.get(0).contains("Test Case"));
        assertTrackFileIsNotUndecided(aggregatedTestCasesMcNemarAsymptoticCcorrectionExactFallback);
        assertThatTotalSignificanceCountsAddUpTestCaseAggregates(aggregatedTestCasesMcNemarAsymptoticCcorrectionExactFallback, numberOfTestCases);

        List<String> aggregatedTracksMcNemarAsymptoticExactFallback =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TRACKS_MC_NEMAR_ASYMPTOTIC_EXACT_FALLBACK));
        assertFalse(aggregatedTracksMcNemarAsymptoticExactFallback.get(0).contains("Test Case"));
        assertThatTotalSignificanceCountsAddUpTrackAggregates(aggregatedTracksMcNemarAsymptoticExactFallback, numberOfTracks);

        List<String> aggregatedTracksMcNemarAsymptoticCcorrectionExactFallback =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TRACKS_MC_NEMAR_ASYMPTOTIC_CCORRECTION_EXACT_FALLBACK));
        assertFalse(aggregatedTracksMcNemarAsymptoticCcorrectionExactFallback.get(0).contains("Test Case"));
        assertThatTotalSignificanceCountsAddUpTrackAggregates(aggregatedTracksMcNemarAsymptoticCcorrectionExactFallback, numberOfTracks);

        List<String> aggregatedTracksMcNemarAsymptoticCcorrection =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TRACKS_MC_NEMAR_ASYMPTOTIC_CCORRECTION));
        assertFalse(aggregatedTracksMcNemarAsymptoticCcorrection.get(0).contains("Test Case"));
        assertThatTotalSignificanceCountsAddUpTrackAggregates(aggregatedTracksMcNemarAsymptoticCcorrection, numberOfTracks);

        List<String> aggregatedTracksMcNemarAsymptotic =
                StringOperations.readListFromFile(new File(baseDirectory,
                        EvaluatorMcNemarSignificance.FILE_NAME_AGGREGATED_TRACKS_MC_NEMAR_ASYMPTOTIC));
        assertFalse(aggregatedTracksMcNemarAsymptotic.get(0).contains("Test Case"));
        assertThatTotalSignificanceCountsAddUpTrackAggregates(aggregatedTracksMcNemarAsymptotic, numberOfTracks);
    }

    void assertThatTotalSignificanceCountsAddUpTrackAggregates(List<String> fileContent, int numberOfTracks){
        boolean firstLine = true;
        for (String line : fileContent) {
            if (firstLine) {
                firstLine = false;
                continue;
            }
            String[] lineTokens = line.split(",");
            int notSignificant = Integer.parseInt(lineTokens[lineTokens.length - 1]);
            int significant = Integer.parseInt(lineTokens[lineTokens.length - 2]);
            assertEquals(numberOfTracks, significant + notSignificant);
        }
    }

    /**
     * Make sure that the sum of significant, not significant, and undefined counts adds up to the total number of
     * test cases.
     *
     * @param fileContent       The file content.
     * @param numberOfTestCases The number of test cases.
     */
    void assertThatTotalSignificanceCountsAddUpTestCaseAggregates(List<String> fileContent, int numberOfTestCases) {
        boolean firstLine = true;
        for (String line : fileContent) {
            if (firstLine) {
                firstLine = false;
                continue;
            }
            String[] lineTokens = line.split(",");
            int significant = Integer.parseInt(lineTokens[lineTokens.length - 3]);
            int notSignificant = Integer.parseInt(lineTokens[lineTokens.length - 2]);
            int undefined = Integer.parseInt(lineTokens[lineTokens.length - 1]);
            assertEquals(numberOfTestCases, significant + notSignificant + undefined);
        }
    }

    /**
     * Check that the specified file content is always defined (must be true for fallback strategies).
     */
    void assertTestCaseFileIsNotUndecided(List<String> fileContent) {
        boolean firstLine = true;
        for (String line : fileContent) {
            if (firstLine) {
                firstLine = false;
                continue;
            }
            String significance = line.split(",")[line.split(",").length - 1];
            assertTrue(significance.equalsIgnoreCase("true") || significance.equalsIgnoreCase("false"));
        }
    }

    /**
     * Check that the specified file content is always defined (must be true for fallback strategies).
     */
    void assertTrackFileIsNotUndecided(List<String> fileContent) {
        boolean firstLine = true;
        for (String line : fileContent) {
            if (firstLine) {
                firstLine = false;
                continue;
            }
            String significance = line.split(",")[line.split(",").length - 1];
            assertTrue(significance.equalsIgnoreCase("0"));
        }
    }

    @Test
    void testApacheCommonsLibrary() {
        ChiSquaredDistribution d = new ChiSquaredDistribution(1);
        System.out.println(1.0 - d.cumulativeProbability(4));
    }

    @Test
    void nCr() {
        assertEquals(2, EvaluatorMcNemarSignificance.nCr(2, 1));
        assertEquals(3, EvaluatorMcNemarSignificance.nCr(3, 2));
    }

    @Test
    void getTrackSignificanceShare(){
        EvaluatorMcNemarSignificance evaluator = new EvaluatorMcNemarSignificance(null);
        assertTrue(evaluator.getTrackSignificanceShare() >= 0.0 &&
                evaluator.getTrackSignificanceShare() <= 1.0);
    }

    @Test
    void setTrackSignificanceShare(){
        EvaluatorMcNemarSignificance evaluator = new EvaluatorMcNemarSignificance(null);
        assertTrue(evaluator.getTrackSignificanceShare() > 0);
        evaluator.setTrackSignificanceShare(-0.5);
        assertTrue(evaluator.getTrackSignificanceShare() == EvaluatorMcNemarSignificance.DEFAULT_TRACK_SIGNIFICANCE_SHARE);
        evaluator.setTrackSignificanceShare(1.5);
        assertTrue(evaluator.getTrackSignificanceShare() == EvaluatorMcNemarSignificance.DEFAULT_TRACK_SIGNIFICANCE_SHARE);
        evaluator.setTrackSignificanceShare(0.75);
        assertEquals(0.75, evaluator.getTrackSignificanceShare());
    }

}