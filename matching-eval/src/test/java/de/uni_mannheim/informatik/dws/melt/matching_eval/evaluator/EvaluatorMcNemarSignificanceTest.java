package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
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
                EvaluatorMcNemarSignificance.TestType.ASYMPTOTIC_TEST_WITH_CONTINUITY_CORRECTION);
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
     * Does not check the contents that are written.
     */
    @Test
    void writeResultsToDirectory(){
        ExecutionResultSet ers = Executor.loadFromAnatomyResultsFolder("./src/test/resources/2016_anatomy/");
        EvaluatorMcNemarSignificance evaluator = new EvaluatorMcNemarSignificance(ers);
        File mcNemarBaseDirectory = new File("./mc_nemar_base_dir");
        mcNemarBaseDirectory.deleteOnExit();
        evaluator.writeResultsToDirectory(mcNemarBaseDirectory);
        assertTrue(mcNemarBaseDirectory.exists());
        assertEquals(4, mcNemarBaseDirectory.listFiles().length);

        // null test (should fail gracefully)
        evaluator.writeResultsToDirectory(null);
    }

    @Test
    void testApacheCommonsLibrary(){
        ChiSquaredDistribution d = new ChiSquaredDistribution(1);
        System.out.println(1.0 - d.cumulativeProbability(4));
    }

}