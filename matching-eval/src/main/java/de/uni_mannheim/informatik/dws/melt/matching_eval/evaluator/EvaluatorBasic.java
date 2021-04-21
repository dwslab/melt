package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrix;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrixMetric;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A basic evaluator that is easy on Memory and prints the performance results per test case in CSV format.
 * No track aggregation is performed. If you are interested in an in-depth analysis (and if you have enough memory), you should use
 * {@link EvaluatorCSV}.
 */
public class EvaluatorBasic extends Evaluator {


    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorBasic.class);

    /**
     * Name of the file that will be written to the base directory.
     */
    private static final String RESULT_FILE_NAME = "resultsEvaluatorBasic.csv";

    /**
     * The alignment extensions that are to be printed.
     */
    private ArrayList<String> alignmentExtensions;

    /**
     * If true: Alignment extensions are printed in CSV.
     */
    private boolean isPrintAlignmentExtensions = true;
    
    /**
     * the ConfusionMatrixMetric to be used for evaluation
     */
    private ConfusionMatrixMetric metric;

    /**
     * Constructor.
     *
     * @param results The results of the matching process that shall be evaluated.
     */
    public EvaluatorBasic(ExecutionResultSet results) {
        this(results, new ConfusionMatrixMetric());
    }
    
    /**
     * Constructor.
     * @param results The results of the matching process that shall be evaluated.
     * @param metric The ConfusionMatrixMetric to be used for evaluation
     */
    public EvaluatorBasic(ExecutionResultSet results, ConfusionMatrixMetric metric) {
        super(results);
        
        this.metric = metric;
        // alignment extensions to be printed
        this.alignmentExtensions = getAlignmentExtensions(results);
    }

    @Override
    public void writeResultsToDirectory(File baseDirectory) {
        try {
            if(!baseDirectory.exists()){
                baseDirectory.mkdirs();
            } else if (baseDirectory.isFile()) {
                LOGGER.error("The baseDirectory needs to be a directory, not a file. ABORTING writing process.");
                return;
            }

            File fileToBeWritten = new File(baseDirectory, RESULT_FILE_NAME);
            CSVPrinter printer = CSVFormat.DEFAULT.print(fileToBeWritten, StandardCharsets.UTF_8);
            printer.printRecord(getHeader());
            for (ExecutionResult er : results) {
                String[] extensionValues;
                if(isPrintAlignmentExtensions && this.alignmentExtensions != null && this.alignmentExtensions.size() > 0) {
                    Map<String, String> alignmentExtensions = er.getSystemAlignment().getExtensions();
                    extensionValues = determineExtensionValuesToWriteForCSV(alignmentExtensions);
                } else extensionValues = new String[0];
                ConfusionMatrix matrix = this.metric.compute(er);
                printer.printRecord(toStringArrayWithArrayAtTheEnd(extensionValues, er.getTestCase().getTrack().getName(), er.getTestCase().getName(), er.getMatcherName(), matrix.getPrecision(),
                        matrix.getRecall(),matrix.getF1measure(), matrix.getTruePositiveSize(),
                        matrix.getFalsePositiveSize(), matrix.getFalseNegativeSize(), er.getRuntime()));
            }
            printer.flush();
            printer.close();
        } catch (IOException ioe){
            LOGGER.error("Problem with results writer.", ioe);
        }
    }

    /**
     * Get the header row for the results.
     *
     * @return Header row in the form of a String-List.
     */
    private List<String> getHeader() {
        List<String> result = new ArrayList<>();
        result.addAll(Arrays.asList("Track", "Test Case", "Matcher", "Precision", "Recall", "F1", "TP Size",
                "FP Size", "FN Size", "Runtime"));
        if(isPrintAlignmentExtensions) {
            for (String extensionUri : this.alignmentExtensions) {
                result.add(extensionUri);
            }
        }
        return result;
    }

    /**
     * This method determines the unique {@link Alignment}
     * extensions that are used in the alignments in the ExecutionResultSet.
     * @param results The result set of which
     * @return A list of unique alignment extensions that are used.
     */
    private ArrayList<String> getAlignmentExtensions(ExecutionResultSet results) {
        HashSet<String> uniqueExtensions = new HashSet<>();

        // go over all matches and check for extensions; many null checks to avoid null pointer exceptions.
        if(results != null) {
            for (ExecutionResult result : results) {
                if(result != null && result.getSystemAlignment() != null) {
                    Map<String, String> extensions = result.getSystemAlignment().getExtensions();
                    if (extensions != null) {
                        uniqueExtensions.addAll(extensions.keySet());
                    }
                }
            }
        }
        return new ArrayList<>(uniqueExtensions);
    }

    /**
     * Creates one string array where the {@code putAtTheEnd} values are arranged at the end of the string.
     * @param putAtTheEnd To be put at the end.
     * @param individualValues Some String values.
     * @return One String array where first values from {@code individualValues} and then the values from {@code putAtTheEnd}
     * are appearing.
     */
    String[] toStringArrayWithArrayAtTheEnd(String[] putAtTheEnd, Object... individualValues){
        String[] result = new String[individualValues.length + putAtTheEnd.length];
        int i = 0;
        for(; i < individualValues.length; i++){
            result[i] = "" + individualValues[i];
        }
        for(int newI = 0; i + newI < result.length; newI++){
            result[i + newI] = putAtTheEnd[newI];
        }
        return result;
    }

    /**
     * Given the existing extension values of an alignment, determine what to write in the CSV file.
     * @param existingExtensionValues The existing extension values in the alignment.
     * @return Tokenized extension values in the correct order for the CSV file to print.
     */
    private String[] determineExtensionValuesToWriteForCSV(Map<String,String> existingExtensionValues){
        String[] result = new String[this.alignmentExtensions.size()];
        for(int i = 0; i < this.alignmentExtensions.size(); i++){
            String extensionUri = alignmentExtensions.get(i);
            if(existingExtensionValues.containsKey(extensionUri)) {
                result[i] = existingExtensionValues.get((String) extensionUri);
            } else {
                result[i] = "-";
            }
        }
        return result;
    }

    public static String getResultFileName() {
        return RESULT_FILE_NAME;
    }

    public boolean isPrintAlignmentExtensions() {
        return isPrintAlignmentExtensions;
    }

    public void setPrintAlignmentExtensions(boolean printAlignmentExtensions) {
        if(printAlignmentExtensions){
            this.alignmentExtensions = getAlignmentExtensions(results);
        }
        isPrintAlignmentExtensions = printAlignmentExtensions;
    }
}
