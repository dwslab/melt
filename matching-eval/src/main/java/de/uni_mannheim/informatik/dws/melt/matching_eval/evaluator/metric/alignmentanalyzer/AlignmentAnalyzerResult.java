package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.alignmentanalyzer;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The AlignmentAnalyzerResult is the output of the {@link AlignmentAnalyzerMetric}.
 * @author Jan Portisch
 */
public class AlignmentAnalyzerResult {
    /**
     * Logger for this class.
     */
    private static Logger LOGGER = LoggerFactory.getLogger(AlignmentAnalyzerResult.class);
    


    /**
     * Execution result that was analyzed.
     */
    private ExecutionResult executionResult;

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
    private Map<CorrespondenceRelation, Integer> frequenciesOfRelations;

    /**
     * Indicates whether only resources of the same type are matched e.g. classes with
     * classes and object properties with object properites.
     */
    private boolean isHomogenousAlingment;

    /**
     * This data structure keeps track of the frequency of different mapping types,
     * e.g. "class - class" → 55
     */
    private Map<String, Integer> frequenciesOfMappingTypes;
    
    /**
     * How many URIs are in correct position which means entity one in correspondence is found in source ontology
     * and entity two is found in target ontology.
     */
    private int urisCorrectPosition;
    
    /**
     * How many URIs are NOT in correct position which means entity one in correspondence is found in target ontology
     * and entity two is found in source ontology.
     */
    private int urisIncorrectPosition;
    
    /**
     * Which URIs in the alignment are not found in source nor target ontology.
     */
    private List<String> urisNotFound;
    
    /**
     * Which arity occurs how often
     */
    private Map<Arity, Integer> arityCounts;
    
    /**
     * If this string is not empty, then it contains the parsing error message.
     */
    private String parsingErrorMessage;
    
    /**
     * Constructor
     * @param executionResult Execution result that was analyzed.
     * @param minimumConfidence The minimum confidence score that is used in the given alignment.
     * @param maximumConfidence The maximum confidence score that is used in the given alignment.
     * @param frequenciesOfRelations The distribution of relations in the given mapping.
     * @param isHomogenousAlingment Indicator on whether only resources of the same type are matched.
     * @param frequenciesOfMappingTypes Frequency of different mapping types.
     * @param urisCorrectPosition How often source and target URIs were correct in the alignment file.
     * @param urisIncorrectPosition How often source and target URIs were incorrect in the alignment file.
     * @param urisNotFound List of URIs that cannot be found in the ontologies to be matched.
     * @param arityCounts Distribution of arities.
     * @param parsingErrorMessage the parsing error message if one exists
     */
    AlignmentAnalyzerResult(ExecutionResult executionResult, double minimumConfidence,
                            double maximumConfidence, Map<CorrespondenceRelation, Integer> frequenciesOfRelations,
                            boolean isHomogenousAlingment, Map<String, Integer> frequenciesOfMappingTypes,
                            int urisCorrectPosition, int urisIncorrectPosition, List<String> urisNotFound,
                            Map<Arity, Integer> arityCounts, String parsingErrorMessage){
        this.executionResult = executionResult;
        this.minimumConfidence = minimumConfidence;
        this.maximumConfidence = maximumConfidence;
        this.frequenciesOfRelations = frequenciesOfRelations;
        this.isHomogenousAlingment = isHomogenousAlingment;
        this.frequenciesOfMappingTypes = frequenciesOfMappingTypes;
        this.urisCorrectPosition = urisCorrectPosition;
        this.urisIncorrectPosition = urisIncorrectPosition;
        this.urisNotFound = urisNotFound;
        this.arityCounts = arityCounts;
        this.parsingErrorMessage = parsingErrorMessage;
    }

    @Override
    public String toString() {
        return getReportForAlignment();
    }


    public String getErroneousReport() {
        StringBuilder sb = new StringBuilder();
        if(this.parsingErrorMessage.length() > 0){
            sb.append("Parsing error").append(this.executionResult);
            return sb.toString();
        }
        if(this.isSwitchOfSourceTargetBetter()){
            sb.append("Need switch: ").append(this.executionResult);
        }
            
        if(this.getUrisNotFound().isEmpty() == false){
            sb.append("Not found: ").append(this.getUrisNotFound());
        }
        return sb.toString();
    }
    
    public void logReport() {
        LOGGER.info(getReportForAlignment());
    }

    public void logErroneousReport() {
       String error = getErroneousReport();
       if(error.length() != 0)
            LOGGER.error(error);
    }
    
