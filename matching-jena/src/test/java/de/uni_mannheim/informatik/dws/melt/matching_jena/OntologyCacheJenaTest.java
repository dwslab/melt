package de.uni_mannheim.informatik.dws.melt.matching_jena;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OntologyCacheJenaTest {

    private static final int NUMBER_MODEL_CLASSES = 40;

    /**
     * Test about the axioms.
     * This test has to evaluate to true in order to work.
     */
    @Test
    void axioms() {
        // has to hold in order for cache to work (true positive test)
        assertTrue(OntModelSpec.OWL_DL_MEM.hashCode() == OntModelSpec.OWL_DL_MEM.hashCode());
        assertTrue(OntModelSpec.OWL_DL_MEM_RDFS_INF.hashCode() == OntModelSpec.OWL_DL_MEM_RDFS_INF.hashCode());

        // true negative test
        assertFalse(OntModelSpec.OWL_DL_MEM.hashCode() == OntModelSpec.OWL_DL_MEM_RDFS_INF.hashCode());
        assertFalse(OntModelSpec.OWL_DL_MEM.hashCode() == OntModelSpec.OWL_LITE_MEM.hashCode());
    }
    
    @Test
    void parseAnatomyFileFromURL() {
        OntModel model = OntologyCacheJena.get("http://oaei.webdatacommons.org/tdrs/testdata/persistent/anatomy_track/anatomy_track-default/suite/mouse-human-suite/component/source/");
        assertTrue(model.size() > 0);
    }
    
    @Test
    void parseNotCorrectlyFormattedFile() {
        File file = new File("./src/test/resources/badNtriple.nt");        
        OntModel model = OntologyCacheJena.get(file);
        assertTrue(model.size() > 0);
    }

    //http://oaei.webdatacommons.org/tdrs/testdata/persistent/anatomy_track/anatomy_track-default/suite/mouse-human-suite/component/source/

    @Test
    void getUrl() {
        File ontologyFile = new File("./src/test/resources/cmt.owl");
        try {
            OntModel model = OntologyCacheJena.get(ontologyFile.toURI().toURL());
            assertNotNull(model);
            List<OntClass> classes = model.listClasses().toList();
            assertEquals(NUMBER_MODEL_CLASSES, model.listClasses().toList().size());

            // run again for cache
            model = OntologyCacheJena.get(ontologyFile.toURI().toURL());
            assertNotNull(model);
            assertEquals(NUMBER_MODEL_CLASSES, model.listClasses().toList().size());
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        }
    }

    @Test
    void getFile() {
        File ontologyFile = new File("./src/test/resources/cmt.owl");
        OntModel model = OntologyCacheJena.get(ontologyFile);
        assertNotNull(model);
        assertEquals(NUMBER_MODEL_CLASSES, model.listClasses().toList().size());

        // run again for cache
        model = OntologyCacheJena.get(ontologyFile);
        assertNotNull(model);
        assertEquals(NUMBER_MODEL_CLASSES, model.listClasses().toList().size());
    }

    @Test
    void getUri() {
        File ontologyFile = new File("./src/test/resources/cmt.owl");
        OntModel model = OntologyCacheJena.get(ontologyFile.toURI());
        assertNotNull(model);
        assertEquals(NUMBER_MODEL_CLASSES, model.listClasses().toList().size());

        // run again for cache
        model = OntologyCacheJena.get(ontologyFile.toURI());
        assertNotNull(model);
        assertEquals(NUMBER_MODEL_CLASSES, model.listClasses().toList().size());
    }

    @Test
    void getUriString() {
        OntModel model = OntologyCacheJena.get("./src/test/resources/cmt.owl");
        assertNotNull(model);
        assertEquals(NUMBER_MODEL_CLASSES,model.listClasses().toList().size());

        // run again for cache
        model = OntologyCacheJena.get("./src/test/resources/cmt.owl");
        assertNotNull(model);
        assertEquals(NUMBER_MODEL_CLASSES, model.listClasses().toList().size());
    }

    @AfterEach
    void teardown(){
        OntologyCacheJena.emptyCache();
    }

}