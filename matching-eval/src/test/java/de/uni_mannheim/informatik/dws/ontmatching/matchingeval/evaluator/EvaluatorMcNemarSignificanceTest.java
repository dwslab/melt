package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.Executor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorMcNemarSignificanceTest {

    @Test
    @EnabledOnOs( { OS.MAC } )
    void calculateSignificance() {
        ExecutionResultSet ers = Executor.loadFromAnatomyResultsFolder("./src/test/resources/2016_anatomy/");
        EvaluatorMcNemarSignificance evaluator = new EvaluatorMcNemarSignificance(ers);
        HashMap<EvaluatorMcNemarSignificance.McNemarIndividualResult, String> result = evaluator.calculateSignificance(EvaluatorMcNemarSignificance.AlphaValue.ZERO_POINT_ZERO_FIVE, EvaluatorMcNemarSignificance.TestType.ASYMPTOTIC_TEST_WITH_CONTINUITY_CORRECTION);
        for(Map.Entry<EvaluatorMcNemarSignificance.McNemarIndividualResult, String> entry : result.entrySet()) {
            EvaluatorMcNemarSignificance.McNemarIndividualResult individualResult = entry.getKey();
            if (individualResult.matcherName1.equals(individualResult.matcherName2)) {
                assertEquals(entry.getValue().toLowerCase(), "false");
            } else if (individualResult.matcherName1.equals("AML") || individualResult.matcherName2.equals("AML")) {
                assertEquals(entry.getValue().toLowerCase(), "true", "Error for set: " + individualResult.matcherName1 + "  ||  " + individualResult.matcherName2);
            }
        }
        evaluator.writeToDirectory();
    }
}