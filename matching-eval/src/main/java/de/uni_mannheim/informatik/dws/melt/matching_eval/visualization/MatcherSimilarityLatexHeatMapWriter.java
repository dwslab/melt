package de.uni_mannheim.informatik.dws.melt.matching_eval.visualization;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.resultsSimilarity.ExecutionResultTuple;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.resultsSimilarity.MatcherSimilarity;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This writer can persist {@link MatcherSimilarity} objects in a LaTex Heat Map.
 *
 * @author Sven Hertling
 */
public class MatcherSimilarityLatexHeatMapWriter {


    // Exemplary Usage
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

        try (PrintWriter w = new PrintWriter("heatmap.tex")) {
            write(similarity, w);
        } catch (FileNotFoundException ex) {
        }
    }
     */


    /**
     * Write LaTex Heatmap of {@link MatcherSimilarity} using the given writer.
     *
     * @param similarityResultInstance Similarity instance that shall be written.
     * @param writer                   Writer that shall be used to write heat map.
     */
    public static void write(MatcherSimilarity similarityResultInstance, PrintWriter writer) {
        //https://tex.stackexchange.com/questions/42444/parametrize-shading-in-table-through-tikz
        //https://texblog.org/2013/06/13/latex-heatmap-using-tabular/

        //https://stackoverflow.com/questions/22187655/design-pattern-for-different-kinds-of-format
        //https://dzone.com/articles/design-patterns-strategy

        HashMap<ExecutionResultTuple, Double> matcherSimilaritySet = similarityResultInstance.getMatcherSimilaritySet();

        // transform to list to ensure constant order
        //ArrayList<ExecutionResult> executionResults = new ArrayList<>();
        //executionResults.addAll(originalExecutionResultSet);
        ArrayList<ExecutionResult> executionResults = similarityResultInstance.getExecutionResultsAsList();


        writer.println("\\documentclass{article}");
        writer.println("\\usepackage[table]{xcolor}");
        writer.println("\\usepackage{pgf}");

        writer.println("\\newcommand\\ColCell[1]{");
        writer.println("	\\pgfmathparse{#1<0.5?1:0}\\ifnum\\pgfmathresult=0\\relax\\color{white}\\fi");
        writer.println("	\\pgfmathsetmacro\\compC{1-#1}");
        writer.println("	\\edef\\x{\\noexpand\\cellcolor[hsb]{0,0,\\compC}}\\x #1");//{\tiny #1}
        writer.println("}%inspired by https://tex.stackexchange.com/questions/42444/parametrize-shading-in-table-through-tikz");

        //rotate: https://tex.stackexchange.com/questions/32683/rotated-column-titles-in-tabular
        //use adjustbox for rotating:
        writer.println("\\usepackage{adjustbox}");
        writer.println("\\newcommand{\\rot}[1]{\\multicolumn{1}{c}{\\adjustbox{angle=60,lap=\\width-1em}{#1}}}");

        //or use rotating ( doesn't leave space for the titles )
        //writer.println("\\usepackage{rotating}");
        //writer.println("\\newcommand{\\rot}[1]{\\begin{rotate}{60}#1\\end{rotate}}");

        writer.println("\\begin{document}");

        writer.println("\\begin{table}");
        writer.println("    \\caption{Matcher Similarity}");
        writer.println("    \\label{tbl:matcherSim}");
        writer.println("    \\centering");
        writer.println("    \\resizebox{\\textwidth}{!}{"); //possibly resize whole tabular
        writer.println("    \\begin{tabular}{r|*{" + executionResults.size() + "}{c}}");
        //writer.println("    \\begin{tabular}{r|" + StringUtils.repeat("c", executionResults.size()) + "}");

        //header
        for (ExecutionResult executionResultTop : executionResults) {
            writer.print("& \\rot{" + executionResultTop.getMatcherName() + "}");
        }
        writer.println("\\\\ \\hline");

        //lines
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatSymbols.setGroupingSeparator(',');
        DecimalFormat df2 = new DecimalFormat("#.##");
        df2.setDecimalFormatSymbols(decimalFormatSymbols);

        for (ExecutionResult executionResultOuter : executionResults) {
            writer.print(executionResultOuter.getMatcherName());
            for (ExecutionResult executionResultInner : executionResults) {
                Double similarity = matcherSimilaritySet.get(new ExecutionResultTuple(executionResultOuter, executionResultInner));
                //writer.print("&");
                //writer.print("& \\ColCell{.28}");
                writer.print("& \\ColCell{" + df2.format(similarity) + "}");
            }
            writer.println("\\\\");
        }
        writer.println("\\hline");

        writer.println("        \\end{tabular}");
        writer.println("}");//from resize above
        writer.println("\\end{table}");
        writer.println("\\end{document}");
        writer.flush();
    }

}
