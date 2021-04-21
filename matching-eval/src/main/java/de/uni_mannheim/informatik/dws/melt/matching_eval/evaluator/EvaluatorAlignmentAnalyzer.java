package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.alignmentanalyzer.AlignmentAnalyzerMetric;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.alignmentanalyzer.AlignmentAnalyzerResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates the alignments (min/max confidence, type of relations, correct positions of uris etc)
 * and writes the output to the results folder.
 */
public class EvaluatorAlignmentAnalyzer extends Evaluator {


    private static Logger LOGGER = LoggerFactory.getLogger(EvaluatorAlignmentAnalyzer.class);
    
    /**
     * Constructor
     * @param results the execution result set
     */
    public EvaluatorAlignmentAnalyzer(ExecutionResultSet results) {
        super(results);
    }
    
    @Override
    protected void writeResultsToDirectory(File baseDirectory) {        
        //write file for each macther and testcase
        AlignmentAnalyzerMetric metric = new AlignmentAnalyzerMetric();
        for (ExecutionResult r : this.results) {
            AlignmentAnalyzerResult analyzerResult = metric.compute(r);
            File analyzeFile = new File(getResultsFolderTrackTestcaseMatcher(baseDirectory, r), "alignmentAnalysis.txt");
            analyzeFile.getParentFile().mkdirs();
            try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(analyzeFile), StandardCharsets.UTF_8))){
                out.write(analyzerResult.getReportForAlignment());
            } catch (IOException ex) {
                LOGGER.warn("Could not write alignmentAnalysis.txt", ex);
            }
        }
        //write overview file
        if(!this.results.isEmpty())
            AlignmentAnalyzerMetric.writeAnalysisFile(results, new File(baseDirectory, "alignmentAnalysisOverview.csv"));
    }
}
