package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.alignmentanalyzer;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.CorrespondenceRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;

/**
 * The AlignmentAnalyzerResult is the output of the {@link AlignmentAnalyzerMetric}.
 * @author Jan Portisch
 */
public class AlignmentAnalyzerResult {

    /**
     * Constructor
     * @param executionResult Execution result that was analyzed.
     * @param minimumConfidence The minimum confidence score that is used in the given alignment.
     * @param maximumConfidence The maximum confidence score that is used in the given alignment.
     * @param frequenciesOfRelations The distribution of relations in the given mapping.
     * @param isHomogenousAlingment Indicator on whether only resources of the same type are matched.
     * @param frequenciesOfMappingTypes Frequency of different mapping types.
     */
    AlignmentAnalyzerResult(ExecutionResult executionResult, double minimumConfidence,
                            double maximumConfidence, HashMap<CorrespondenceRelation, Integer> frequenciesOfRelations,
                            boolean isHomogenousAlingment, HashMap<String, Integer> frequenciesOfMappingTypes){

        this.executionResult = executionResult;
        this.minimumConfidence = minimumConfidence;
        this.maximumConfidence = maximumConfidence;
        this.frequenciesOfRelations = frequenciesOfRelations;
        this.isHomogenousAlingment = isHomogenousAlingment;
        this.frequenciesOfMappingTypes = frequenciesOfMappingTypes;
    }


    /**
     * Execution result that was analyzed.
     */
    private ExecutionResult executionResult;


    /**
     * Logger for this class.
     */
    private static Logger LOGGER = LoggerFactory.getLogger(AlignmentAnalyzerResult.class);


    /**
     * The minimum confidence score that is used in the given alignment.
     */
    private double minimumConfidence;

    /**
     * The maximum confidence score that is used in the given alignment.
     */
    private double maximumConfidence;

    /**
     * The distribution of relations in the given mapping.
     * Example: EQUIVALENCE → 40
     */
    private HashMap<CorrespondenceRelation, Integer> frequenciesOfRelations;

    /**
     * Indicates whether only resources of the same type are matched e.g. classes with
     * classes and object properties with object properites.
     */
    private boolean isHomogenousAlingment;

    /**
     * This data structure keeps track of the frequency of different mapping types,
     * e.g. "class - class" → 55
     */
    private HashMap<String, Integer> frequenciesOfMappingTypes;



    /**
     * Get a textual report of the alignment as String.
     * @return
     */
    public String getReportForAlignment() {

        // header
        String result = "Alignment Report for " + executionResult.getMatcherName() + " on "
                + "track " + executionResult.getTestCase().getTrack().getName() + " "
                + "for test case " + executionResult.getTestCase().getName() + "\n\n";

        // base line
        result = result + "Number of correspondences: " + this.getNumberOfCorrespondences() + "\n\n";

        // heterogeneity
        if(this.isHomogenousAlingment) {
            result = result + "The mapping is homogenous.\n\n";
        } else {
            result = result + "The mapping is heterogenous.\n\n";
        }

        // mapping type distribution
        result = result + "Distribution of mapping types:\n";
        for (String key : this.getFrequenciesOfMappingTypes().keySet()) {
            result = result + key + " (" + this.getFrequenciesOfMappingTypes().get(key) + ")\n";
        }
        result = result + "\n";

        // relations
        if(isAlwaysEqualityRelation()) {
            result = result + "All correspondences are made up of equivalence relations.\n";
        } else {
            result = result + "Distribution of mapping relations:\n";
            for (CorrespondenceRelation key : this.getFrequenciesOfRelations().keySet()) {
                result = result + key + " (" + this.getFrequenciesOfRelations().get(key) + ")\n";
            }
            result = result + "\n";
        }
        result = result + "\n";

        if(isConfidenceScoresAreAlwaysOne()){
            result = result + "The confidence of all correspondences is 1.0.\n";
        } else {
            result = result + "The minimum confidence is " + this.getMinimumConfidence() + "\n";
            result = result + "The maximum confidence is " + this.getMaximumConfidence() + "\n";
        }

        return result;
    }

    /**
     * Get the minimum confidence score of the alignment.
     * @return
     */
    public double getMinimumConfidence() {
        return this.minimumConfidence;
    }

    /**
     * Get the maximum confidence score of the alignment
     * @return
     */
    public double getMaximumConfidence() {
        return this.maximumConfidence;
    }

    /**
     * Checks whether the confidence scores of the given mapping are all equal to 1.0.
     * @return True if confidence scores = 1.0; else false.
     */
    public boolean isConfidenceScoresAreAlwaysOne(){
        if(minimumConfidence == 1.0 && maximumConfidence == 1.0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if each cell in the mapping uses the equivalence relation.
     * If multiple reltions are used, false will be returned.
     * @return
     */
    public boolean isAlwaysEqualityRelation() {
        if(frequenciesOfRelations.size() == 1 && frequenciesOfRelations.containsKey(CorrespondenceRelation.EQUIVALENCE)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a set of the different mapping relations (e.g. "=" or "&gt;") that are used in the mapping.
     * @return
     */
    public HashSet<CorrespondenceRelation> getRelationsUsed(){
        return new HashSet<CorrespondenceRelation>(frequenciesOfRelations.keySet());
    }

    /**
     * Returns a set of different mapping types (e.g. CLASS-CLASS) that are used in the mapping
     * @return
     */
    public HashSet<String> getMappingTypesUsed(){
        return new HashSet<>(frequenciesOfMappingTypes.keySet());
    }

    /**
     * Returns true if the alignment is homogenous, i.e., whether only resources of the same type
     * are matched e.g. classes with classes and object properties with object properites.
     * @return
     */
    public boolean isHomogenousAlignment() {
        return this.isHomogenousAlingment;
    }

    /**
     * Returns true if the alignment is heterogenous, i.e., whether any resource types can be
     * matched with each other e.g. object properties with classes.
     * @return
     */
    public boolean isHeterogenousAlignment() {
        return !isHomogenousAlignment();
    }

    /**
     * Returns the frequency of different mapping types.
     * e.g. "class - class" → 55
     */
    public HashMap<String, Integer> getFrequenciesOfMappingTypes(){
        return frequenciesOfMappingTypes;
    }


    /**
     * Returns frequencies of relations in the given mapping.
     * Example: EQUIVALENCE → 40
     */
    public HashMap<CorrespondenceRelation, Integer> getFrequenciesOfRelations(){
        return this.frequenciesOfRelations;
    }


    /**
     * Print a textual report of the alignment to the console.
     */
    public void printReportForAlignmentToConsole() {
        System.out.println(getReportForAlignment());
    }


    /**
     * Returns the size of the mapping.
     * @return Size of the mapping as integer.
     */
    public int getNumberOfCorrespondences(){
        return executionResult.getSystemAlignment().size();
    }

    /**
     * Print a textual report using the logger.
     */
    public void logReportForAlignmentToConsole(){
        LOGGER.info("\n" + getReportForAlignment());
    }

}
