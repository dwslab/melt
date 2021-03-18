package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class WordNetEmbeddingLinkerTest {


    @Test
    void checkFileReadOperation(){
        WordNetEmbeddingLinker linker = new WordNetEmbeddingLinker(loadFile("wn_rdf2vec_entities.txt"));
        assertNotNull(linker.linkToSingleConcept("free list"));
        assertNull(linker.linkToSingleConcept("JPP"));
    }

    @Test
    void normalize(){
        WordNetEmbeddingLinker linker = new WordNetEmbeddingLinker(loadFile("wn_rdf2vec_entities.txt"));
        assertEquals("Chaldean-a", linker.normalize("http://wordnet-rdf.princeton" +
                ".edu/rdf/lemma/Chaldean#Chaldean-a"));
    }

    /**
     * Helper function to load files in class path that contain spaces.
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private File loadFile(String fileName){
        try {
            File result = FileUtils.toFile(this.getClass().getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception){
            exception.printStackTrace();
            fail("Could not load file.");
            return null;
        }
    }
}