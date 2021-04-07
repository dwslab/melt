package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.babelnet;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class BabelNetEmbeddingLinkerTest {


    @Test
    void normalize() {
        assertEquals("Petit_Bersac", BabelNetEmbeddingLinker.normalizeStatic("bn:Petit-Bersac_n_EN"));
        assertEquals("Europe", BabelNetEmbeddingLinker.normalizeStatic("Europe"));
        assertEquals("Quadrant",BabelNetEmbeddingLinker.normalizeStatic("http://babelnet.org/rdf/Quadrant_n_EN"));
    }

    @Test
    void linkToSingleConcept(){
        BabelNetEmbeddingLinker linker = new BabelNetEmbeddingLinker(loadFile("babelnet_links.txt"));
        assertNotNull(linker.linkToSingleConcept("third_cervical_vertebra"));
        assertNotNull(linker.linkToSingleConcept("third cervical vertebra"));
        assertNotNull(linker.linkToSingleConcept("Third cervical vertebra"));
        assertNull(linker.linkToSingleConcept("Jan Philipp Portisch"));
    }

    /**
     * Helper function to load files in class path that contain spaces.
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private File loadFile(String fileName){
        try {
            File result =  FileUtils.toFile(this.getClass().getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception){
            exception.printStackTrace();
            fail("Could not load file.", exception);
            return null;
        }
    }
}