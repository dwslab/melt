package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
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
     * The metic should always use the {@link ExecutionResult#getSystemAlignment()} and {@link ExecutionResult#getReferenceAlignment()} methods.
     * @param executionResult Execution result for which the calculation shall be performed.
     * @return The metric result.
     */
    public MetricResult get(ExecutionResult executionResult){
        MetricResult r = cache.get(executionResult);
        if(r == null){
            r = compute(executionResult);
            this.cache.put(executionResult, r);
        }
        return r;
    }
    
    protected abstract MetricResult compute(ExecutionResult executionResult);
}
