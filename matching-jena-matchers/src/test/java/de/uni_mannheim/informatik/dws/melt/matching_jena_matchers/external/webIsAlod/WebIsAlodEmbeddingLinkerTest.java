package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class WebIsAlodEmbeddingLinkerTest {

    @Test
    void linkToSingleConcept(){
        String pathToAlodEntityFile = loadFile("alod_entity_test_file.txt").getAbsolutePath();
        WebIsAlodEmbeddingLinker webIsAlodLinker =  new WebIsAlodEmbeddingLinker(pathToAlodEntityFile);

        // test 1: Europe
        String term = "Europe";
        String webIsAlod1_link = webIsAlodLinker.linkToSingleConcept(term);
        assertNotNull(webIsAlod1_link);
        //System.out.println("WebIsALOD (" + term + "): " + webIsAlod1_link);

        // test 2: european union member
        term = "European Union member";
        webIsAlod1_link = webIsAlodLinker.linkToSingleConcept(term);
        assertNotNull(webIsAlod1_link);
        //System.out.println("WebIsALOD (" + term + "): " + webIsAlod1_link);

        // test 3: check something that is not found
        term = "Hello World";
        webIsAlod1_link = webIsAlodLinker.linkToSingleConcept(term);
        assertNull(webIsAlod1_link);
        //System.out.println("WebIsALOD (" + term + "): " + webIsAlod1_link);
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