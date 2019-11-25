package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.cm.ConfusionMatrix;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.cm.ConfusionMatrixMetric;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A basic evaluator that is easy on Memory.
 * If you are interested in an in-depth analysis (and if you have enough memory), you should use
 * EvaluatorCSV.
 */
public class EvaluatorBasic extends Evaluator {

    /**
     * Default logger.
     */
    private static Logger LOGGER = LoggerFactory.getLogger(EvaluatorBasic.class);

    /**
     * Name of the file that will be written to the base directory.
     */
    private static final String RESULT_FILE_NAME = "resultsEvaluatorBasic.csv";

    /**
     * Constructor.
     *
     * @param results The results of the matching process that shall be evaluated.
     */
    public EvaluatorBasic(ExecutionResultSet results) {
        super(results);
    }

    @Override
    public void writeToDirectory(File baseDirectory) {
        try {
            File fileToBeWritten = new File(baseDirectory, RESULT_FILE_NAME);
            CSVPrinter printer = new CSVPrinter(new FileWriter(fileToBeWritten, false), CSVFormat.DEFAULT);
            ConfusionMatrixMetric metric = new ConfusionMatrixMetric();
            printer.printRecord("Track", "Test Case", "Matcher", "Precision", "Recall", "F1", "TP Size",
                    "FP Size", "FN Size", "Runtime");
            for (ExecutionResult er : results) {
                ConfusionMatrix matrix = metric.compute(er);
                printer.printRecord(er.getTestCase().getTrack().getName(), er.getTestCase().getName(), er.getMatcher(), matrix.getPrecision(),
                        matrix.getRecall(),matrix.getF1measure(), matrix.getTruePositiveSize(),
                        matrix.getFalsePositiveSize(), matrix.getFalseNegativeSize(), er.getRuntime());
            }
            printer.flush();
            printer.close();
        } catch (IOException ioe){
            LOGGER.error("Problem with results writer.", ioe);
        }
    }

    public static String getResultFileName() {
        return RESULT_FILE_NAME;
    }
}
