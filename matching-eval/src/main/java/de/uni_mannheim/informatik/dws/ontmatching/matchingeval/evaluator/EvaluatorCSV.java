package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.IExplainerResource;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ResourceType;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.baselineMatchers.BaselineStringMatcher;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.explainer.ExplainerResourceProperty;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.cm.ConfusionMatrix;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.cm.ConfusionMatrixMetric;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util.AlignmentsCube;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util.AnalyticalAlignmentInformation;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util.EvaluatorUtil;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.refinement.ResidualRefiner;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.refinement.TypeRefiner;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This evaluator is capable of persisting the results of the matching process in a CSV file (which can be consumed
 * in excel, for example).
 * It divides mappings into certain groups, namely: classes, properties, instances, and allConfusionMatrix.
 *
 * @author Jan Portisch
 */
public class EvaluatorCSV extends Evaluator {

    /**
     * If true: system alignments will be copied to the evaluation directories.
     * Default: true
     */
    private boolean copyAlignmentFiles = true;

    /**
     * Baseline matcher for residual results.
     */
    private IOntologyMatchingToolBridge baselineMatcher = new BaselineStringMatcher();

    /**
     * Default Logger
     */
    private Logger LOGGER = LoggerFactory.getLogger(EvaluatorCSV.class);

    private ConfusionMatrixMetric confusionMatrixMetric;
    private TypeRefiner classRefiner;
    private TypeRefiner propertyRefiner;
    private TypeRefiner instanceRefiner;
    private ResidualRefiner residualRefiner;

    /**
     * Indicates whether the CSV shall be printed with shortened strings. Default: true.
     */
    private boolean isPrintAsShortenedString = true;

    /**
     * Analytical Store for all alignments.
     */
    AlignmentsCube alignmentsCube;

    /**
     * Printer which can be used to print an individual matcher performance for a test case
     * (e.g. precision, recall, f1).
     */
    private CSVPrinter testCasePerformanceCubePrinter;

    /**
     * The explainers to be used in the CSV that will be written.
     */
    List<IExplainerResource> resourceExplainers;

    /**
     * Printer which can be used to print an individual matcher performance for a track
     * (e.g. macro-precision, micro-precision).
     */
    private CSVPrinter trackPerformanceCubePrinter;


    /**
     * Constructor
     *
     * @param results The execution results for which an evaluation shall be performed.
     * @param metric The confusion matrix metric to be used.
     * @param isPrintAsShortenedString The CSV output will be written with shortened URIs.
     */
    public EvaluatorCSV(ExecutionResultSet results, ConfusionMatrixMetric metric, boolean isPrintAsShortenedString) {
        super(results);

        // output configuration
        this.isPrintAsShortenedString = isPrintAsShortenedString;

        // metrics
        this.confusionMatrixMetric = metric;

        // refiners
        classRefiner = new TypeRefiner(ResourceType.CLASS);
        propertyRefiner = new TypeRefiner(ResourceType.RDF_PROPERTY);
        instanceRefiner = new TypeRefiner(ResourceType.INSTANCE);
        residualRefiner = new ResidualRefiner(baselineMatcher);

        // analytical cube
        alignmentsCube = new AlignmentsCube();

        // resource explainers
        ExplainerResourceProperty explainerResourceProperty = new ExplainerResourceProperty();
        explainerResourceProperty.add("Label", RDFS.label);
        explainerResourceProperty.add("Comment", RDFS.comment);
        explainerResourceProperty.add("Type", RDF.type);        
        alignmentsCube.setResourceExplainers(Arrays.asList(explainerResourceProperty));
    }


    /**
     * Constructor
     *
     * @param results The execution results for which an evaluation shall be performed.
     * @param metric The confusion matrix metric to be used.
     */
    public EvaluatorCSV(ExecutionResultSet results, ConfusionMatrixMetric metric) {
        this(results, metric, true);
    }


