package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;

import java.io.Writer;
import java.util.Map;

public interface MetricResultPrinter <MetricResult> {


    void write(Map<ExecutionResult, MetricResult> results, Writer writer);
}
