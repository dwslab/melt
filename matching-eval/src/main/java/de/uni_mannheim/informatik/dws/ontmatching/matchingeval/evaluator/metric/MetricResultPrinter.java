package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import java.io.Writer;
import java.util.Map;

public interface MetricResultPrinter <MetricResult> {
    public void write(Map<ExecutionResult, MetricResult> results, Writer writer);
}
