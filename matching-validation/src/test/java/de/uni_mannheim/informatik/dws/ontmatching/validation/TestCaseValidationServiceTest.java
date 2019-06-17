package de.uni_mannheim.informatik.dws.ontmatching.validation;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test ensures a correct implementation of the validation services themselves.
 * The tests executed here are not track-specific - they do not test or validate a particular track.
 *
 * Due to the <a href="http://www.mojohaus.org/templating-maven-plugin/">Templating Maven Plugin</a>, this test has
 * to be executed in maven and might fail when run within the IDE.
 */
class TestCaseValidationServiceTest {

    @Test
    void analzye() {
        //----------------------------------------------
        // Test 1: Correct Alignment
        //----------------------------------------------
        File sourceOntologyFile = new File("src/test/resources/cmt.owl");
        File targetOntologyFile = new File("src/test/resources/conference.owl");
        File referenceAlignment = new File("src/test/resources/cmt-conference.rdf");
        TestCase testCase1 = new TestCase("test_case_1", sourceOntologyFile.toURI(), targetOntologyFile.toURI(), referenceAlignment.toURI(), null);
        TestCaseValidationService result1 = new TestCaseValidationService(testCase1);
        assertTrue(result1.sourceJenaOntologyValidationService.isOntologyParseable());
        assertTrue(result1.targetJenaOntologyValidationService.isOntologyParseable());
        assertTrue(result1.sourceOwlApiOntologyValidationService.isOntologyParseable());
        assertTrue(result1.targetOwlApiOntologyValidationService.isOntologyParseable());
        assertTrue(result1.isReferenceAlignmentParseable());
        assertTrue(result1.isAllSourceReferenceEntitiesFound());
        assertTrue(result1.isAllTargetReferenceEntitiesFound());
        assertEquals(0, result1.getNotFoundInSourceOntology().size());
        assertEquals(0, result1.getNotFoundInTargetOntology().size());
        assertFalse(result1.isSourceClassesFullyMapped());
        assertFalse(result1.isTargetClassesFullyMapped());
        assertFalse(result1.getSourceClassesNotMapped().contains("http://cmt#Conference"));
        assertFalse(result1.getTargetClassesNotMapped().contains("http://conference#Conference_volume"));
        assertFalse(result1.isClassesFullyMapped());
        assertFalse(result1.isSourceObjectPropertiesFullyMapped());
        assertFalse(result1.isTargetObjectPropertiesFullyMapped());
        assertFalse(result1.isObjectPropertiesFullyMapped());
        assertFalse(result1.isSourceFullyMapped());
        assertFalse(result1.isTargetFullyMapped());
        assertTrue(result1.isOK());

        //----------------------------------------------
        // Test 2: Incorrect Alignment
        // Same data set as for test 1 but an additional URI has been added to the alignment that is not contained in
        // any ontology ("http://conference#DoesNotExist").
        //----------------------------------------------
        referenceAlignment = new File("src/test/resources/cmt-conference-corrputed.rdf");
        TestCase testCase2 = new TestCase("test_case_2", sourceOntologyFile.toURI(), targetOntologyFile.toURI(), referenceAlignment.toURI(), null);
        TestCaseValidationService result2 = new TestCaseValidationService(testCase2);
        assertTrue(result2.sourceJenaOntologyValidationService.isOntologyParseable());
        assertTrue(result2.targetJenaOntologyValidationService.isOntologyParseable());
        assertTrue(result2.sourceOwlApiOntologyValidationService.isOntologyParseable());
        assertTrue(result2.targetOwlApiOntologyValidationService.isOntologyParseable());
        assertTrue(result2.isReferenceAlignmentParseable());
        assertTrue(result2.isAllSourceReferenceEntitiesFound());
        assertFalse(result2.isAllTargetReferenceEntitiesFound());
        assertEquals(0, result2.getNotFoundInSourceOntology().size());
        assertEquals(1, result2.getNotFoundInTargetOntology().size());
        assertTrue(result2.getNotFoundInTargetOntology().contains("http://conference#DoesNotExist"));
        assertFalse(result2.isSourceClassesFullyMapped());
        assertFalse(result2.getSourceClassesNotMapped().contains("http://cmt#Conference"));
        assertFalse(result2.getTargetClassesNotMapped().contains("http://conference#Conference_volume"));
        assertFalse(result2.isTargetClassesFullyMapped());
        assertFalse(result2.isClassesFullyMapped());
        assertFalse(result2.isSourceObjectPropertiesFullyMapped());
        assertFalse(result2.isTargetObjectPropertiesFullyMapped());
        assertFalse(result2.isObjectPropertiesFullyMapped());
        assertFalse(result2.isSourceFullyMapped());
        assertFalse(result2.isTargetFullyMapped());
        assertFalse(result2.isOK());
    }
}