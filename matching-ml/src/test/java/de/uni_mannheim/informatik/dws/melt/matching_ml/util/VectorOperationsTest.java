package de.uni_mannheim.informatik.dws.melt.matching_ml.util;

import org.javatuples.Triplet;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VectorOperationsTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(VectorOperationsTest.class);

    @Test
    void readVectorFile() {
        Map<String, Double[]> result = VectorOperations.readVectorFile(getPathOfResource("freude_vectors.txt"));
        assertEquals(12, result.size());

        // check concrete concept
        Double[] deinVector = result.get("dein");
        assertEquals(deinVector.length, 3);
        assertEquals(-0.0020970153, deinVector[0]);
        assertEquals(-0.0008417874, deinVector[1]);
        assertEquals(-0.000101082886, deinVector[2]);

        // error behavior
        assertNull(VectorOperations.readVectorFile("XZY"));
    }

    @Test
    void readVectorFileWithErrors() {
        Map<String, Double[]> result = VectorOperations.readVectorFile(getPathOfResource("freude_vectors_with_errors.txt"));
        assertEquals(12, result.size());

        // make sure the valid ones are still there:
        Double[] deinVector = result.get("dein");
        assertEquals(deinVector.length, 3);
        assertEquals(-0.0020970153, deinVector[0]);
        assertEquals(-0.0008417874, deinVector[1]);
        assertEquals(-0.000101082886, deinVector[2]);

        // make sure invalid concept is not there:
        assertFalse(result.containsKey("Gott"));

        // error behavior
        assertNull(VectorOperations.readVectorFile("XZY"));
    }

    @Test
    void readVectorFileAsFloat() {
        Map<String, Float[]> result = VectorOperations.readVectorFileAsFloat(getPathOfResource("freude_vectors.txt"));
        assertEquals(12, result.size());

        // check concrete concept
        Float[] deinVector = result.get("dein");
        assertEquals(deinVector.length, 3);
        assertEquals(-0.0020970153f, deinVector[0]);
        assertEquals(-0.0008417874f, deinVector[1]);
        assertEquals(-0.000101082886f, deinVector[2]);

        // error behavior
        assertNull(VectorOperations.readVectorFile("XZY"));
    }

    @Test
    void readVectorFileAsFloatWithErrors() {
        Map<String, Float[]> result = VectorOperations.readVectorFileAsFloat(getPathOfResource("freude_vectors_with_errors.txt"));
        assertEquals(12, result.size());

        // make sure the valid ones are still there:
        Float[] deinVector = result.get("dein");
        assertEquals(deinVector.length, 3);
        assertEquals(-0.0020970153f, deinVector[0]);
        assertEquals(-0.0008417874f, deinVector[1]);
        assertEquals(-0.000101082886f, deinVector[2]);

        // make sure invalid concept is not there:
        assertFalse(result.containsKey("Gott"));

        // error behavior
        assertNull(VectorOperations.readVectorFile("XZY"));
    }

    @Test
    void analyzeVectorTextFile() {
        Triplet<Set<String>, Integer, Boolean> result = VectorOperations.analyzeVectorTextFile(new File(getPathOfResource("freude_vectors.txt")));
        Set<String> concepts = result.getValue0();
        assertTrue(concepts.size() == 12);
        assertTrue(concepts.contains("betreten"));
        assertTrue(result.getValue1() == 3);
        assertTrue(result.getValue2());
    }

    /**
     * Helper method to obtain the canonical path of a (test) resource.
     * @param resourceName File/directory name.
     * @return Canonical path of resource.
     */
    public String getPathOfResource(String resourceName){
        try {
            URL res = getClass().getClassLoader().getResource(resourceName);
            if(res == null) throw new IOException();
            File file = Paths.get(res.toURI()).toFile();
            return file.getCanonicalPath();
        } catch (URISyntaxException | IOException ex) {
            LOGGER.info("Cannot create path of resource", ex);
            return null;
        }
    }
}