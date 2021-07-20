package de.uni_mannheim.informatik.dws.melt.matching_owlapi;

import java.io.File;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.semanticweb.owlapi.model.OWLOntology;

public class OntologyCacheOwlApiTest {
    private static final int NUMBER_AXIOMS = 318;

    @Test
    public void testTwoTimesSameFileContent() {
        File ontologyFile = new File("./src/test/resources/cmt.owl");
        OWLOntology modelCmt = OntologyCacheOwlApi.get(ontologyFile);
        assertNotNull(modelCmt);
        
        assertEquals(NUMBER_AXIOMS, modelCmt.getAxiomCount());
        
        OWLOntology cachedModel = OntologyCacheOwlApi.get(ontologyFile); //should be loaded by cache
        assertEquals(modelCmt, cachedModel); 
        
        
        File ontologyFile2 = new File("./src/test/resources/cmt2.owl"); //no cache but same file content
        OWLOntology model2 = OntologyCacheOwlApi.get(ontologyFile2);
        assertNotNull(model2);
        assertEquals(modelCmt, model2);
    }
}