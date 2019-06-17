package de.uni_mannheim.informatik.dws.ontmatching.validation;


import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;



import java.util.HashSet;
import java.util.Set;

/**
 * This class analyzes a test case.
 *
 * @author Jan Portisch
 */
public class TestCaseValidationService {

    /**
     * Constructor
     * @param testCase Test case on which the analysis was performed on.
     */
    TestCaseValidationService(TestCase testCase){
        this.testCase = testCase;
        analzye();
    }

    /**
     * Test case on which the analysis was performed on.
     */
    private TestCase testCase;

    //-------------------------------------------------------------------------
    // Hard Validation Fields
    //-------------------------------------------------------------------------


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
        return sourceJenaOntologyValidationService.isOntologyParseable() && sourceOwlApiOntologyValidationService.isOntologyParseable()
                && targetJenaOntologyValidationService.isOntologyParseable() && targetOwlApiOntologyValidationService.isOntologyParseable()
                && isReferenceAlignmentParseable && allReferenceEntitiesFound();
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

    public boolean isOneToNMapping(){
        if(
                (nSourceMappings.size() == 0 && nTargetMappings.size() > 0) ||
                        (nSourceMappings.size() > 0 && nTargetMappings.size() == 0)
        ) return true;
        else return false;
    }

    /**
     * Classes of the source ontology that do not appear in the reference mapping.
     */
    HashSet<String> sourceClassesNotMapped = new HashSet<>();

    /**
     * Classes of the target ontology that do not appear in the reference mapping.
     */
    HashSet<String> targetClassesNotMapped = new HashSet<>();


    JenaOntologyValidationService sourceJenaOntologyValidationService;

    JenaOntologyValidationService targetJenaOntologyValidationService;

    OwlApiOntologyValidationService sourceOwlApiOntologyValidationService;

    OwlApiOntologyValidationService targetOwlApiOntologyValidationService;

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
                "Source Ontology parseable( " + sourceJenaOntologyValidationService.getLibName() + ", " + sourceJenaOntologyValidationService.getLibVersion() + "): " + sourceJenaOntologyValidationService.isOntologyParseable() + "\n" +
                "Source Ontology parseable( " + sourceOwlApiOntologyValidationService.getLibName() + ", " + sourceOwlApiOntologyValidationService.getLibVersion() + "): " + sourceOwlApiOntologyValidationService.isOntologyParseable() + "\n" +
                "Target Ontology parseable( " + targetJenaOntologyValidationService.getLibName() + ", " + targetJenaOntologyValidationService.getLibVersion() + "): " + targetJenaOntologyValidationService.isOntologyParseable() + "\n" +
                "Target Ontology parseable( " + targetOwlApiOntologyValidationService.getLibName() + ", " + targetOwlApiOntologyValidationService.getLibVersion() + "): " + targetOwlApiOntologyValidationService.isOntologyParseable() + "\n" +
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

        result = "\n" + result + "\n---------------------------------\nSTATISTICAL INFORMATION ALIGNMENT\n---------------------------------\n";

        // source mappings
        result = result + "Analysis for test case: " + testCase.getName() + "\n" +
                "Number of mappings: " + testCase.getParsedReferenceAlignment().size() + "\n";

        if(isOneToOneMapping()){
            result = result + "Mapping Type: 1:1\n";
        } else if(isOneToNMapping()) {
            result = result + "Mapping Type: 1:n\n";
        } else {
            result = result + "Mapping Type: n:n\n";
        }

        result = result +
                "All source classes mapped: " + isSourceClassesFullyMapped() + "\n" +
                "All source datatype properties mapped: " + isSourceDatatypePropertiesFullyMapped() + "\n" +
                "All source object properties mappes: " + isSourceObjectPropertiesFullyMapped() + "\n";

        // target mappings
        result = result + "All target classes mapped: " + isTargetClassesFullyMapped() + "\n" +
                "All target datatype properties mapped: " + isTargetDatatypePropertiesFullyMapped() + "\n" +
                "All target object properties mappes: " + isTargetObjectPropertiesFullyMapped() + "\n";

        result = "\n" + result + "\n----------------------------------\nSTATISTICAL INFORMATION ONTOLOGIES\n----------------------------------\n";

        result = result + "Analysis for test case: " + testCase.getName() + "\n";

        result = result + "Source Ontology Jena (" + this.sourceOwlApiOntologyValidationService.getLibName() + ", " + sourceOwlApiOntologyValidationService.getLibVersion() + ")" +  "\n";
        result = result + "# of Classes: " + this.sourceJenaOntologyValidationService.getNumberOfClasses() + "\n";
        result = result + "# of Datatype Properties: " + this.sourceJenaOntologyValidationService.getNumberOfDatatypeProperties() + "\n";
        result = result + "# of Object Properties: " + this.sourceJenaOntologyValidationService.getNumberOfObjectProperties() + "\n\n";

        result = result + "Target Ontology Jena (" + this.targetJenaOntologyValidationService.getLibName() + ", " + targetJenaOntologyValidationService.getLibVersion() + ")" +  "\n";
        result = result + "# of Classes: " + this.targetJenaOntologyValidationService.getNumberOfClasses() + "\n";
        result = result + "# of Datatype Properties: " + this.targetJenaOntologyValidationService.getNumberOfDatatypeProperties() + "\n";
        result = result + "# of Object Properties: " + this.targetJenaOntologyValidationService.getNumberOfObjectProperties() + "\n\n";

