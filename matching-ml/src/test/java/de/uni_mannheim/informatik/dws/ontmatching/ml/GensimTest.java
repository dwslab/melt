package de.uni_mannheim.informatik.dws.ontmatching.ml;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.MAC;

class GensimTest {

    static Gensim gensim = Gensim.getInstance();

    @Test
    @EnabledOnOs({ MAC })
    void isInVocabulary() {
        String pathToModel = getClass().getClassLoader().getResource("test_model").getPath();
        assertTrue(gensim.isInVocabulary("Europe", pathToModel));
        assertTrue(gensim.isInVocabulary("united", pathToModel));
        assertFalse(gensim.isInVocabulary("China", pathToModel));
    }

    @AfterAll
    static void shutdown(){
        gensim.shutDown();
    }
}