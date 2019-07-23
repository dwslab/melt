package de.uni_mannheim.informatik.dws.ontmatching.validation.local;

import de.uni_mannheim.informatik.dws.ontmatching.validation.JenaOntologyValidationService;
import de.uni_mannheim.informatik.dws.ontmatching.validation.OntologyValidationService;
import de.uni_mannheim.informatik.dws.ontmatching.validation.OwlApiOntologyValidationService;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLocalFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestLocalFile.class);
        
    //@ParameterizedTest
    //@ValueSource(strings = { })
    public void testOwlAPI(String path) {
        LOGGER.info("Path: " + path);
        OntologyValidationService ovs = new OwlApiOntologyValidationService(new File(path));
        LOGGER.info(ovs.getLibName());
        LOGGER.info(ovs.toString());
        assertTrue(ovs.isOntologyParseable());
        assertTrue(ovs.isOntologyDefined());
        assertTrue(ovs.getNumberOfClasses() > 50);
        assertTrue(ovs.getNumberOfInstances() > 4000);
        //assertTrue(ovs.getNumberOfProperties() > 0);
        assertTrue(ovs.getNumberOfStatements() > 4000);
    }
    
    //@ParameterizedTest
    //@ValueSource(strings = { })
    public void testJena(String path) {
        LOGGER.info("Path: " + path);
        OntologyValidationService ovs = new JenaOntologyValidationService(new File(path));
        LOGGER.info(ovs.getLibName());
        LOGGER.info(ovs.toString());
        assertTrue(ovs.isOntologyParseable());
        assertTrue(ovs.isOntologyDefined());
        assertTrue(ovs.getNumberOfClasses() > 50);
        assertTrue(ovs.getNumberOfInstances() > 4000);
        assertTrue(ovs.getNumberOfProperties() > 100);
        assertTrue(ovs.getNumberOfStatements() > 4000);
    }
}
