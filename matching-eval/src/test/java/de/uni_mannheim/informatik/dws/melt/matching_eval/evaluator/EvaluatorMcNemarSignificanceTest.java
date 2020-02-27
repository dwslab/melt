package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorMcNemarSignificanceTest {

    @Test
    //@EnabledOnOs( { OS.MAC } )
    void calculateSignificance() {
        ExecutionResultSet ers = Executor.loadFromAnatomyResultsFolder("./src/test/resources/2016_anatomy/");
        EvaluatorMcNemarSignificance evaluator = new EvaluatorMcNemarSignificance(ers);
        HashMap<EvaluatorMcNemarSignificance.McNemarIndividualResult, Double> result = evaluator.calculatePvalues(0.05, EvaluatorMcNemarSignificance.TestType.ASYMPTOTIC_TEST_WITH_CONTINUITY_CORRECTION);
        for(Map.Entry<EvaluatorMcNemarSignificance.McNemarIndividualResult, Double> entry : result.entrySet()) {
            EvaluatorMcNemarSignificance.McNemarIndividualResult individualResult = entry.getKey();
            if (individualResult.matcherName1.equals(individualResult.matcherName2)) {
                assertTrue(entry.getValue() > individualResult.alpha);
            } else if (individualResult.matcherName1.equals("AML") || individualResult.matcherName2.equals("AML")) {
                assertTrue(entry.getValue().doubleValue() < individualResult.alpha, "Error for set: " + individualResult.matcherName1 + "  ||  " + individualResult.matcherName2);
            }
        }
    }

    @Test
    void testApacheCommonsLibrary(){
        ChiSquaredDistribution d = new ChiSquaredDistribution(1);
        System.out.println(1.0 - d.cumulativeProbability(4));
    }

}