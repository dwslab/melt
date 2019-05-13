package de.uni_mannheim.informatik.dws.ontmatching.matchingjena;

import org.apache.jena.ontology.OntModelSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OntologyCacheJenaTest {

    /**
     * Test about the axioms.
     * This test has to evaluate to true in order to work.
     */
    @Test
    public static void axioms(){
        // has to hold in order for cache to work (true positive test)
        assertTrue(OntModelSpec.OWL_DL_MEM.hashCode() == OntModelSpec.OWL_DL_MEM.hashCode());
        assertTrue(OntModelSpec.OWL_DL_MEM_RDFS_INF.hashCode() == OntModelSpec.OWL_DL_MEM_RDFS_INF.hashCode());

        // true negative test
        assertFalse(OntModelSpec.OWL_DL_MEM.hashCode() == OntModelSpec.OWL_DL_MEM_RDFS_INF.hashCode());
        assertFalse(OntModelSpec.OWL_DL_MEM.hashCode() == OntModelSpec.OWL_LITE_MEM.hashCode());
    }

}