    /**
     * Constructor
     *
     * @param results The execution results for which an evaluation shall be performed.
     */
    public EvaluatorCSV(ExecutionResultSet results){
       this(results, new ConfusionMatrixMetric());
    }

    /**
     * Constructor
     *
     * @param results The execution results for which an evaluation shall be performed.
     * @param isPrintAsShortenedString The CSV output will be written with shortened URIs.
     */
    public EvaluatorCSV(ExecutionResultSet results, boolean isPrintAsShortenedString){
        this(results, new ConfusionMatrixMetric(), isPrintAsShortenedString);
    }

    @Override
    public void writeToDirectory(File baseDirectory) {
        initializePrinters(baseDirectory);
        for (String matcher : this.results.getDistinctMatchers()) {
            // individual evaluation per test case
            for (TestCase testCase : this.results.getDistinctTestCases(matcher)) {
                writeOverviewFileMatcherTestCase(testCase, matcher, baseDirectory, false);
            }
            for (Track track : this.results.getDistinctTracks(matcher)) {
                writeAggregatedFileMatcherTrack(track, matcher, baseDirectory);
            }
        }
        alignmentsCube.write(baseDirectory);
        closePrinters();
    }


    /**
     * This method initializes global writers for the performance KPI CSV files.
     *
     * @param baseDirectory The base directory to which the CSV files shall be written to.
     */
    private void initializePrinters(File baseDirectory) {
        if(!baseDirectory.exists()) baseDirectory.mkdir();
        try {
            testCasePerformanceCubePrinter = new CSVPrinter(new FileWriter(new File(baseDirectory, "testCasePerformanceCube.csv"), false), CSVFormat.DEFAULT);
            testCasePerformanceCubePrinter.printRecord(getHeaderTestCasePerformanceCube());
            trackPerformanceCubePrinter = new CSVPrinter(new FileWriter(new File(baseDirectory, "trackPerformanceCube.csv"), false), CSVFormat.DEFAULT);
            trackPerformanceCubePrinter.printRecord(getHeaderTrackPerformanceCube());
        } catch (IOException ioe) {
            LOGGER.error("Could not initialize CSV Printers for performance cubes.");
        }
    }



    /**
     * This method flushes and closes global printers.
     */
    private void closePrinters() {
        try {
            testCasePerformanceCubePrinter.flush();
            testCasePerformanceCubePrinter.close();
        } catch (IOException ioe) {
            LOGGER.error("Coud not flush and close testCasePerformanceCubePrinter.", ioe);
        }
        try {
            trackPerformanceCubePrinter.flush();
            trackPerformanceCubePrinter.close();
        } catch (IOException ioe) {
            LOGGER.error("Coud not flush and close trackPerformanceCubePrinter.", ioe);
        }
    }

