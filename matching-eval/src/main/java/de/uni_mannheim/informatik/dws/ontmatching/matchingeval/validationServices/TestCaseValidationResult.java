package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.validationServices;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;

import java.util.HashSet;

/**
 * Individual Result for the {@link TestCaseValidationService}
 *
 * @author Jan Portisch
 */
public class TestCaseValidationResult {

    /**
     * Constructor
     * @param testCase Test case on which the analysis was performed on.
     */
    TestCaseValidationResult(TestCase testCase){
        this.testCase = testCase;
    }

    /**
     * Test case on which the analysis was performed on.
     */
    private TestCase testCase;

    //-------------------------------------------------------------------------
    // Hard Validation Fields
    //-------------------------------------------------------------------------

    /**
     * Indicator whether the source ontology can be parsed by Jena.
     */
    boolean isParseableByJenaSourceOntology;

    /**
     * Indicator whether the target ontology can be parsed by Jena.
     */
    boolean isParseableByJenaTargetOntology;

    /**
     * Indicator whether the reference alignment of the test case is parseable.
     */
    boolean isReferenceAlignmentParseable;

    /**
     * Set of entities that were mentioned in the reference alignment but could not be found in the source ontology.
     */
    HashSet<String> notFoundInSourceOntology = new HashSet<>();

    /**
     * Set of entities that were mentioned in the reference alignment but could not be found in the target ontology.
     */
    HashSet<String> notFoundInTargetOntology = new HashSet<>();

    /**
     * Indicates whether the URIs named in the reference file
     * @return True if all reference entries found, else false.
     */
    public boolean allReferenceEntitiesFound(){
        return isAllSourceReferenceEntitiesFound() && isAllTargetReferenceEntitiesFound();
    }

    /**
     * Indicates whether the result object is free of errors.
     * @return True if error free, else false.
     */
    public boolean isOK(){
        return isParseableByJenaSourceOntology && isParseableByJenaTargetOntology && isReferenceAlignmentParseable && allReferenceEntitiesFound();
    }

    //-------------------------------------------------------------------------
    // Statistical Fields
    //-------------------------------------------------------------------------

    /**
     * Set of the source elements that were mapped more than once.
     */
    HashSet<String> nSourceMappings = new HashSet<>();

    /**
     * Set of the target elements that were mapped more than once.
     */
    HashSet<String> nTargetMappings = new HashSet<>();

    /**
     * Indicates whether the reference alignment is a 1-1 mapping or a 1-n mapping.
     * @return
     */
    public boolean isOneToOneMapping(){
        if(nSourceMappings.size() + nTargetMappings.size() > 0){
            return false;
        } else return true;
    }

    /**
     * Classes of the source ontology that do not appear in the reference mapping.
     */
    HashSet<String> sourceClassesNotMapped = new HashSet<>();

    /**
     * Classes of the target ontology that do not appear in the reference mapping.
     */
    HashSet<String> targetClassesNotMapped = new HashSet<>();

    /**
     * Indicates whether all source classes appear in the mapping.
     * @return True if all source elements appear in the mapping, else false.
     */
    public boolean isSourceClassesFullyMapped(){
        return sourceClassesNotMapped.size() == 0;
    }

    /**
     * Indicates whether all target classes appear in the mapping.
     * @return True if all target elements appear in the mapping, else false.
     */
    public boolean isTargetClassesFullyMapped(){
        return targetClassesNotMapped.size() == 0;
    }

    /**
     * Indicates whether all source and all target elements appear in the mapping.
     * @return True if all source and all target elements appear in the mapping, else false.
     */
    public boolean isClassesFullyMapped(){
        return isSourceClassesFullyMapped() && isTargetClassesFullyMapped();
    }

    /**
     * Object properties of the source ontology that do not appear in the reference mapping.
     */
    HashSet<String> sourceObjectPropertiesNotMapped = new HashSet<>();

    /**
     * Object properties of the target ontology that do not appear in the reference mapping.
     */
    HashSet<String> targetObjectPropertiesNotMapped = new HashSet<>();

    /**
     * Indicates whether all source object properties appear in the mapping.
     * @return True if all source object properties appear in the mapping, else false.
     */
    public boolean isSourceObjectPropertiesFullyMapped(){
        return sourceObjectPropertiesNotMapped.size() == 0;
    }

    /**
     * Indicates whether all target object properties appear in the mapping.
     * @return True if all target object properties appear in the mapping, else false.
     */
    public boolean isTargetObjectPropertiesFullyMapped(){
        return targetObjectPropertiesNotMapped.size() == 0;
    }

    /**
     * Indicates whether all source and all target ObjectProperties appear in the mapping.
     * @return True if all source and all target ObjectProperties appear in the mapping, else false.
     */
    public boolean isObjectPropertiesFullyMapped(){
        return isSourceObjectPropertiesFullyMapped() && isTargetObjectPropertiesFullyMapped();
    }

    /**
     * Classes of the source ontology that do not appear in the reference mapping.
     */
    HashSet<String> sourceDatatypePropertiesNotMapped = new HashSet<>();

    /**
     * Classes of the target ontology that do not appear in the reference mapping.
     */
    HashSet<String> targetDatatypePropertiesNotMapped = new HashSet<>();


    /**
     * Indicates whether all source data type properties appear in the mapping.
     * @return True if all source object properties appear in the mapping, else false.
     */
    public boolean isSourceDatatypePropertiesFullyMapped(){
        return sourceDatatypePropertiesNotMapped.size() == 0;
    }

