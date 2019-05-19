package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.IExplainerResource;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ResourceType;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.baselineMatchers.BaselineStringMatcher;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.explainer.ExplainerResourceProperty;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.explainer.NamePropertyTuple;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.cm.ConfusionMatrix;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.cm.ConfusionMatrixMetric;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util.AlignmentsCube;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util.AnalyticalAlignmentInformation;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util.EvaluatorUtil;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.refinement.ResidualRefiner;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.refinement.TypeRefiner;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;
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
     * Analytical Store for all alignments.
     */
    private AlignmentsCube alignmentsCube;

    /**
     * Constructor
     *
     * @param results The execution results for which an evaluation shall be performed.
     */
    public EvaluatorCSV(ExecutionResultSet results) {
        super(results);

        // metrics
        confusionMatrixMetric = new ConfusionMatrixMetric();

        // refiners
        classRefiner = new TypeRefiner(ResourceType.CLASS);
        propertyRefiner = new TypeRefiner(ResourceType.RDF_PROPERTY);
        instanceRefiner = new TypeRefiner(ResourceType.INSTANCE);
        residualRefiner = new ResidualRefiner(baselineMatcher);

        // analytical cube
        alignmentsCube = new AlignmentsCube();

        // resource explainers
        ArrayList<NamePropertyTuple> propertiesList = new ArrayList<>();
        propertiesList.add(new NamePropertyTuple("Label", RDFS.label));
        propertiesList.add(new NamePropertyTuple("Comment", RDFS.comment));
        propertiesList.add(new NamePropertyTuple("Type", RDF.type));
        ExplainerResourceProperty explainerResourceProperty = new ExplainerResourceProperty(propertiesList);
        ArrayList<IExplainerResource> resourceExplainers = new ArrayList<>();
        resourceExplainers.add(explainerResourceProperty);
        alignmentsCube.setResourceExplainers(resourceExplainers);
    }

    @Override
    public void write(File baseDirectory) {
        // evaluation per matcher
        for (String matcher : this.results.getDistinctMatchers()) {

            // individual evaluation per test case
            for (TestCase testCase : this.results.getDistinctTestCases(matcher)) {
                writeOverviewFileMatcherTestCase(testCase, matcher, baseDirectory);
            }

            // aggregated evaluation per track
            for (Track track : this.results.getDistinctTracks(matcher)) {
                writeAggregatedFileMatcherTrack(track, matcher, baseDirectory);
            }
        }
        alignmentsCube.write(baseDirectory);
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
        if (fileToBeWritten.getParentFile().mkdirs()) {
            LOGGER.info("Results directory created because it did not exist.");
        }
        try {
            CSVPrinter printer = new CSVPrinter(new FileWriter(fileToBeWritten, false), CSVFormat.DEFAULT);
            printer.printRecord(getHeaderAggregated());
            printer.printRecord("ALL", macroAllCm.getPrecision(), macroAllCm.getRecall(), macroAllResidualCm.getRecall(), macroAllCm.getF1measure(), microAllCm.getPrecision(), microAllCm.getRecall(), microAllResidualCm.getRecall(), microAllCm.getF1measure(), macroAllCm.getTruePositiveSize(), macroAllResidualCm.getTruePositiveSize(), macroAllCm.getFalsePositiveSize(), macroAllCm.getFalseNegativeSize(), "-");
            printer.printRecord("CLASSES", macroClassesCm.getPrecision(), macroClassesCm.getRecall(), macroClassesResidualCm.getRecall(), macroClassesCm.getF1measure(), microClassesCm.getPrecision(), microClassesCm.getRecall(), microClassesResidualCm.getRecall(), microClassesCm.getF1measure(), macroClassesCm.getTruePositiveSize(), macroClassesResidualCm.getTruePositiveSize(), macroClassesCm.getFalsePositiveSize(), macroClassesCm.getFalseNegativeSize(), "-");
            printer.printRecord("PROPERTIES", macroPropertiesCm.getPrecision(), macroPropertiesCm.getRecall(), macroPropertiesResidualCm.getRecall(), macroPropertiesCm.getF1measure(), microPropertiesCm.getPrecision(), microPropertiesCm.getRecall(), microPropertiesResidualCm.getRecall(), microPropertiesCm.getF1measure(), macroPropertiesCm.getTruePositiveSize(), macroPropertiesResidualCm.getTruePositiveSize(), macroPropertiesCm.getFalsePositiveSize(), macroPropertiesCm.getFalseNegativeSize(), "-");
            printer.printRecord("INSTANCES", macroInstancesCm.getPrecision(), macroInstancesCm.getRecall(), macroInstancesResidualCm.getRecall(), macroInstancesCm.getF1measure(), microInstancesCm.getPrecision(), microInstancesCm.getRecall(), microInstancesResidualCm.getRecall(), microInstancesCm.getF1measure(), macroInstancesCm.getTruePositiveSize(), macroInstancesResidualCm.getTruePositiveSize(), macroInstancesCm.getFalsePositiveSize(), macroInstancesCm.getFalseNegativeSize(), "-");
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
     */
    private void writeOverviewFileMatcherTestCase(TestCase testCase, String matcher, File baseDirectory) {

        // write alignment file
        File targetFileForCopyAction = new File(getResultsFolderTrackTestcaseMatcher(baseDirectory,  results.get(testCase, matcher)), "systemAlignment.rdf");
        targetFileForCopyAction.getParentFile().mkdirs();
        EvaluatorUtil.copySystemAlignment(results.get(testCase, matcher), targetFileForCopyAction);

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
        if(allCm.getTruePositive() != null) alignmentsCube.getAnalyticalMappingInformation(testCase, matcher).addAll(allCm.getTruePositive(), AnalyticalAlignmentInformation.DefaultFeatures.EVALUATION_RESULT.toString(), "true positive");
        if(allCm.getFalsePositive() != null) alignmentsCube.getAnalyticalMappingInformation(testCase, matcher).addAll(allCm.getFalsePositive(), AnalyticalAlignmentInformation.DefaultFeatures.EVALUATION_RESULT.toString(), "false positive");
        if(allCm.getFalseNegative() != null) alignmentsCube.getAnalyticalMappingInformation(testCase, matcher).addAll(allCm.getFalseNegative(), AnalyticalAlignmentInformation.DefaultFeatures.EVALUATION_RESULT.toString(), "false negative");

        // residuals
        if(allResidualCm.getTruePositive() != null) alignmentsCube.getAnalyticalMappingInformation(testCase, matcher).addAll(allResidualCm.getTruePositive(), AnalyticalAlignmentInformation.DefaultFeatures.RESIDUAL.toString(), "true");
        if(allResidualCm.getFalseNegative() != null) alignmentsCube.getAnalyticalMappingInformation(testCase, matcher).addAll(allResidualCm.getFalseNegative(), AnalyticalAlignmentInformation.DefaultFeatures.RESIDUAL.toString(), "true");

        try {
            File fileToBeWritten = new File(super.getResultsFolderTrackTestcaseMatcher(baseDirectory, allExecutionResult), "performance.csv");
            fileToBeWritten.getParentFile().mkdirs();
            CSVPrinter printer = new CSVPrinter(new FileWriter(fileToBeWritten, false), CSVFormat.DEFAULT);
            printer.printRecord(getHeaderIndividual());
            printer.printRecord("ALL", allCm.getPrecision(), allCm.getRecall(), allResidualCm.getRecall(), allCm.getF1measure(), allCm.getTruePositiveSize(), allCm.getFalsePositiveSize(), allCm.getFalseNegativeSize(), allExecutionResult.getRuntime());
            printer.printRecord("CLASSES", classCm.getPrecision(), classCm.getRecall(), classResidualCm.getRecall(), classCm.getF1measure(), classCm.getTruePositiveSize(), classCm.getFalsePositiveSize(), classCm.getFalseNegativeSize(), "-");
            printer.printRecord("PROPERTIES", propertiesCm.getPrecision(), propertiesCm.getRecall(), propertiesResidualCm.getRecall(), propertiesCm.getF1measure(), propertiesCm.getTruePositiveSize(), propertiesCm.getFalsePositiveSize(), propertiesCm.getFalseNegativeSize(), "-");
            printer.printRecord("INSTANCES", instanceCm.getPrecision(), instanceCm.getRecall(), instanceResidualCm.getRecall(), instanceCm.getF1measure(), instanceCm.getTruePositiveSize(), instanceCm.getFalsePositiveSize(), instanceCm.getFalseNegativeSize(), "-");
            printer.flush();
            printer.close();
        } catch (IOException ioe) {
            LOGGER.error("Could not write KPI file.", ioe);
            ioe.printStackTrace();
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
     * Given a confusion matrix, this method returns a line as list that can be written.
     *
     * @param systemConfusionMatrix   The system confusion matrix for which a line shall be written. Cannot be null.
     * @param residualConfusionMatrix The residual confusion matrix, can be null.
     * @param confusionMatrixType     Type of the confusion matrix e.g. CLASSES.
     * @return The line to be written as List.
     */
    private List<String> getLineStatistics(ConfusionMatrix systemConfusionMatrix, ConfusionMatrix residualConfusionMatrix, EvaluatorUtil.ConfusionMatrixType confusionMatrixType) {
        List<String> result = new ArrayList<>();
        result.add(confusionMatrixType.toString()); // Type
        result.add(Double.toString(systemConfusionMatrix.getPrecision())); // Precision
        result.add(Double.toString(systemConfusionMatrix.getRecall())); // Recall
        if (residualConfusionMatrix != null) {
            result.add(Double.toString(residualConfusionMatrix.getRecall())); // Residual Recall
        } else {
            result.add("-");
        }
        result.add(Double.toString(systemConfusionMatrix.getF1measure())); // F1
        result.add(Double.toString(systemConfusionMatrix.getTruePositiveSize())); // # of TP
        result.add(Double.toString(systemConfusionMatrix.getFalsePositiveSize())); // # of FP
        result.add(Double.toString(systemConfusionMatrix.getFalseNegativeSize())); // # of FN
        return result;
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
}