    /**
     * Write the aggregated overview file, i.e. KPIs such as recall or precision, for a matcher on a particular track.
     *
     * @param track         Track
     * @param matcher       Matcher name
     * @param baseDirectory Base directory where file shall be written.
     */
    private void writeAggregatedFileMatcherTrack(Track track, String matcher, File baseDirectory) {

        // micro averages
        ConfusionMatrix microAllCm = confusionMatrixMetric.getMicroAveragesForResults(this.results.getGroup(track, matcher));
        ConfusionMatrix microClassesCm = confusionMatrixMetric.getMicroAveragesForResults(this.results.getGroup(track, matcher, classRefiner));
        ConfusionMatrix microPropertiesCm = confusionMatrixMetric.getMicroAveragesForResults(this.results.getGroup(track, matcher, propertyRefiner));
        ConfusionMatrix microInstancesCm = confusionMatrixMetric.getMicroAveragesForResults(this.results.getGroup(track, matcher, instanceRefiner));
        ConfusionMatrix microAllResidualCm = confusionMatrixMetric.getMicroAveragesForResults(this.results.getGroup(track, matcher, residualRefiner));
        ConfusionMatrix microClassesResidualCm = confusionMatrixMetric.getMicroAveragesForResults(this.results.getGroup(track, matcher, classRefiner, residualRefiner));
        ConfusionMatrix microPropertiesResidualCm = confusionMatrixMetric.getMicroAveragesForResults(this.results.getGroup(track, matcher, propertyRefiner, residualRefiner));
        ConfusionMatrix microInstancesResidualCm = confusionMatrixMetric.getMicroAveragesForResults(this.results.getGroup(track, matcher, instanceRefiner, residualRefiner));

        // macro averages
        ConfusionMatrix macroAllCm = confusionMatrixMetric.getMacroAveragesForResults(this.results.getGroup(track, matcher));
        ConfusionMatrix macroClassesCm = confusionMatrixMetric.getMacroAveragesForResults(this.results.getGroup(track, matcher, classRefiner));
        ConfusionMatrix macroPropertiesCm = confusionMatrixMetric.getMacroAveragesForResults(this.results.getGroup(track, matcher, propertyRefiner));
        ConfusionMatrix macroInstancesCm = confusionMatrixMetric.getMacroAveragesForResults(this.results.getGroup(track, matcher, instanceRefiner));
        ConfusionMatrix macroAllResidualCm = confusionMatrixMetric.getMacroAveragesForResults(this.results.getGroup(track, matcher, residualRefiner));
        ConfusionMatrix macroClassesResidualCm = confusionMatrixMetric.getMacroAveragesForResults(this.results.getGroup(track, matcher, classRefiner, residualRefiner));
        ConfusionMatrix macroPropertiesResidualCm = confusionMatrixMetric.getMacroAveragesForResults(this.results.getGroup(track, matcher, propertyRefiner, residualRefiner));
        ConfusionMatrix macroInstancesResidualCm = confusionMatrixMetric.getMacroAveragesForResults(this.results.getGroup(track, matcher, instanceRefiner, residualRefiner));

        File fileToBeWritten = new File(getResultsDirectoryTrackMatcher(baseDirectory, track), "/" + matcher + "/aggregatedPerformance.csv");
        if (fileToBeWritten.getParentFile().mkdirs())
            LOGGER.info("Results directory created because it did not exist.");

        try {
            CSVPrinter printer = new CSVPrinter(new FileWriter(fileToBeWritten, false), CSVFormat.DEFAULT);
            printer.printRecord(getHeaderAggregated());
            printer.printRecord("ALL", macroAllCm.getPrecision(), macroAllCm.getRecall(), macroAllResidualCm.getRecall(), macroAllCm.getF1measure(), microAllCm.getPrecision(), microAllCm.getRecall(), microAllResidualCm.getRecall(), microAllCm.getF1measure(), macroAllCm.getTruePositiveSize(), macroAllResidualCm.getTruePositiveSize(), macroAllCm.getFalsePositiveSize(), macroAllCm.getFalseNegativeSize(), "-");
            trackPerformanceCubePrinter.printRecord(track.getName(), matcher, "ALL", macroAllCm.getPrecision(), macroAllCm.getRecall(), macroAllResidualCm.getRecall(), macroAllCm.getF1measure(), microAllCm.getPrecision(), microAllCm.getRecall(), microAllResidualCm.getRecall(), microAllCm.getF1measure(), macroAllCm.getTruePositiveSize(), macroAllResidualCm.getTruePositiveSize(), macroAllCm.getFalsePositiveSize(), macroAllCm.getFalseNegativeSize(), "-");
            printer.printRecord("CLASSES", macroClassesCm.getPrecision(), macroClassesCm.getRecall(), macroClassesResidualCm.getRecall(), macroClassesCm.getF1measure(), microClassesCm.getPrecision(), microClassesCm.getRecall(), microClassesResidualCm.getRecall(), microClassesCm.getF1measure(), macroClassesCm.getTruePositiveSize(), macroClassesResidualCm.getTruePositiveSize(), macroClassesCm.getFalsePositiveSize(), macroClassesCm.getFalseNegativeSize(), "-");
            trackPerformanceCubePrinter.printRecord(track.getName(), matcher, "CLASSES", macroClassesCm.getPrecision(), macroClassesCm.getRecall(), macroClassesResidualCm.getRecall(), macroClassesCm.getF1measure(), microClassesCm.getPrecision(), microClassesCm.getRecall(), microClassesResidualCm.getRecall(), microClassesCm.getF1measure(), macroClassesCm.getTruePositiveSize(), macroClassesResidualCm.getTruePositiveSize(), macroClassesCm.getFalsePositiveSize(), macroClassesCm.getFalseNegativeSize(), "-");
            printer.printRecord("PROPERTIES", macroPropertiesCm.getPrecision(), macroPropertiesCm.getRecall(), macroPropertiesResidualCm.getRecall(), macroPropertiesCm.getF1measure(), microPropertiesCm.getPrecision(), microPropertiesCm.getRecall(), microPropertiesResidualCm.getRecall(), microPropertiesCm.getF1measure(), macroPropertiesCm.getTruePositiveSize(), macroPropertiesResidualCm.getTruePositiveSize(), macroPropertiesCm.getFalsePositiveSize(), macroPropertiesCm.getFalseNegativeSize(), "-");
            trackPerformanceCubePrinter.printRecord(track.getName(), matcher, "PROPERTIES", macroPropertiesCm.getPrecision(), macroPropertiesCm.getRecall(), macroPropertiesResidualCm.getRecall(), macroPropertiesCm.getF1measure(), microPropertiesCm.getPrecision(), microPropertiesCm.getRecall(), microPropertiesResidualCm.getRecall(), microPropertiesCm.getF1measure(), macroPropertiesCm.getTruePositiveSize(), macroPropertiesResidualCm.getTruePositiveSize(), macroPropertiesCm.getFalsePositiveSize(), macroPropertiesCm.getFalseNegativeSize(), "-");
            printer.printRecord("INSTANCES", macroInstancesCm.getPrecision(), macroInstancesCm.getRecall(), macroInstancesResidualCm.getRecall(), macroInstancesCm.getF1measure(), microInstancesCm.getPrecision(), microInstancesCm.getRecall(), microInstancesResidualCm.getRecall(), microInstancesCm.getF1measure(), macroInstancesCm.getTruePositiveSize(), macroInstancesResidualCm.getTruePositiveSize(), macroInstancesCm.getFalsePositiveSize(), macroInstancesCm.getFalseNegativeSize(), "-");
            trackPerformanceCubePrinter.printRecord(track.getName(), matcher, "INSTANCES", macroInstancesCm.getPrecision(), macroInstancesCm.getRecall(), macroInstancesResidualCm.getRecall(), macroInstancesCm.getF1measure(), microInstancesCm.getPrecision(), microInstancesCm.getRecall(), microInstancesResidualCm.getRecall(), microInstancesCm.getF1measure(), macroInstancesCm.getTruePositiveSize(), macroInstancesResidualCm.getTruePositiveSize(), macroInstancesCm.getFalsePositiveSize(), macroInstancesCm.getFalseNegativeSize(), "-");
            printer.flush();
            printer.close();
        } catch (IOException ex) {
            LOGGER.error("Could not write detailed evaluation file.", ex);
            ex.printStackTrace();
        }
    }


