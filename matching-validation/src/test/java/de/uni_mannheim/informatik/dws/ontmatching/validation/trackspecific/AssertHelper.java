package de.uni_mannheim.informatik.dws.ontmatching.validation.trackspecific;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;
import de.uni_mannheim.informatik.dws.ontmatching.validation.SemanticWebLibrary;
import de.uni_mannheim.informatik.dws.ontmatching.validation.TestCaseValidationService;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DynamicTest;

/**
 * This class contains some methods to make tests for tracks easier.
 */
public class AssertHelper {
    
    private static void assertTestCaseValidationService(TestCaseValidationService service){
        assertTrue(service.isSourceOntologyParseable(), "Source Ontology not parsable");
        assertTrue(service.isTargetOntologyParseable(), "Target Ontology not parsable");
        assertTrue(service.isReferenceAlignmentParseable(), "Reference alignment not parseable");


        assertEquals(0, service.getNotFoundInSourceOntology().size(), 
                "Some resources from reference alignment not found in source ontology: " + service.getNotFoundInSourceOntology().toString());
        assertEquals(0, service.getNotFoundInTargetOntology().size(), 
                "Some resources from reference alignment not found in target ontology: " + service.getNotFoundInTargetOntology().toString());

        assertTrue(service.getSourceOntologyValidationService().getNumberOfClasses() > 0 , "Number of classes in source ontology is zero");
        assertTrue(service.getTargetOntologyValidationService().getNumberOfClasses() > 0 , "Number of classes in target ontology is zero");
    }
    
    
    
            
    public static void assertTestCase(TestCase testCase){
        assertTestCaseValidationService(new TestCaseValidationService(testCase, SemanticWebLibrary.JENA));
        assertTestCaseValidationService(new TestCaseValidationService(testCase, SemanticWebLibrary.OWLAPI));
    }
    
    public static void assertTrack(Track t){
        for(TestCase testCase : t.getTestCases()){
            assertTestCase(testCase);
        }
    }
    
    
    /**
     * This method returns multiple tests to be used with JUnit's @TestFactory annotation.
     * Each testcase will become its own test.
     * @param t
     * @return 
     */
    public static Stream<DynamicTest> assertDynamicTrack(Track t){
        return t.getTestCases().stream().map((testCase) -> 
            DynamicTest.dynamicTest("Test " + testCase.getName(), () -> AssertHelper.assertTestCase(testCase))
        );
    }
    
}