        result = result + "Source Ontology OWL API (" + this.sourceOwlApiOntologyValidationService.getLibName() + ", " + sourceOwlApiOntologyValidationService.getLibVersion() + ")" +  "\n";
        result = result + "# of Classes: " + this.sourceOwlApiOntologyValidationService.getNumberOfClasses() + "\n";
        result = result + "# of Datatype Properties: " + this.sourceOwlApiOntologyValidationService.getNumberOfDatatypeProperties() + "\n";
        result = result + "# of Object Properties: " + this.sourceOwlApiOntologyValidationService.getNumberOfObjectProperties() + "\n\n";

        result = result + "Target Ontology OWL API (" + this.targetOwlApiOntologyValidationService.getLibName() + ", " + targetOwlApiOntologyValidationService.getLibVersion() + ")" +  "\n";
        result = result + "# of Classes: " + this.targetOwlApiOntologyValidationService.getNumberOfClasses() + "\n";
        result = result + "# of Datatype Properties: " + this.targetOwlApiOntologyValidationService.getNumberOfDatatypeProperties() + "\n";
        result = result + "# of Object Properties: " + this.targetOwlApiOntologyValidationService.getNumberOfObjectProperties() + "\n\n";

        return result;
    }

    //---------------------------------------------
    // Getters
    //---------------------------------------------

    public TestCase getTestCase() {
        return testCase;
    }

        public boolean isSourceOntologyParseable() {
            return this.sourceJenaOntologyValidationService.isOntologyParseable() && this.sourceOwlApiOntologyValidationService.isOntologyParseable();
        }

        public boolean isTargetOntologyParseable() {
            return this.targetJenaOntologyValidationService.isOntologyParseable() && this.targetOwlApiOntologyValidationService.isOntologyParseable();
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

    /**
     * Perform an analysis on the level of a test case.
     *
     **/
    private void analzye() {

        // part1 parse ontologies
        this.sourceJenaOntologyValidationService = new JenaOntologyValidationService(this.testCase.getSource());
        this.targetJenaOntologyValidationService = new JenaOntologyValidationService(this.testCase.getTarget());
        this.sourceOwlApiOntologyValidationService = new OwlApiOntologyValidationService(this.testCase.getSource());
        this.targetOwlApiOntologyValidationService = new OwlApiOntologyValidationService(this.testCase.getTarget());

        Set<String> sourceResources = sourceJenaOntologyValidationService.getAllResources();
        Set<String> targetResources = targetJenaOntologyValidationService.getAllResources();

        // Part 2: Check whether alignment file is parseable.
        Alignment reference = testCase.getParsedReferenceAlignment();
        this.isReferenceAlignmentParseable = (reference != null);


        // Part 3: Check whether all URIs in the reference exist in the ontologies.

        for (Correspondence correspondence : reference) {
            if (!sourceResources.contains(correspondence.getEntityOne())) {
                notFoundInSourceOntology.add(correspondence.getEntityOne());
            }
            if (!targetResources.contains(correspondence.getEntityTwo())) {
                notFoundInTargetOntology.add(correspondence.getEntityTwo());
            }
        }


        // Part 4: (statistical) Check for 1-1 or 1-n mappings
        HashSet<String> sourceElementsMapped = new HashSet<>();
        HashSet<String> nSourceMappings = new HashSet<>();
        HashSet<String> targetElementsMapped = new HashSet<>();
        HashSet<String> nTargetMappings = new HashSet<>();

        for (Correspondence correspondence : reference) {
            if (sourceElementsMapped.contains(correspondence.getEntityOne())) {
                nSourceMappings.add(correspondence.getEntityOne());
            } else sourceElementsMapped.add(correspondence.getEntityOne());
            if (targetElementsMapped.contains(correspondence.getEntityTwo())) {
                nTargetMappings.add(correspondence.getEntityTwo());
            } else targetElementsMapped.add(correspondence.getEntityTwo());
        }

        this.nSourceMappings = nSourceMappings;
        this.nTargetMappings = nTargetMappings;

        // Part 5: (statistical) Check whether all ontology elements are mapped
        for(String resource : sourceJenaOntologyValidationService.getClasses()){
            if(!reference.isSourceContained(resource.toString())){
                this.sourceClassesNotMapped.add(resource.toString());
            }
        }


        for(String resource : targetJenaOntologyValidationService.getClasses()){
            if(!reference.isTargetContained(resource.toString())){
                this.targetClassesNotMapped.add(resource.toString());
            }
        }


        // Part 6: (statistical) Check whether all ontology object properties are mapped
        for(String property : sourceJenaOntologyValidationService.getObjectProperties()){
            if(!reference.isSourceContained(property.toString())){
                this.sourceObjectPropertiesNotMapped.add(property.toString());
            }
        }

        for(String property : targetJenaOntologyValidationService.getObjectProperties()){
            if(!reference.isTargetContained(property.toString())){
                this.targetObjectPropertiesNotMapped.add(property.toString());
            }
        }


        // Part 7: (statistical) Check whether all ontology datatype properties are mapped
        for(String property : sourceJenaOntologyValidationService.getDatatypeProperties()){
            if(!reference.isSourceContained(property.toString())){
                this.sourceDatatypePropertiesNotMapped.add(property.toString());
            }
        }

        for(String property : targetJenaOntologyValidationService.getDatatypeProperties()){
            if(!reference.isTargetContained(property.toString())){
                this.targetDatatypePropertiesNotMapped.add(property.toString());
            }
        }

    }

}
