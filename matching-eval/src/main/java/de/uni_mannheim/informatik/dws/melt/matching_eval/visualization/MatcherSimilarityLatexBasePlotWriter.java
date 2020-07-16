package de.uni_mannheim.informatik.dws.melt.matching_eval.visualization;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrixMetric;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.resultsSimilarity.ExecutionResultTuple;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.resultsSimilarity.MatcherSimilarity;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.resultsSimilarity.MatcherSimilarityMetric;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;

import java.io.FileNotFoundException;
import java.io.PrintWriter;


import java.util.Map.Entry;

/**
 * This writer can persist {@link MatcherSimilarity} objects in a LaTex graph from the perspective of one particular matcher.
 */
public class MatcherSimilarityLatexBasePlotWriter {

    /**
     * Persists a matcher similarity instance as LaTex graph from the perspective of one particular matcher.
     * @param baseMatcher Name of the base matcher that shall be used for the calculation.
     * @param similarityResultInstance Similarity instance that shall be persisted.
     * @param writer The writer that shall be used to persist the results.
     */
    public static void write(String baseMatcher, MatcherSimilarity similarityResultInstance, PrintWriter writer) {
        write(baseMatcher, new ConfusionMatrixMetric(), similarityResultInstance, writer);
    }

    /**
     * Persists a matcher similarity instance as LaTex graph from the perspective of one particular matcher.
     * @param baseMatcherName Name of the base matcher that shall be used for the calculation.
     * @param cm You can use this parameter to use a previously calculated confusion matrix in order to save execution time..
     * @param similarityResultInstance Similarity instance that shall be persisted.
     * @param writer The writer that shall be used to persist the results.
     */
    public static void write(String baseMatcherName, ConfusionMatrixMetric cm, MatcherSimilarity similarityResultInstance, PrintWriter writer) {
        writer.println("\\documentclass{article}");
        writer.println("\\usepackage{pgfplots}");

        writer.println("\\begin{document}");
        writer.println("\\begin{figure}");
        writer.println("    \\centering");
        writer.println("    \\begin{tikzpicture}");
        writer.println("    \\begin{axis}[");
        writer.println("    ylabel={F1 measure},");
        writer.println("    xlabel={jaccard similarity}");
        writer.println("    ]");

        writer.println("\\addplot[color=black, only marks, mark=*,text mark as node=true,point meta=explicit symbolic,nodes near coords, nodes near coords style={font=\\tiny}, mark options={scale=0.8}] coordinates { ");
        //List<String> names = new ArrayList();
        for (Entry<ExecutionResultTuple, Double> entry : similarityResultInstance.getMatcherSimilaritySet().entrySet()) {
            ExecutionResult resultOne = entry.getKey().result1;
            ExecutionResult resultTwo = entry.getKey().result2;
            if (resultOne.getMatcherName().equals(baseMatcherName)) {
                double f1 = cm.get(resultTwo).getF1measure();
                writer.println("(" + entry.getValue() + "," + f1 + ") [" + resultTwo.getMatcherName() + "]");
                //writer.println("\\addplot coordinates { (" + entry.getValue() + "," + f1 + ")};");
                //names.add(resultTwo.getMatcherName());
            } else if (resultTwo.getMatcherName().equals(baseMatcherName)) {
                double f1 = cm.get(resultOne).getF1measure();
                writer.println("(" + entry.getValue() + "," + f1 + ") [" + resultOne.getMatcherName() + "]");
                //writer.println("\\addplot coordinates { (" + entry.getValue() + "," + f1 + ")};");
                //names.add(resultOne.getMatcherName());
            }
        }
        //writer.println("\\legend{" + String.join(",", names) + "}");
        writer.println("};");
        writer.println("    \\end{axis}");
        writer.println("    \\end{tikzpicture}");
        writer.println("    \\caption{Comparison of " + baseMatcherName + " to other matchers.}");
        writer.println("    \\label{com_" + baseMatcherName + "}");
        writer.println("\\end{figure}");
        writer.println("\\end{document}");
        writer.flush();
    }

}