    /**
     * Get a textual report of the alignment as String.
     * @return Textual report.
     */
    public String getReportForAlignment() {
        StringBuilder result = new StringBuilder();
        
        // header
        result.append(String.format("Alignment Report for %s on track %s (%s) for test case %s%n%n", 
                executionResult.getMatcherName(),
                executionResult.getTestCase().getTrack().getName(),
                executionResult.getTestCase().getTrack().getVersion(),
                executionResult.getTestCase().getName()));
        
        if(this.hasParsingError()) {
            result.append(String.format("The mapping has the following parsing error:%n"));
            result.append(this.getParsingErrorMessage());
            return result.toString();
        }
        
        // base line
        result.append(String.format("Number of correspondences: %d%n%n", this.getNumberOfCorrespondences()));

        // heterogeneity
        if(this.isHomogenousAlingment) {
            result.append(String.format("The mapping is homogenous.%n%n"));
        } else {
            result.append(String.format("The mapping is heterogenous.%n%n"));
        }

        // mapping type distribution
        result.append(String.format("Distribution of mapping types:%n"));
        for (Entry<String, Integer> freq : this.getFrequenciesOfMappingTypes().entrySet()) {
            result.append(String.format("%s (%d)%n", freq.getKey(), freq.getValue()));
        }
        result.append(String.format("%n"));

        // relations
        if(isAlwaysEqualityRelation()) {
            result.append(String.format("All correspondences are made up of equivalence relations.%n%n"));
        } else {
            result.append(String.format("Distribution of mapping relations:%n"));
            for (Entry<CorrespondenceRelation, Integer> freq : this.getFrequenciesOfRelations().entrySet()) {
                result.append(String.format("%s (%d)%n", freq.getKey(), freq.getValue()));
            }
            result.append(String.format("%n%n"));
        }
        
        if(isConfidenceScoresAreAlwaysOne()){
            result.append(String.format("The confidence of all correspondences is 1.0%n%n"));
        } else {
            result.append(String.format("The minimum confidence is %f%n",this.getMinimumConfidence()));
            result.append(String.format("The maximum confidence is %f%n%n", this.getMaximumConfidence()));
        }
        
        if(this.urisNotFound.isEmpty()){
            result.append(String.format("All URIs in the correspondence are found in source or target.%n"));
        } else {
            result.append(String.format("The following URIs are not found in source nor target: %s%n",this.urisNotFound));
        }
        
        if(this.isSwitchOfSourceTargetBetter()){
            result.append(String.format("More left entites(entity one) in alignment are found in target ontology."
                    + "A switch of entity one and two in alignment makes sense! URIs in correct order: %d URIs incorrect order %d%n%n",
                    this.urisCorrectPosition, this.urisIncorrectPosition));
        }else{
            result.append(String.format("A switch of entity one and two in alignment makes NO sense! URIs in correct order: %d URIs incorrect order: %d%n%n",
                    this.urisCorrectPosition, this.urisIncorrectPosition));
        }
        
        result.append(String.format("Arity analysis:%n"));
        for (Entry<Arity, Integer> arity : this.arityCounts.entrySet()) {
            result.append(String.format("%s (%d)%n", arity.getKey(), arity.getValue()));
        }
        return result.toString();
    }

    /**
     * Get the minimum confidence score of the alignment.
     * @return Minimum confidence as double.
     */
    public double getMinimumConfidence() {
        return this.minimumConfidence;
    }

    /**
     * Get the maximum confidence score of the alignment
     * @return Maximum confidence as double.
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
     * If multiple relations are used, false will be returned.
     * @return True if the only relation in the alignment is equality, else false.
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
     * @return Mapping relations used as set.
     */
    public HashSet<CorrespondenceRelation> getRelationsUsed(){
        return new HashSet<CorrespondenceRelation>(frequenciesOfRelations.keySet());
    }

    /**
     * Returns a set of different mapping types (e.g. CLASS-CLASS) that are used in the mapping
     * @return Mapping types used as set.
     */
    public HashSet<String> getMappingTypesUsed(){
        return new HashSet<>(frequenciesOfMappingTypes.keySet());
    }

    /**
     * Returns true if the alignment is homogenous, i.e., whether only resources of the same type
     * are matched e.g. classes with classes and object properties with object properites.
     * @return True if homogenous, else false.
     */
    public boolean isHomogenousAlignment() {
        return this.isHomogenousAlingment;
    }

    /**
     * Returns true if the alignment is heterogenous, i.e., whether any resource types can be
     * matched with each other e.g. object properties with classes.
     * @return True if heterogenous else false.
     */
    public boolean isHeterogenousAlignment() {
        return !isHomogenousAlignment();
    }

    /**
     * Returns the frequency of different mapping types.
     * e.g. "class - class" → 55
     * @return Distribution of mapping types.
     */
    public Map<String, Integer> getFrequenciesOfMappingTypes(){
        return frequenciesOfMappingTypes;
    }


    /**
     * Returns frequencies of relations in the given mapping.
     * Example: EQUIVALENCE → 40
     * @return Mapping of type {@code relation → frequency}.
     */
    public Map<CorrespondenceRelation, Integer> getFrequenciesOfRelations(){
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

    /**
     * Returns the number of URIs in correct position.
     * This means entity one in correspondence is found in source ontology
     * and entity two is found in target ontology.
     * @return number of URIs in correct position
     */
    public int getUrisCorrectPosition() {
        return urisCorrectPosition;
    }

    /**
     * Returns the number of URIs NOT in correct position.
     * This means entity one in correspondence is found in target ontology
     * and entity two is found in source ontology.
     * @return number of URIs NOT in correct position
     */
    public int getUrisIncorrectPosition() {
        return urisIncorrectPosition;
    }
    
    /**
     * Tests if a switch of source and target URIs makes sense 
     * e.g. more first entities of correspondnce are found in target ontology and not source ontology.
     * @return True if a switch of source and target URIs makes sense .
     */
    public boolean isSwitchOfSourceTargetBetter() {
        return urisCorrectPosition < urisIncorrectPosition;
    }

    /**
     * Returns a list of URIs which are found not in source nor target ontology.
     * @return List of URIs which are found not in source nor target ontology
     */
    public List<String> getUrisNotFound() {
        return urisNotFound;
    }

    /**
     * Return the map of arity to corresponding counts.
     * @return map of arity to corresponding counts.
     */
    public Map<Arity, Integer> getArityCounts() {
        return arityCounts;
    }

    /**
     * If this string is not empty, then it contains the parsing error message.
     * @return the parsing error message
     */
    public String getParsingErrorMessage() {
        return parsingErrorMessage;
    }
    
    /**
     * Return true, if there is a parsing error.
     * @return return true, if there is a parsing error.
     */
    public boolean hasParsingError(){
        return parsingErrorMessage.length() > 0;
    }
    
    
}
