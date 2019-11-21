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
    void isInVocabulary() {
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
    void getSimilarity() {
        // test case 1: model file
        String pathToModel = getClass().getClassLoader().getResource("test_model").getPath();
        double similarity = gensim.getSimilarity("Europe", "united", pathToModel);
        System.out.println("sim(Europe,united) = " + similarity);
        assertTrue(similarity > 0);

        // test case 2: vector file
        String pathToVectorFile = getClass().getClassLoader().getResource("test_model_vectors.kv").getPath();
        similarity = gensim.getSimilarity("Europe", "united", pathToVectorFile);
        System.out.println("sim(Europe,united) = " + similarity);
        assertTrue(similarity > 0);
    }

    @AfterAll
    static void shutdown(){
        gensim.shutDown();
    }
}