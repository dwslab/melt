package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class DBpediaEmbeddingLinkerTest {


    static DBpediaEmbeddingLinker linker;

    @BeforeAll
    static void setUp(){
        DBpediaKnowledgeSource dks = new DBpediaKnowledgeSource(false);
        DBpediaLinker dBpediaLinker = new DBpediaLinker(dks);
        linker = new DBpediaEmbeddingLinker(dBpediaLinker,
                loadFile("dbpedia_embedding_entities.txt").getAbsolutePath());
    }

    @Test
    void linkToSingleConcept() {
        // linkable and in entity list
        String link = linker.linkToSingleConcept("mius");
        assertNotNull(link);

        //linkable but not in entity list
        assertNull(linker.linkToSingleConcept("Mia"));

        // linkable and in entity list
        assertNotNull(linker.linkToSingleConcept("trochlear nerve"));
    }

    @Test
    void linkToPotentiallyMultipleConcepts() {
        // linkable and in entity list
        assertNotNull(linker.linkToPotentiallyMultipleConcepts("trochlear nerve research institute"));

        // linkable but not in entity list
        assertNull(linker.linkToPotentiallyMultipleConcepts("trochlear nerve MIA"));
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
                    FileUtils.toFile(DBpediaEmbeddingLinkerTest.class.getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception){
            exception.printStackTrace();
            fail("Could not load file.", exception);
            return null;
        }
    }
}