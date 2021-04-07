package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class which represents a metric.
 * @author Sven Hertling, Jan Portisch
 */
public abstract class Metric<MetricResult> {


    protected Map<ExecutionResult, MetricResult> cache = new HashMap<>();
        
    /**
     * Triggers the computation of the metric for an individual execution result.
     * The metric should always use the {@link ExecutionResult#getSystemAlignment()} and {@link ExecutionResult#getReferenceAlignment()} methods.
     * @param executionResult Execution result for which the calculation shall be performed.
     * @return The metric result.
     */
    public MetricResult get(ExecutionResult executionResult){
        MetricResult result = cache.get(executionResult);
        if(result == null){
            result = compute(executionResult);
            this.cache.put(executionResult, result);
        }
        return result;
    }
    
    protected abstract MetricResult compute(ExecutionResult executionResult);
}
