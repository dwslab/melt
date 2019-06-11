package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.visualization;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.Executor;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.cm.ConfusionMatrixMetric;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.resultsSimilarity.MatcherSimilarity;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.resultsSimilarity.MatcherSimilarityMetric;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This writer can persist {@link MatcherSimilarity} objects in a LaTex graph from the perspective of one particular matcher.
 *
 * @author Sven Hertling
 */
public class MatcherSimilarityLatexPlotWriter {


    // exemplary usage
    /*
    public static void main(String[] args) {
        final String pathToAnatomyResultFiles = "C:\\...\\results";
        final TestCase testCase = TrackRepository.Anatomy.Default.getTestCases().get(0);

        //----------------------------
        // Evaluation
        //----------------------------

        ExecutionResultSet executionResultSet = Executor.loadFromFolder(pathToAnatomyResultFiles, testCase);
        MatcherSimilarityMetric metric = new MatcherSimilarityMetric();
        MatcherSimilarity similarity = metric.get(executionResultSet, testCase);

        try (PrintWriter w = new PrintWriter("basematcher.tex")) {
            write(new ConfusionMatrixMetric(), similarity, w);
        } catch (FileNotFoundException ex) {
        }
    }
    */


    /**
     * Persists a matcher similarity instance as LaTex graph from the perspective of one particular matcher.
     * @param similarityResultInstance Similarity instance that shall be persisted.
     * @param writer The writer that shall be used to persist the results.
     */
    public static void write(MatcherSimilarity similarityResultInstance, PrintWriter writer) {
        write(new ConfusionMatrixMetric(), similarityResultInstance, writer);
    }

    /**
     * Persists a matcher similarity instance as LaTex graph from the perspective of one particular matcher.
     * @param cm You can use this parameter to use a previously calculated confusion matrix in order to save execution time..
     * @param similarityResultInstance Similarity instance that shall be persisted.
     * @param writer The writer that shall be used to persist the results.
     */
    public static void write(ConfusionMatrixMetric cm, MatcherSimilarity similarityResultInstance, PrintWriter writer) {
        writer.println("\\documentclass{article}");
        writer.println("\\usepackage{pgfplots}");

        writer.println("\\begin{document}");
        writer.println("\\begin{figure}");
        writer.println("    \\centering");
        writer.println("    \\begin{tikzpicture}");
        writer.println("    \\begin{axis}[");
        writer.println("    ylabel={$F_1$ measure},");
        writer.println("    xlabel={MAD of the Jaccard Similarity}");
        writer.println("    ]");

        writer.println("\\addplot[color=black, only marks, mark=*,text mark as node=true,point meta=explicit symbolic,nodes near coords, nodes near coords style={font=\\tiny}, mark options={scale=0.8}] coordinates { ");
        //List<String> names = new ArrayList();
        
        Map<String, List<Double>> matcherToSimilarities = new HashMap<>();
        
        for(ExecutionResult outer : similarityResultInstance.getExecutionResultsAsList()){
            List<Double> similarities = new ArrayList();
            for(ExecutionResult inner : similarityResultInstance.getExecutionResultsAsList()){
                if(inner != outer){
                    similarities.add(similarityResultInstance.getMatcherSimilarity(outer, inner));
                }                
            }
            //double d = mean(similarities);
            double d = meanAbsoluteDeviation(similarities);
            System.out.println(outer.getMatcherName() + " -> " + d);
            writer.println("(" + d + "," + cm.get(outer).getF1measure() + ") [" + outer.getMatcherName() + "]");
        }
        writer.println("};");
        writer.println("    \\end{axis}");
        writer.println("    \\end{tikzpicture}");
        writer.println("    \\caption{Matcher comparison using MAD}");
        writer.println("    \\label{comp}");
        writer.println("\\end{figure}");
        writer.println("\\end{document}");
        writer.flush();
    }


    /**
     * Helper function which calculates the mean.
     * @param numbers Numbers for which the mean shall be calculated.
     * @return Mean as double.
     */
    private static double mean(List<Double> numbers) {  
        double sum = 0;
        for(Double d: numbers){
            sum += d;
        }
        return sum / numbers.size();        
    }


    /**
     * Helper function to calculate the <a href="https://en.wikipedia.org/wiki/Average_absolute_deviation"> Mean Absolute Deviation (MAD)</a>.
     * @param numbers The numbers for which the MAD shall be calculated.
     * @return MAD as double
     */
    private static double meanAbsoluteDeviation(List<Double> numbers) {
        int n = numbers.size();
        double mean = mean(numbers);
        
        double mad = 0;
        for(Double d : numbers){
            mad += Math.abs(d - mean);
        }
        return mad / n;
    } 

}
