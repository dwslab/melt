package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.validationServices;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;

import java.util.HashSet;

/**
 * Individual Result for the {@link TestCaseValidationService}
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
     * Indicator whether all URIs that are used in the reference alignment could also be found in the
     * specified source ontology.
     */
    boolean allSourceReferenceEntitiesFound;

    /**
     * Indicator whether all URIs that are used in the reference alignment could also be found in the
     * specified source ontology.
     */
    boolean allTargetReferenceEntitiesFound;

    /**
     * Indicates whether the result object is free of errors.
     * @return True if error free, else false.
     */
    public boolean isOK(){
        return isParseableByJenaSourceOntology && isParseableByJenaTargetOntology && isReferenceAlignmentParseable && allReferenceEntitiesFound();
    }

    /**
     * Indicates whether the URIs named in the reference file
     * @return True if all reference entries found, else false.
     */
    public boolean allReferenceEntitiesFound(){
        return allSourceReferenceEntitiesFound && allTargetReferenceEntitiesFound;
    }

    @Override
    public String toString(){
        String result =  "Analysis for test case: " + testCase.getName() + "\n" +
                "Source Ontology parseable: " + isParseableByJenaSourceOntology + "\n" +
                "Target Ontology parseable: " + isParseableByJenaTargetOntology + "\n" +
                "Reference Alignment parseable: " + isReferenceAlignmentParseable + "\n";

        // reference: source
        if(allSourceReferenceEntitiesFound){
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
        if(allTargetReferenceEntitiesFound){
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
        return allSourceReferenceEntitiesFound;
    }

    public boolean isAllTargetReferenceEntitiesFound() {
        return allTargetReferenceEntitiesFound;
    }
}
