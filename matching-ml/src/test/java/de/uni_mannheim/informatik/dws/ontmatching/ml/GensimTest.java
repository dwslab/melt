package de.uni_mannheim.informatik.dws.ontmatching.ml;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.MAC;

class GensimTest {

    static Gensim gensim = Gensim.getInstance();

    @Test
    @EnabledOnOs({ MAC })
    /**
     * Default test with cache.
     */
    void isInVocabulary() {
        gensim.setVectorCaching(true);
        // test case 1: model file
        String pathToModel = getClass().getClassLoader().getResource("test_model").getPath();
        assertTrue(gensim.isInVocabulary("Europe", pathToModel));
        assertTrue(gensim.isInVocabulary("united", pathToModel));
        assertFalse(gensim.isInVocabulary("China", pathToModel));

        // test case 2: vector file
        String pathToVectorFile = getClass().getClassLoader().getResource("test_model_vectors.kv").getPath();
        assertTrue(gensim.isInVocabulary("Europe", pathToVectorFile));
        assertTrue(gensim.isInVocabulary("united", pathToVectorFile));
        assertFalse(gensim.isInVocabulary("China", pathToVectorFile));
    }

    @Test
    @EnabledOnOs({ MAC })
    /**
     * Default test without cache.
     */
    void isInVocabularyNoCaching() {
        gensim.setVectorCaching(false);
        // test case 1: model file
        String pathToModel = getClass().getClassLoader().getResource("test_model").getPath();
        assertTrue(gensim.isInVocabulary("Europe", pathToModel));
        assertTrue(gensim.isInVocabulary("united", pathToModel));
        assertFalse(gensim.isInVocabulary("China", pathToModel));

        // test case 2: vector file
        String pathToVectorFile = getClass().getClassLoader().getResource("test_model_vectors.kv").getPath();
        assertTrue(gensim.isInVocabulary("Europe", pathToVectorFile));
        assertTrue(gensim.isInVocabulary("united", pathToVectorFile));
        assertFalse(gensim.isInVocabulary("China", pathToVectorFile));
    }


    @Test
    @EnabledOnOs({ MAC })
    /**
     * Default test with cache.
     */
    void getSimilarity() {
        gensim.setVectorCaching(true);
        // test case 1: model file
        String pathToModel = getClass().getClassLoader().getResource("test_model").getPath();
        double similarity = gensim.getSimilarity("Europe", "united", pathToModel);
        assertTrue(similarity > 0);

        // test case 2: vector file
        String pathToVectorFile = getClass().getClassLoader().getResource("test_model_vectors.kv").getPath();
        similarity = gensim.getSimilarity("Europe", "united", pathToVectorFile);
        assertTrue(similarity > 0);
    }


    @Test
    @EnabledOnOs({ MAC })
    void getSimilarityNoCaching() {
        gensim.setVectorCaching(false);
        // test case 1: model file
        String pathToModel = getClass().getClassLoader().getResource("test_model").getPath();
        double similarity = gensim.getSimilarity("Europe", "united", pathToModel);
        assertTrue(similarity > 0);

        // test case 2: vector file
        String pathToVectorFile = getClass().getClassLoader().getResource("test_model_vectors.kv").getPath();
        similarity = gensim.getSimilarity("Europe", "united", pathToVectorFile);
        assertTrue(similarity > 0);
    }


    @Test
    @EnabledOnOs({ MAC })
    /**
     * Default test with cache.
     */
    void getVector() {
        gensim.setVectorCaching(true);
        // test case 1: vector file
        String pathToVectorFile = getClass().getClassLoader().getResource("test_model_vectors.kv").getPath();
        Double[] europeVector = gensim.getVector("Europe", pathToVectorFile);
        assertEquals(100, europeVector.length);

        Double[] unitedVector = gensim.getVector("united", pathToVectorFile);

        double similarityJava = (gensim.cosineSimilarity(europeVector, unitedVector));
        double similarityPyhton = (gensim.getSimilarity("Europe", "united", pathToVectorFile));
        assertEquals(similarityJava, similarityPyhton, 0.0001);

        // test case 2: model file
        String pathToModel = getClass().getClassLoader().getResource("test_model").getPath();
        europeVector = gensim.getVector("Europe", pathToModel);
        assertEquals(100, europeVector.length);
    }


    @Test
    @EnabledOnOs({ MAC })
    /**
     * Test without cache.
     */
    void getVectorNoCaching() {
        gensim.setVectorCaching(false);
        // test case 1: vector file
        String pathToVectorFile = getClass().getClassLoader().getResource("test_model_vectors.kv").getPath();
        Double[] europeVector = gensim.getVector("Europe", pathToVectorFile);
        assertEquals(100, europeVector.length);

        Double[] unitedVector = gensim.getVector("united", pathToVectorFile);

        double similarityJava = (gensim.cosineSimilarity(europeVector, unitedVector));
        double similarityPyhton = (gensim.getSimilarity("Europe", "united", pathToVectorFile));
        assertEquals(similarityJava, similarityPyhton, 0.0001);

        // test case 2: model file
        String pathToModel = getClass().getClassLoader().getResource("test_model").getPath();
        europeVector = gensim.getVector("Europe", pathToModel);
        assertEquals(100, europeVector.length);
    }


    @AfterAll
    static void shutdown(){
        gensim.shutDown();
    }
}