    /**
     * Write the overview file, i.e. KPIs such as recall or precision, for a matcher on a particular test case.
     *
     * @param testCase      Test case
     * @param matcher       Matcher name
     * @param baseDirectory Base directory where file shall be written.
     * @param onlyCalculateCube Indicator whether only the {@link EvaluatorCSV#alignmentsCube} shall be calculated.
     */
    private void writeOverviewFileMatcherTestCase(TestCase testCase, String matcher, File baseDirectory, boolean onlyCalculateCube) {

        // write alignment file
        if(!onlyCalculateCube) {
            File targetFileForCopyAction = new File(getResultsFolderTrackTestcaseMatcher(baseDirectory, results.get(testCase, matcher)), "systemAlignment.rdf");
            targetFileForCopyAction.getParentFile().mkdirs();
            EvaluatorUtil.copySystemAlignment(results.get(testCase, matcher), targetFileForCopyAction);
        }

        // evaluate system result
        ExecutionResult allExecutionResult = results.get(testCase, matcher);
        ConfusionMatrix allCm = confusionMatrixMetric.compute(results.get(testCase, matcher));
        ConfusionMatrix classCm = confusionMatrixMetric.compute(results.get(testCase, matcher, classRefiner));
        ConfusionMatrix propertiesCm = confusionMatrixMetric.compute(results.get(testCase, matcher, propertyRefiner));
        ConfusionMatrix instanceCm = confusionMatrixMetric.compute(results.get(testCase, matcher, instanceRefiner));
        ConfusionMatrix allResidualCm = confusionMatrixMetric.compute(results.get(testCase, matcher, residualRefiner));
        ConfusionMatrix classResidualCm = confusionMatrixMetric.compute(results.get(testCase, matcher, classRefiner, residualRefiner));
        ConfusionMatrix propertiesResidualCm = confusionMatrixMetric.compute(results.get(testCase, matcher, propertyRefiner, residualRefiner));
        ConfusionMatrix instanceResidualCm = confusionMatrixMetric.compute(results.get(testCase, matcher, instanceRefiner, residualRefiner));

        // evaluation result
        if (allCm.getTruePositive() != null)
            alignmentsCube.getAnalyticalMappingInformation(testCase, matcher).addAll(allCm.getTruePositive(), AnalyticalAlignmentInformation.DefaultFeatures.EVALUATION_RESULT.toString(), "true positive");
        if (allCm.getFalsePositive() != null)
            alignmentsCube.getAnalyticalMappingInformation(testCase, matcher).addAll(allCm.getFalsePositive(), AnalyticalAlignmentInformation.DefaultFeatures.EVALUATION_RESULT.toString(), "false positive");
        if (allCm.getFalseNegative() != null)
            alignmentsCube.getAnalyticalMappingInformation(testCase, matcher).addAll(allCm.getFalseNegative(), AnalyticalAlignmentInformation.DefaultFeatures.EVALUATION_RESULT.toString(), "false negative");

        // residuals (true)
        if (allResidualCm.getTruePositive() != null)
            alignmentsCube.getAnalyticalMappingInformation(testCase, matcher).addAll(allResidualCm.getTruePositive(), AnalyticalAlignmentInformation.DefaultFeatures.RESIDUAL.toString(), "true");
        if (allResidualCm.getFalseNegative() != null)
            alignmentsCube.getAnalyticalMappingInformation(testCase, matcher).addAll(allResidualCm.getFalseNegative(), AnalyticalAlignmentInformation.DefaultFeatures.RESIDUAL.toString(), "true");

        // residuals (false) -> all correspondences that are not true are false
        HashMap<Correspondence, HashMap<String, String>> mappingInformation = alignmentsCube.getAnalyticalMappingInformation(testCase, matcher).getMappingInformation();
        List<Correspondence> nonResidualCorrespondence = new ArrayList<>();
        for(HashMap.Entry<Correspondence, HashMap<String, String>> entry : mappingInformation.entrySet()){
            if(!entry.getValue().containsKey(AnalyticalAlignmentInformation.DefaultFeatures.RESIDUAL.toString())){
                // non residual
                nonResidualCorrespondence.add(entry.getKey());
            }
        }
        alignmentsCube.getAnalyticalMappingInformation(testCase, matcher).addAll(nonResidualCorrespondence, AnalyticalAlignmentInformation.DefaultFeatures.RESIDUAL.toString(), "false");


        if(!onlyCalculateCube) {
            try {
                File fileToBeWritten = new File(super.getResultsFolderTrackTestcaseMatcher(baseDirectory, allExecutionResult), "performance.csv");
                fileToBeWritten.getParentFile().mkdirs();
                CSVPrinter printer = new CSVPrinter(new FileWriter(fileToBeWritten, false), CSVFormat.DEFAULT);
                printer.printRecord(getHeaderIndividual());
                printer.printRecord("ALL", allCm.getPrecision(), allCm.getRecall(), allResidualCm.getRecall(), allCm.getF1measure(), allCm.getTruePositiveSize(), allCm.getFalsePositiveSize(), allCm.getFalseNegativeSize(), allExecutionResult.getRuntime());
                testCasePerformanceCubePrinter.printRecord(testCase.getTrack().getName(), testCase.getName(), matcher, "ALL", allCm.getPrecision(), allCm.getRecall(), allResidualCm.getRecall(), allCm.getF1measure(), allCm.getTruePositiveSize(), allCm.getFalsePositiveSize(), allCm.getFalseNegativeSize(), allExecutionResult.getRuntime());
                printer.printRecord("CLASSES", classCm.getPrecision(), classCm.getRecall(), classResidualCm.getRecall(), classCm.getF1measure(), classCm.getTruePositiveSize(), classCm.getFalsePositiveSize(), classCm.getFalseNegativeSize(), "-");
                testCasePerformanceCubePrinter.printRecord(testCase.getTrack().getName(), testCase.getName(), matcher, "CLASSES", classCm.getPrecision(), classCm.getRecall(), classResidualCm.getRecall(), classCm.getF1measure(), classCm.getTruePositiveSize(), classCm.getFalsePositiveSize(), classCm.getFalseNegativeSize(), "-");
                printer.printRecord("PROPERTIES", propertiesCm.getPrecision(), propertiesCm.getRecall(), propertiesResidualCm.getRecall(), propertiesCm.getF1measure(), propertiesCm.getTruePositiveSize(), propertiesCm.getFalsePositiveSize(), propertiesCm.getFalseNegativeSize(), "-");
                testCasePerformanceCubePrinter.printRecord(testCase.getTrack().getName(), testCase.getName(), matcher, "PROPERTIES", propertiesCm.getPrecision(), propertiesCm.getRecall(), propertiesResidualCm.getRecall(), propertiesCm.getF1measure(), propertiesCm.getTruePositiveSize(), propertiesCm.getFalsePositiveSize(), propertiesCm.getFalseNegativeSize(), "-");
                printer.printRecord("INSTANCES", instanceCm.getPrecision(), instanceCm.getRecall(), instanceResidualCm.getRecall(), instanceCm.getF1measure(), instanceCm.getTruePositiveSize(), instanceCm.getFalsePositiveSize(), instanceCm.getFalseNegativeSize(), "-");
                testCasePerformanceCubePrinter.printRecord(testCase.getTrack().getName(), testCase.getName(), matcher, "INSTANCES", instanceCm.getPrecision(), instanceCm.getRecall(), instanceResidualCm.getRecall(), instanceCm.getF1measure(), instanceCm.getTruePositiveSize(), instanceCm.getFalsePositiveSize(), instanceCm.getFalseNegativeSize(), "-");
                printer.flush();
                printer.close();
            } catch (IOException ioe) {
                LOGGER.error("Could not write KPI file.", ioe);
                ioe.printStackTrace();
            }
        }
    }


