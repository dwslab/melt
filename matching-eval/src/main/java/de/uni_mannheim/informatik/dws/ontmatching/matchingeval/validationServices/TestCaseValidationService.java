package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.validationServices;


import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;


import java.util.HashSet;

/**
 * This class analyzes a test case.
 *
 * @author Jan Portisch
 */
public class TestCaseValidationService {

    /**
     * Perform an analysis on the level of a test case.
     *
     * @param testCase
     * @return
     */
    public static TestCaseValidationResult analzye(TestCase testCase) {

        TestCaseValidationResult result = new TestCaseValidationResult(testCase);

        // Part 1: Ontologies parseable in Jena.
        OntModel sourceOntology = testCase.getSourceOntology(OntModel.class);
        OntModel targetOntology = testCase.getTargetOntology(OntModel.class);
        result.isParseableByJenaSourceOntology = (sourceOntology != null);
        result.isParseableByJenaTargetOntology = (targetOntology != null);


        // Part 2: Check whether alignment file is parseable.
        Alignment reference = testCase.getParsedReferenceAlignment();
        result.isReferenceAlignmentParseable = (reference != null);


        // Part 3: Check whether all URIs in the reference exist in the ontologies.
        HashSet<String> notFoundInSourceOntology = new HashSet<>();
        HashSet<String> notFoundInTargetOntology = new HashSet<>();

        for (Correspondence correspondence : reference) {
            if (sourceOntology.getOntResource(correspondence.getEntityOne()) == null) {
                notFoundInSourceOntology.add(correspondence.getEntityOne());
            }
            if (targetOntology.getOntResource(correspondence.getEntityTwo()) == null) {
                notFoundInTargetOntology.add(correspondence.getEntityTwo());
            }
        }

        if (notFoundInSourceOntology.size() > 0) {
            result.notFoundInSourceOntology = notFoundInSourceOntology;
        }

        if (notFoundInTargetOntology.size() > 0) {
            result.notFoundInTargetOntology = notFoundInTargetOntology;
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


        // Part 5: (statistical) Check whether all ontology elements are mapped
        for(OntClass resource : sourceOntology.listClasses().toSet()){
            if(resource.isAnon()) continue;
            if(!reference.isSourceContained(resource.toString())){
                result.sourceClassesNotMapped.add(resource.toString());
            }
        }
        for(OntClass resource : targetOntology.listClasses().toSet()){
            if(resource.isAnon()) continue;
            if(!reference.isTargetContained(resource.toString())){
                result.targetClassesNotMapped.add(resource.toString());
            }
        }


        // Part 6: (statistical) Check whether all ontology object properties are mapped
        for(OntProperty property : sourceOntology.listObjectProperties().toSet()){
            if(property.isAnon()) continue;
            if(!reference.isSourceContained(property.toString())){
                result.sourceObjectPropertiesNotMapped.add(property.toString());
            }
        }

        for(OntProperty property : targetOntology.listObjectProperties().toSet()){
            if(property.isAnon()) continue;
            if(!reference.isTargetContained(property.toString())){
                result.targetObjectPropertiesNotMapped.add(property.toString());
            }
        }


        // Part 7: (statistical) Check whether all ontology datatype properties are mapped
        for(OntProperty property : sourceOntology.listDatatypeProperties().toSet()){
            if(property.isAnon()) continue;
            if(!reference.isSourceContained(property.toString())){
                result.sourceDatatypePropertiesNotMapped.add(property.toString());
            }
        }

        for(OntProperty property : targetOntology.listDatatypeProperties().toSet()){
            if(property.isAnon()) continue;
            if(!reference.isTargetContained(property.toString())){
                result.targetDatatypePropertiesNotMapped.add(property.toString());
            }
        }
        return result;
    }

}
