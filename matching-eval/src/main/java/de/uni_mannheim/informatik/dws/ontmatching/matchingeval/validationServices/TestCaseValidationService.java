package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.validationServices;


import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import org.apache.jena.ontology.OntModel;

import java.io.File;
import java.net.URI;
import java.util.HashSet;

/**
 * This class analyzes a test case.
 */
public class TestCaseValidationService {


    /**
     * Perform an analysis on the level of a test case.
     * @param testCase
     * @return
     */
    public static TestCaseValidationResult analzye(TestCase testCase){

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

        for(Correspondence correspondence : reference){
            if(sourceOntology.getOntResource(correspondence.getEntityOne()) == null){
                notFoundInSourceOntology.add(correspondence.getEntityOne());
            }
            if(targetOntology.getOntResource(correspondence.getEntityTwo()) == null){
                notFoundInTargetOntology.add(correspondence.getEntityTwo());
            }
        }

        if(notFoundInSourceOntology.size() > 0){
            result.allSourceReferenceEntitiesFound = false;
            result.notFoundInSourceOntology = notFoundInSourceOntology;
        } else result.allSourceReferenceEntitiesFound = true;

        if(notFoundInTargetOntology.size() > 0){
            result.allTargetReferenceEntitiesFound = false;
            result.notFoundInTargetOntology = notFoundInTargetOntology;
        } else result.allTargetReferenceEntitiesFound = true;

        return result;
    }

}
