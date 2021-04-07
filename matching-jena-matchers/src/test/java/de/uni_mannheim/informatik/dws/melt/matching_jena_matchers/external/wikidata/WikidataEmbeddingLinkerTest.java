package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wikidata;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WikidataEmbeddingLinkerTest {


    static String entityFilePath;

    static WikidataEmbeddingLinker linker;

    @BeforeAll
    static void setUp(){
        entityFilePath = loadFile("wikidata_embedding_entities.txt").getAbsolutePath();
        linker = new WikidataEmbeddingLinker(entityFilePath);
    }

    @Test
    void linkToSingleConcept(){
        // linkable and in entity list
        assertNotNull(linker.linkToSingleConcept("Jan Philipp Portisch"));

        // linkable but not contained in entity list
        assertNull(linker.linkToSingleConcept("Heiko Paulheim"));
    }

    @Test
    void linkToPotentiallyMultipleConcepts(){
        // linkable and in entity list
        assertNotNull(linker.linkToPotentiallyMultipleConcepts("Jan Philipp Portisch"));

        // linkable but not contained in entity list
        assertNull(linker.linkToPotentiallyMultipleConcepts("Heiko Paulheim"));

        // linkable and in entity list
        assertNotNull(linker.linkToPotentiallyMultipleConcepts("Jan Philipp Portisch Sven Hertling"));

        // linkable and in entity list
        assertNull(linker.linkToPotentiallyMultipleConcepts("Jan Philipp Portisch Heiko Paulheim"));

        Set<String> notFound = linker.getUrisNotFound();
        assertTrue(notFound.size() > 0);
    }

    @Test
    void constructorTest(){
        assertEquals(5, linker.uris.size());
        linker.uris.contains("http://www.wikidata.org/entity/Q29673815");
    }

    @Test
    void getNameOfLinker() {
        assertNotNull(linker.getNameOfLinker());
    }

    @Test
    void setNameOfLinker(){
        linker.setNameOfLinker("Hello");
        assertEquals("Hello", linker.getNameOfLinker());
        linker.setNameOfLinker(null);
        assertNotNull(linker.getNameOfLinker());
    }

    /**
     * Helper function to load files in class path that contain spaces.
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private static File loadFile(String fileName){
        try {
            File result =
                    FileUtils.toFile(WikidataEmbeddingLinkerTest.class.getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception){
            exception.printStackTrace();
            fail("Could not load file.", exception);
            return null;
        }
    }
}