    //-------------------------------------------------------------------------------------------
    // Formatting Output
    //-------------------------------------------------------------------------------------------

    /**
     * Get the header row for the individual statistics.
     *
     * @return Header row in the form of a String-List.
     */
    private List<String> getHeaderIndividual() {
        List<String> result = new ArrayList<>();
        result.add("Type");
        result.add("Precision (P)");
        result.add("Recall (R)");
        result.add("Residual Recall (R+)");
        result.add("F1");
        result.add("# of TP");
        result.add("# of FP");
        result.add("# of FN");
        result.add("Time");
        return result;
    }


    /**
     * Get the header row for the individual statistics in the overall CSV file.
     *
     * @return CSV header row in the form of a String-List.
     */
    private List<String> getHeaderTrackPerformanceCube() {
        List<String> result = new ArrayList<>();
        result.add("Track");
        result.add("Matcher");
        result.addAll(getHeaderAggregated());
        return result;
    }


    /**
     * Get the header row for the aggregated results on a per matcher basis.
     *
     * @return Header row in the form of a String-List.
     */
    private List<String> getHeaderAggregated() {
        List<String> result = new ArrayList<>();
        result.add("Type");
        result.add("Macro Precision (P)");
        result.add("Macro Recall (R)");
        result.add("Residual Macro Recall (R+)"); // macro average of recall+
        result.add("Macro F1");
        result.add("Micro Precision (P)");
        result.add("Micro Recall (R)");
        result.add("Residual Micro Recall (R+)"); // micro average of recall+
        result.add("Micro F1");
        result.add("# of TP");
        result.add("# of Residual TP"); // TPs that are not in the baseline
        result.add("# of FP");
        result.add("# of FN");
        result.add("Total Runtime");
        return result;
    }