    /**
     * Indicates whether all target data type properties appear in the mapping.
     * @return True if all target data type properties appear in the mapping, else false.
     */
    public boolean isTargetDatatypePropertiesFullyMapped(){
        return targetDatatypePropertiesNotMapped.size() == 0;
    }

    /**
     * Indicates whether all source and all target data type properties appear in the mapping.
     * @return True if all source and all target data type properties appear in the mapping, else false.
     */
    public boolean isDatatypePropertiesFullyMapped(){
        return isSourceDatatypePropertiesFullyMapped() && isTargetDatatypePropertiesFullyMapped();
    }

    /**
     * Checks whether all classes, data type properties and object properties of the source are fully mapped.
     * @return True if all classes, data type properties and object properties of the source are fully mapped; else false.
     */
    public boolean isSourceFullyMapped(){
        return isSourceClassesFullyMapped() && isSourceDatatypePropertiesFullyMapped() && isSourceObjectPropertiesFullyMapped();
    }

    /**
     * Checks whether all classes, data type properties and object properties of the target are fully mapped.
     * @return True if all classes, data type properties and object properties of the target are fully mapped; else false.
     */
    public boolean isTargetFullyMapped(){
        return isTargetClassesFullyMapped() && isTargetDatatypePropertiesFullyMapped() && isTargetObjectPropertiesFullyMapped();
    }

    /**
     * Checks whether all classes, data type properties and object properties are fully mapped.
     * @return True if all classes, data type properties and object properties are fully mapped; else false.
     */
    public boolean isFullyMapped(){
        return isSourceFullyMapped() && isTargetFullyMapped();
    }

    @Override
    public String toString(){
        String result = "----------------------\nVALIDATION INFORMATION\n----------------------\n" +
                "Analysis for test case: " + testCase.getName() + "\n" +
                "Source Ontology parseable: " + isParseableByJenaSourceOntology + "\n" +
                "Target Ontology parseable: " + isParseableByJenaTargetOntology + "\n" +
                "Reference Alignment parseable: " + isReferenceAlignmentParseable + "\n";

        // reference: source
        if(isAllSourceReferenceEntitiesFound()){
            result = result + "All source reference entries found: true\n";
        } else {
            result = result + "All source reference entries found: false\n";
            if(notFoundInSourceOntology.size() > 0) {
                result = result + "The following source entries could not be found: \n";
                for (String uri : notFoundInSourceOntology){
                    result = result + uri + "\n";
                }
            }
        }

        // reference: target
        if(isAllTargetReferenceEntitiesFound()){
            result = result + "All target reference entries found: true\n";
        } else {
            result = result + "All target reference entries found: false\n";
            if(notFoundInTargetOntology.size() > 0) {
                result = result + "The following target entries could not be found: \n";
                for (String uri : notFoundInTargetOntology){
                    result = result + uri + "\n";
                }
            }
        }

        result = "\n" + result + "\n-----------------------\nSTATISTICAL INFORMATION\n-----------------------\n";

        // soruce mappings
        result = result + "Analysis for test case: " + testCase.getName() + "\n" +
                "All source classes mapped: " + isSourceClassesFullyMapped() + "\n" +
                "All source datatype properties mapped: " + isSourceDatatypePropertiesFullyMapped() + "\n" +
                "All source object properties mappes: " + isSourceObjectPropertiesFullyMapped() + "\n";

        // target mappings
        result = result + "All target classes mapped: " + isTargetClassesFullyMapped() + "\n" +
                "All target datatype properties mapped: " + isTargetDatatypePropertiesFullyMapped() + "\n" +
                "All target object properties mappes: " + isTargetObjectPropertiesFullyMapped() + "\n";

        return result;
    }

    //---------------------------------------------
    // Getters
    //---------------------------------------------

    public TestCase getTestCase() {
        return testCase;
    }

    public boolean isParseableByJenaSourceOntology() {
        return isParseableByJenaSourceOntology;
    }

    public boolean isParseableByJenaTargetOntology() {
        return isParseableByJenaTargetOntology;
    }

    public boolean isReferenceAlignmentParseable() {
        return isReferenceAlignmentParseable;
    }

    public HashSet<String> getNotFoundInSourceOntology() {
        return notFoundInSourceOntology;
    }

    public HashSet<String> getNotFoundInTargetOntology() {
        return notFoundInTargetOntology;
    }

    public boolean isAllSourceReferenceEntitiesFound() {
        return notFoundInSourceOntology.size() == 0;
    }

    public boolean isAllTargetReferenceEntitiesFound() {
        return notFoundInTargetOntology.size() == 0;
    }

    public HashSet<String> getnSourceMappings() {
        return nSourceMappings;
    }

    public HashSet<String> getnTargetMappings() {
        return nTargetMappings;
    }

    public HashSet<String> getSourceClassesNotMapped() {
        return sourceClassesNotMapped;
    }

    public HashSet<String> getTargetClassesNotMapped() {
        return targetClassesNotMapped;
    }

    public HashSet<String> getSourceObjectPropertiesNotMapped() {
        return sourceObjectPropertiesNotMapped;
    }

    public HashSet<String> getTargetObjectPropertiesNotMapped() {
        return targetObjectPropertiesNotMapped;
    }

    public HashSet<String> getSourceDatatypePropertiesNotMapped() {
        return sourceDatatypePropertiesNotMapped;
    }

    public HashSet<String> getTargetDatatypePropertiesNotMapped() {
        return targetDatatypePropertiesNotMapped;
    }
}
