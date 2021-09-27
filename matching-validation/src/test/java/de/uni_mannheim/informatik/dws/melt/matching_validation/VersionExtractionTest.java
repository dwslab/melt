package de.uni_mannheim.informatik.dws.melt.matching_validation;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class VersionExtractionTest {
    
    @Test
    void analzye() {
        JenaOntologyValidationService jenaService = new JenaOntologyValidationService(new File("src/test/resources/cmt.owl"));
        System.out.println("Jena version: " + jenaService.getLibVersion());
        assertNotNull(jenaService.getLibVersion());
        assertTrue(jenaService.getLibVersion().trim().length() > 0);
        
        
        
        OwlApiOntologyValidationService owlapiService = new OwlApiOntologyValidationService(new File("src/test/resources/cmt.owl"));
        System.out.println("OwlApi version: " +owlapiService.getLibVersion());
        assertNotNull(owlapiService.getLibVersion());
        assertTrue(owlapiService.getLibVersion().trim().length() > 0);
        
    }
}