package de.uni_mannheim.informatik.dws.ontmatching.validation.trackspecific;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;
import de.uni_mannheim.informatik.dws.ontmatching.validation.SemanticWebLibrary;
import de.uni_mannheim.informatik.dws.ontmatching.validation.TestCaseValidationService;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DynamicTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains some methods to make tests for tracks easier.
 */
public class AssertHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssertHelper.class);
    
    
    private static void assertTestCaseValidationService(TestCaseValidationService service){
        LOGGER.info("ParsingInfo of testcase {}: \n\tsource:{}\n\ttarget:{}", service.getTestCase().toString(),
                service.getSourceOntologyValidationService().toString(), 
                service.getTargetOntologyValidationService().toString());
        assertTrue(service.isSourceOntologyParseable(), "Source Ontology not parsable");
        assertTrue(service.isTargetOntologyParseable(), "Target Ontology not parsable");
        assertTrue(service.isReferenceAlignmentParseable(), "Reference alignment not parseable");
        
        assertTrue(service.getSourceOntologyValidationService().isOntologyDefined(), "The source ontology does not contain an ontology definition.");
        assertTrue(service.getTargetOntologyValidationService().isOntologyDefined(), "The target ontology does not contain an ontology definition.");


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
    
    public static void assertTestCase(TestCase testCase, SemanticWebLibrary lib){
        assertTestCaseValidationService(new TestCaseValidationService(testCase, lib));
    }
    
    
    public static void assertTrack(Track t){
        for(TestCase testCase : t.getTestCases()){
            assertTestCase(testCase);
        }
    }
    
    public static void assertTrack(Track t, SemanticWebLibrary lib){
        for(TestCase testCase : t.getTestCases()){
            assertTestCase(testCase, lib);
        }
    }
    
    
    /**
     * This method returns multiple tests to be used with JUnit's @TestFactory annotation.
     * Each testcase will become its own test.
     * @param t
     * @return 
     */
    public static Stream<DynamicTest> assertDynamicTrack(Track t){
        return t.getTestCases().stream().flatMap((testCase) -> 
                Stream.of(
                    DynamicTest.dynamicTest("Test " + testCase.getName() + " parse with Jena", () -> 
                            AssertHelper.assertTestCase(testCase, SemanticWebLibrary.JENA)),
                    DynamicTest.dynamicTest("Test " + testCase.getName() + " parse with OWLAPI", () -> 
                            AssertHelper.assertTestCase(testCase, SemanticWebLibrary.OWLAPI))
                )
        );
    }
    
    public static Stream<DynamicTest> assertDynamicTrack(Track t, SemanticWebLibrary lib){
        return t.getTestCases().stream().map((testCase) -> 
            DynamicTest.dynamicTest("Test " + testCase.getName(), () -> AssertHelper.assertTestCase(testCase, lib))
        );
    }
    
}