    /**
     * Get the header row for the individual statistics in the overall CSV file.
     *
     * @return CSV header row in the form of a String-List.
     */
    private List<String> getHeaderTestCasePerformanceCube() {
        List<String> result = new ArrayList<>();
        result.add("Track");
        result.add("Test Case");
        result.add("Matcher");
        result.addAll(getHeaderIndividual());
        return result;
    }


    /**
     * Obtain an output stream that can be used to write the CSV file.
     * @return
     */
    public String getAlignmentsCubeAsString(){
        if(!isPrintAsShortenedString) {
            for (String matcher : this.results.getDistinctMatchers()) {
                // individual evaluation per test case
                for (TestCase testCase : this.results.getDistinctTestCases(matcher)) {
                    writeOverviewFileMatcherTestCase(testCase, matcher, null, true);
                }
            }
            return this.alignmentsCube.toString();
        } else {
            // shortened string, default option
            return getAlignmentsCubeAsShortenedString();
        }
    }

    /**
     * Obtain an output stream that can be used to write the CSV file.
     * @return
     */
    private String getAlignmentsCubeAsShortenedString(){
        for (String matcher : this.results.getDistinctMatchers()) {
            // individual evaluation per test case
            for (TestCase testCase : this.results.getDistinctTestCases(matcher)) {
                writeOverviewFileMatcherTestCase(testCase, matcher, null, true);
            }
        }
        return this.alignmentsCube.toShortString();
    }


