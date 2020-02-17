package de.uni_mannheim.informatik.dws.melt.matching_validation;

import java.io.File;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


/**
 * This test ensures a correct implementation of the validation services themselves.
 * The tests executed here are not track-specific - they do not test or validate a particular track.
 *
 * Due to the <a href="http://www.mojohaus.org/templating-maven-plugin/">Templating Maven Plugin</a>, this test has
 * to be executed in maven and might fail when run within the IDE.
 */
public class OntologyValidationServiceTest {


    @Test
    public void test() {

        JenaOntologyValidationService jenaService = new JenaOntologyValidationService(new File("src/test/resources/cmt.owl"));
        assertTrue(jenaService.isOntologyParseable());
        assertTrue(jenaService.isOntologyDefined());
        assertTrue(jenaService.getNumberOfClasses() > 0);
        assertTrue(jenaService.getNumberOfInstances() == 0);
        assertTrue(jenaService.getNumberOfStatements() > 0);
        System.out.println(jenaService.toString());

        OwlApiOntologyValidationService owlapiService = new OwlApiOntologyValidationService(new File("src/test/resources/cmt.owl"));
        assertTrue(owlapiService.isOntologyParseable());
        assertTrue(owlapiService.isOntologyDefined());
        assertTrue(owlapiService.getNumberOfClasses() > 0);
        assertTrue(owlapiService.getNumberOfInstances() == 0);
        assertTrue(owlapiService.getNumberOfStatements() > 0);
        System.out.println(owlapiService.toString());

    }

}
