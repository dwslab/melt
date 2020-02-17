package de.uni_mannheim.informatik.dws.melt.matching_eval.refinement;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;

/**
 * A refinement operation.
 * Have to override equals and hash if it uses some attributes.
 * @author Sven Hertling
 */
public interface Refiner {
    
    /**
     * Create a new refined ExecutionResult from an ExecutionResult. Examples are: only class matches, only non trivial matches etc.
     * @param toBeRefined ExecutionResult which should be refined
     * @return a new ExecutionResult which is refined
     */
    ExecutionResult refine(ExecutionResult toBeRefined);
}