    //-------------------------------------------------------------------------------------------
    // Getters and Setters
    //-------------------------------------------------------------------------------------------

    public boolean isCopyAlignmentFiles() {
        return copyAlignmentFiles;
    }

    public void setCopyAlignmentFiles(boolean copyAlignmentFiles) {
        this.copyAlignmentFiles = copyAlignmentFiles;
    }

    public IOntologyMatchingToolBridge getBaselineMatcher() {
        return baselineMatcher;
    }

    public void setBaselineMatcher(IOntologyMatchingToolBridge baselineMatcher) {
        this.baselineMatcher = baselineMatcher;
    }

    public ConfusionMatrixMetric getConfusionMatrixMetric() {
        return confusionMatrixMetric;
    }

    public void setConfusionMatrixMetric(ConfusionMatrixMetric confusionMatrixMetric) {
        this.confusionMatrixMetric = confusionMatrixMetric;
    }

    public List<IExplainerResource> getResourceExplainers() {
        return resourceExplainers;
    }

    public void setResourceExplainers(List<IExplainerResource> resourceExplainers) {
        this.resourceExplainers = resourceExplainers;
        alignmentsCube.setResourceExplainers(resourceExplainers);
    }

    public boolean isPrintAsShortenedString() {
        return isPrintAsShortenedString;
    }

    public void setPrintAsShortenedString(boolean printAsShortenedString) {
        isPrintAsShortenedString = printAsShortenedString;
    }
}
