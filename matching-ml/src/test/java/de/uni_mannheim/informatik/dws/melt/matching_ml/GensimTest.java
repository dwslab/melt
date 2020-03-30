package de.uni_mannheim.informatik.dws.melt.matching_ml;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class GensimTest {

    private static Gensim gensim;

    @BeforeAll
    public static void setup(){
        gensim = Gensim.getInstance();
    }

    @AfterAll
    public static void tearDown(){
        gensim.shutDown();
    }

    private static Logger LOGGER = LoggerFactory.getLogger(GensimTest.class);

    @Test
    /**
     * Default test with cache.
     */
    void isInVocabulary() {
        gensim.setVectorCaching(true);
        // test case 1: model file
        String pathToModel = getPathOfResource("test_model");
        assertTrue(gensim.isInVocabulary("Europe", pathToModel));
        assertTrue(gensim.isInVocabulary("united", pathToModel));
        assertFalse(gensim.isInVocabulary("China", pathToModel));

        // test case 2: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        assertTrue(gensim.isInVocabulary("Europe", pathToVectorFile));
        assertTrue(gensim.isInVocabulary("united", pathToVectorFile));
        assertFalse(gensim.isInVocabulary("China", pathToVectorFile));
    }

    @Test
    /**
     * Default test without cache.
     */
    void isInVocabularyNoCaching() {
        gensim.setVectorCaching(false);
        // test case 1: model file
        String pathToModel = getPathOfResource("test_model");
        assertTrue(gensim.isInVocabulary("Europe", pathToModel));
        assertTrue(gensim.isInVocabulary("united", pathToModel));
        assertFalse(gensim.isInVocabulary("China", pathToModel));

        // test case 2: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        assertTrue(gensim.isInVocabulary("Europe", pathToVectorFile));
        assertTrue(gensim.isInVocabulary("united", pathToVectorFile));
        assertFalse(gensim.isInVocabulary("China", pathToVectorFile));
    }


    @Test
    /**
     * Default test with cache.
     */
    void getSimilarity() {
        gensim.setVectorCaching(true);
        // test case 1: model file
        String pathToModel = getPathOfResource("test_model");
        double similarity = gensim.getSimilarity("Europe", "united", pathToModel);
        assertTrue(similarity > 0);

        // test case 2: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        similarity = gensim.getSimilarity("Europe", "united", pathToVectorFile);
        assertTrue(similarity > 0);
    }


    @Test
    void getSimilarityNoCaching() {
        gensim.setVectorCaching(false);
        // test case 1: model file
        String pathToModel = getPathOfResource("test_model");
        double similarity = gensim.getSimilarity("Europe", "united", pathToModel);
        assertTrue(similarity > 0);

        // test case 2: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        similarity = gensim.getSimilarity("Europe", "united", pathToVectorFile);
        assertTrue(similarity > 0);
    }


    @Test
    void testMultipleShutdownCallsAndRestarts() {
        gensim.setVectorCaching(false);
        // test case 1: model file
        gensim.shutDown();
        gensim = Gensim.getInstance();
        String pathToModel = getPathOfResource("test_model");
        double similarity = gensim.getSimilarity("Europe", "united", pathToModel);
        assertTrue(similarity > 0);

        // test case 2: vector file
        gensim.shutDown();
        gensim = Gensim.getInstance();
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        similarity = gensim.getSimilarity("Europe", "united", pathToVectorFile);
        assertTrue(similarity > 0);
    }


    @Test
    /**
     * Default test with cache.
     */
    void getVector() {
        gensim.setVectorCaching(true);
        // test case 1: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        Double[] europeVector = gensim.getVector("Europe", pathToVectorFile);
        assertEquals(100, europeVector.length);

        Double[] unitedVector = gensim.getVector("united", pathToVectorFile);

        double similarityJava = (gensim.cosineSimilarity(europeVector, unitedVector));
        double similarityPyhton = (gensim.getSimilarity("Europe", "united", pathToVectorFile));
        assertEquals(similarityJava, similarityPyhton, 0.0001);

        // test case 2: model file
        String pathToModel = getPathOfResource("test_model");
        europeVector = gensim.getVector("Europe", pathToModel);
        assertEquals(100, europeVector.length);
    }


    @Test
    /**
     * Test without cache.
     */
    void getVectorNoCaching() {
        gensim.setVectorCaching(false);
        // test case 1: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        Double[] europeVector = gensim.getVector("Europe", pathToVectorFile);
        assertEquals(100, europeVector.length);

        Double[] unitedVector = gensim.getVector("united", pathToVectorFile);

        double similarityJava = (gensim.cosineSimilarity(europeVector, unitedVector));
        double similarityPython = (gensim.getSimilarity("Europe", "united", pathToVectorFile));
        assertEquals(similarityJava, similarityPython, 0.0001);

        // test case 2: model file
        String pathToModel = getPathOfResource("test_model");
        europeVector = gensim.getVector("Europe", pathToModel);
        assertEquals(100, europeVector.length);
    }

    @Test
    void writeModelAsTextFile() {
        // "normal" training task
        String testFilePath = getPathOfResource("testInputForWord2Vec.txt");
        String fileToWrite = "./freudeWord2vec.kv";
        assertTrue(gensim.trainWord2VecModel(fileToWrite, testFilePath, Word2VecConfiguration.CBOW));

        File vectorFile = new File(fileToWrite);
        File modelFile = new File(fileToWrite.substring(0, fileToWrite.length() - 3));
        assertTrue(vectorFile.exists(), "No vector file was written.");
        assertTrue(modelFile.exists(), "No model file was written.");
        assertTrue(gensim.getSimilarity("Menschen", "Brüder", fileToWrite) > -1.0);

        gensim.writeModelAsTextFile(fileToWrite, "./testTextVectors.txt");
        File writtenFile = new File("./testTextVectors.txt");
        assertTrue(writtenFile.exists());
        assertTrue(getNumberOfLines(writtenFile) > 10);

        String entityFile = getPathOfResource("freudeSubset.txt");
        gensim.writeModelAsTextFile(fileToWrite, "./testTextVectors2.txt", entityFile);
        File writtenFile2 = new File("./testTextVectors2.txt");
        assertTrue(writtenFile2.exists());
        assertTrue(getNumberOfLines(writtenFile2) <= 2);

        // cleaning up
        writtenFile2.delete();
        writtenFile.delete();
        modelFile.delete();
        vectorFile.delete();
    }

    @Test
    void trainWord2VecModelSG() {
        String testFilePath = getPathOfResource("testInputForWord2Vec.txt");
        String vectorFilePath = "./freudeWord2vec_sg.kv";
        assertTrue(gensim.trainWord2VecModel(vectorFilePath, testFilePath, Word2VecConfiguration.SG));

        File vectorFile = new File(vectorFilePath);
        File modelFile = new File(vectorFilePath.substring(0, vectorFilePath.length() - 3));
        assertTrue(vectorFile.exists(), "No vector file was written.");
        assertTrue(modelFile.exists(), "No model file was written.");
        double similarity = gensim.getSimilarity("Menschen", "Brüder", vectorFilePath);
        assertTrue(similarity > -1.0, "Problem with the simliarity. Similarity: " + similarity);

        // cleaning up
        modelFile.delete();
        vectorFile.delete();
    }

    @Test
    void trainWord2VecModelCBOW() {
        String testFilePath = getPathOfResource("testInputForWord2Vec.txt");
        String vectorFilePath = "./freudeWord2vec.kv";
        assertTrue(gensim.trainWord2VecModel(vectorFilePath, testFilePath, Word2VecConfiguration.CBOW));

        File vectorFile = new File(vectorFilePath);
        File modelFile = new File(vectorFilePath.substring(0, vectorFilePath.length() - 3));
        assertTrue(vectorFile.exists(), "No vector file was written.");
        assertTrue(modelFile.exists(), "No model file was written.");
        double similarity = gensim.getSimilarity("Menschen", "Brüder", vectorFilePath);
        assertTrue(similarity > -1.0, "Problem with the simliarity. Similarity: " + similarity);

        // cleaning up
        modelFile.delete();
        vectorFile.delete();
    }

    @Test
    void externalResourcesDirectory(){
        // shut down
        gensim.shutDown();

        // reinitialize
        File externalResourcesDirectory = new File("./ext/");
        gensim = Gensim.getInstance(externalResourcesDirectory);
        File serverFile = new File(externalResourcesDirectory, "python_server.py");
        assertTrue(serverFile.exists());
        try {
            FileUtils.deleteDirectory(externalResourcesDirectory);
        } catch (IOException e) {
            LOGGER.info("Cleanup failed.");
            e.printStackTrace();
        }

        // shut down again to keep using default resources directory
        gensim.shutDown();

        try {
            FileUtils.deleteDirectory(externalResourcesDirectory);
        } catch (IOException e) {
            LOGGER.error("Failed to clean up external resources directory.");
        }
    }

    private String getPathOfResource(String resource){
        try {
            URL res = getClass().getClassLoader().getResource(resource);
            File file = Paths.get(res.toURI()).toFile();
            return file.getCanonicalPath();
        } catch (URISyntaxException | IOException ex) {
            LOGGER.info("Cannot create path of resource", ex);
            return "";
        }
    }

    /**
     * Helper method to obtain the number of read lines.
     * @param file File to be read.
     * @return Number of lines in the file.
     */
    private static int getNumberOfLines(File file){
        int linesRead = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while(br.readLine() != null){
                linesRead++;
            }
            br.close();
        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        return linesRead;
    }

}