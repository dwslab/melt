package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.resultsSimilarity;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;

/**
 * Data structure to hold two {@link ExecutionResult} instances.
 * Two ExecutionResultTuple instances are considered equal if they hold the same {@link ExecutionResult} instances
 * regardless in which order.
 */
public class ExecutionResultTuple {

    public ExecutionResult result1;
    public ExecutionResult result2;

    public ExecutionResultTuple(ExecutionResult result1, ExecutionResult result2) {
        this.result1 = result1;
        this.result2 = result2;
    }

    @Override
    public int hashCode(){
        return 34 + result1.getMatcherName().hashCode() + result2.getMatcherName().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (object.getClass() != this.getClass()) return false;
        ExecutionResultTuple castedObject = (ExecutionResultTuple) object;
        if (
                (castedObject.result1.equals(this.result1) || castedObject.result1.equals(this.result2))
                        && (castedObject.result2.equals(this.result1) || castedObject.result2.equals(this.result2))
        ) return true;
        return false;
    }

}
