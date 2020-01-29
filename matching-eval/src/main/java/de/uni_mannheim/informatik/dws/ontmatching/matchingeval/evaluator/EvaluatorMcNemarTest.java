package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;

import java.io.File;

/**
 * Implementation of a significance test according to
 * <a href="https://dl.acm.org/doi/pdf/10.1145/3193573">Mohammadi, Majid; Atashin, Amir Ahooye;
 * Hofman, Wout; Tan, Yaohua. Comparison of Ontology
 * Alignment Systems Across Single Matching Task Via the McNemar's Test. 2018.</a>.
 */
public class EvaluatorMcNemarTest extends Evaluator {
    /**
     * Constructor.
     *
     * @param results The results of the matching process.
     */
    public EvaluatorMcNemarTest(ExecutionResultSet results) {
        super(results);
    }

    @Override
    public void writeToDirectory(File baseDirectory) {
        for (ExecutionResult result1 : results) {
            for (ExecutionResult result2 : results){
                if(result1.getTestCase().getName().equals(result2.getTestCase().getName())){
                    isSignificant(result1, result2);
                }
            }
        }
    }

    /**
     * Given two execution results, it is determined whether the two results are significantly different.
     * @param executionResult1
     * @param executionResult2
     * @return True if significant, else false.
     */
    private boolean isSignificant(ExecutionResult executionResult1, ExecutionResult executionResult2){
        // TODO implement
        return false;
    }

}
