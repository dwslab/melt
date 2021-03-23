package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wiktionary;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class WiktionaryEmbeddingLinkerTest {


    private static WiktionaryEmbeddingLinker linker;

    @BeforeAll
    static void setup(){
         linker = new WiktionaryEmbeddingLinker(loadFile("dbnary_embedding_entities.txt"));
    }

    @Test
    void linkToSingleConcept(){
       assertNotNull(linker.linkToSingleConcept("CAT"));
       assertNotNull(linker.linkToSingleConcept("cat"));
       assertNotNull(linker.linkToSingleConcept("European Union"));
       assertNotNull(linker.linkToSingleConcept("European_Union"));
       assertNull(linker.linkToSingleConcept("THIS CONCEPT DOES NOT EXIST"));
    }

    @Test
    void normalize() {
    }

    @Test
    void getNameOfLinker() {
        assertNotNull(linker.getNameOfLinker());
    }

    @Test
    void setNameOfLinker() {
        final String name = "My Linker";
        linker.setNameOfLinker(name);
        assertEquals(name, linker.getNameOfLinker());
    }

    /**
     * Helper function to load files in class path that contain spaces.
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private static File loadFile(String fileName){
        try {
            File result =
                    FileUtils.toFile(WiktionaryEmbeddingLinkerTest.class.getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception){
            exception.printStackTrace();
            fail("Could not load file.");
            return null;
        }
    }
}