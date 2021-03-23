package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.embeddings;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wiktionary.WiktionaryEmbeddingLinker;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class GensimEmbeddingModelTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(GensimEmbeddingModelTest.class);

    public static GensimEmbeddingModel getDummyModel(){
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        String entityFilePath = getPathOfResource("test_model_vectors_vocab.txt");
        GensimEmbeddingModel gem = new GensimEmbeddingModel(pathToVectorFile, entityFilePath, 0.5,
                new WiktionaryEmbeddingLinker(entityFilePath), "Dummy");
        return gem;
    }

    @Test
    void gensimEmbeddingModelTest(){
        GensimEmbeddingModel gem = getDummyModel();
        assertTrue(gem.getSynonymyConfidence("Europe", "united") > 0.0);
        gem.close();
    }

    /**
     * Helper method to obtain the canonical path of a (test) resource.
     * @param resourceName File/directory name.
     * @return Canonical path of resource.
     */
    public static String getPathOfResource(String resourceName){
        try {
            URL res = GensimEmbeddingModelTest.class.getClassLoader().getResource(resourceName);
            if(res == null) throw new IOException();
            File file = Paths.get(res.toURI()).toFile();
            return file.getCanonicalPath();
        } catch (URISyntaxException | IOException ex) {
            LOGGER.info("Cannot create path of resource", ex);
            return null;
        }
    }
}