package de.uni_mannheim.informatik.dws.melt.matching_ml;

import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.Word2VecConfiguration;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.Word2VecType;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class PythonServerTest {


    private static PythonServer pythonServer;

    @BeforeAll
    public static void setup(){
        pythonServer = PythonServer.getInstance();
    }

    @AfterAll
    public static void tearDown(){
        PythonServer.shutDown();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PythonServerTest.class);

    /**
     * Default test with cache.
     */
    @Test
    void isInVocabulary() {
        pythonServer.setVectorCaching(true);
        // test case 1: model file
        String pathToModel = getPathOfResource("test_model");
        assertTrue(pythonServer.isInVocabulary("Europe", pathToModel));
        assertTrue(pythonServer.isInVocabulary("united", pathToModel));
        assertFalse(pythonServer.isInVocabulary("China", pathToModel));

        // test case 2: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        assertNotNull(pathToVectorFile, "Test resource not found.");
        File vectorFile = new File(pathToVectorFile);
        assertTrue(pythonServer.isInVocabulary("Europe", vectorFile));
        assertTrue(pythonServer.isInVocabulary("united", vectorFile));
        assertFalse(pythonServer.isInVocabulary("China", vectorFile));
    }

    @Test
    void checkRequirements(){
        assertTrue(PythonServer.checkRequirements());
    }

    /**
     * Default test without cache.
     */
    @Test
    void isInVocabularyNoCaching() {
        pythonServer.setVectorCaching(false);
        // test case 1: model file
        String pathToModel = getPathOfResource("test_model");
        assertTrue(pythonServer.isInVocabulary("Europe", pathToModel));
        assertTrue(pythonServer.isInVocabulary("united", pathToModel));
        assertFalse(pythonServer.isInVocabulary("China", pathToModel));

        // test case 2: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        assertTrue(pythonServer.isInVocabulary("Europe", pathToVectorFile));
        assertTrue(pythonServer.isInVocabulary("united", pathToVectorFile));
        assertFalse(pythonServer.isInVocabulary("China", pathToVectorFile));
    }

    /**
     * Default test with cache.
     */
    @Test
    void getSimilarity() {
        pythonServer.setVectorCaching(true);
        // test case 1: model file
        String pathToModel = getPathOfResource("test_model");
        double similarity = pythonServer.getSimilarity("Europe", "united", pathToModel);
        assertTrue(similarity > 0);

        // test case 2: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        similarity = pythonServer.getSimilarity("Europe", "united", pathToVectorFile);
        assertTrue(similarity > 0);
    }

    @Test
    void getSimilarityNoCaching() {
        pythonServer.setVectorCaching(false);
        // test case 1: model file
        String pathToModel = getPathOfResource("test_model");
        double similarity = pythonServer.getSimilarity("Europe", "united", pathToModel);
        assertTrue(similarity > 0);

        // test case 2: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        similarity = pythonServer.getSimilarity("Europe", "united", pathToVectorFile);
        assertTrue(similarity > 0);
    }

    @Test
    void testMultipleShutdownCallsAndRestarts() {
        pythonServer.setVectorCaching(false);
        // test case 1: model file
        PythonServer.shutDown();
        pythonServer = PythonServer.getInstance();
        String pathToModel = getPathOfResource("test_model");
        double similarity = pythonServer.getSimilarity("Europe", "united", pathToModel);
        assertTrue(similarity > 0);

        // test case 2: vector file
        PythonServer.shutDown();
        pythonServer = PythonServer.getInstance();
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        similarity = pythonServer.getSimilarity("Europe", "united", pathToVectorFile);
        assertTrue(similarity > 0);
    }

    /**
     * Default test with cache.
     */
    @Test
    void getVector() {
        pythonServer.setVectorCaching(true);
        // test case 1: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        Double[] europeVector = pythonServer.getVector("Europe", pathToVectorFile);
        assertEquals(100, europeVector.length);

        Double[] unitedVector = pythonServer.getVector("united", pathToVectorFile);

        double similarityJava = (PythonServer.cosineSimilarity(europeVector, unitedVector));
        double similarityPyhton = (pythonServer.getSimilarity("Europe", "united", pathToVectorFile));
        assertEquals(similarityJava, similarityPyhton, 0.0001);

        // test case 2: model file
        String pathToModel = getPathOfResource("test_model");
        europeVector = pythonServer.getVector("Europe", pathToModel);
        assertEquals(100, europeVector.length);
    }

    /**
     * Test without cache.
     */
    @Test
    void getVectorNoCaching() {
        pythonServer.setVectorCaching(false);
        // test case 1: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        Double[] europeVector = pythonServer.getVector("Europe", pathToVectorFile);
        assertEquals(100, europeVector.length);

        Double[] unitedVector = pythonServer.getVector("united", pathToVectorFile);

        double similarityJava = (PythonServer.cosineSimilarity(europeVector, unitedVector));
        double similarityPython = (pythonServer.getSimilarity("Europe", "united", pathToVectorFile));
        assertEquals(similarityJava, similarityPython, 0.0001);

        // test case 2: model file
        String pathToModel = getPathOfResource("test_model");
        europeVector = pythonServer.getVector("Europe", pathToModel);
        assertEquals(100, europeVector.length);
    }

    /**
     * Check whether vectors can be read using two different ports.
     * Test without cache.
     */
    @ParameterizedTest
    @ValueSource(ints = {1808, 1809})
    void getVectorNoCachingDifferentPorts(int port) {
        PythonServer.shutDown();
        PythonServer.setPort(port);
        assertEquals(port, PythonServer.getPort());
        pythonServer = PythonServer.getInstance();
        pythonServer.setVectorCaching(false);
        // test case 1: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        Double[] europeVector = pythonServer.getVector("Europe", pathToVectorFile);
        assertEquals(100, europeVector.length);

        Double[] unitedVector = pythonServer.getVector("united", pathToVectorFile);

        double similarityJava = (PythonServer.cosineSimilarity(europeVector, unitedVector));
        double similarityPython = (pythonServer.getSimilarity("Europe", "united", pathToVectorFile));
        assertEquals(similarityJava, similarityPython, 0.0001);

        // test case 2: model file
        String pathToModel = getPathOfResource("test_model");
        europeVector = pythonServer.getVector("Europe", pathToModel);
        assertEquals(100, europeVector.length);
    }

    @Test
    void setGetPort(){
        int testPort = 41194;
        PythonServer.setPort(testPort);
        assertNotEquals(testPort, PythonServer.getPort());
        PythonServer.shutDown();
        PythonServer.setPort(testPort);
        pythonServer = PythonServer.getInstance();
        assertEquals(testPort, PythonServer.getPort());
        assertTrue(PythonServer.getServerUrl().contains("41194"));
    }

    @Test
    void writeModelAsTextFile() {
        // "normal" training task
        String testFilePath = getPathOfResource("testInputForWord2Vec.txt");
        String fileToWrite = "./freudeWord2vec.kv";
        assertTrue(pythonServer.trainWord2VecModel(fileToWrite, testFilePath, new Word2VecConfiguration(Word2VecType.CBOW)));

        File vectorFile = new File(fileToWrite);
        File modelFile = new File(fileToWrite.substring(0, fileToWrite.length() - 3));
        assertTrue(vectorFile.exists(), "No vector file was written.");
        assertTrue(modelFile.exists(), "No model file was written.");
        assertTrue(pythonServer.getSimilarity("Menschen", "Brüder", fileToWrite) > -1.0);

        pythonServer.writeModelAsTextFile(fileToWrite, "./testTextVectors.txt");
        File writtenFile = new File("./testTextVectors.txt");
        assertTrue(writtenFile.exists());
        assertTrue(getNumberOfLines(writtenFile) > 10);

        String entityFile = getPathOfResource("freudeSubset.txt");
        pythonServer.writeModelAsTextFile(fileToWrite, "./testTextVectors2.txt", entityFile);
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
        assertTrue(pythonServer.trainWord2VecModel(vectorFilePath, testFilePath, new Word2VecConfiguration(Word2VecType.SG)));

        File vectorFile = new File(vectorFilePath);
        File modelFile = new File(vectorFilePath.substring(0, vectorFilePath.length() - 3));
        assertTrue(vectorFile.exists(), "No vector file was written.");
        assertTrue(modelFile.exists(), "No model file was written.");
        double similarity = pythonServer.getSimilarity("Menschen", "Brüder", vectorFilePath);
        assertTrue(similarity > -1.0, "Problem with the simliarity. Similarity: " + similarity);

        //contains "Hymne" (count = 1)
        assertTrue(pythonServer.isInVocabulary("Hymne", vectorFilePath));

        //contains "Freude" (count > 3)
        assertTrue(pythonServer.isInVocabulary("Freude", vectorFilePath));

        //contains "Bösewicht," (count = 1)
        assertTrue(pythonServer.isInVocabulary("Bösewicht,", vectorFilePath));

        int vocabularySize = pythonServer.getVocabularySize(vectorFilePath);
        assertTrue(vocabularySize > 100);

        // cleaning up
        modelFile.delete();
        vectorFile.delete();
    }

    @Test
    void trainWord2VecModelWithWalkDirectory() {
        String testFilePath = getPathOfResource("walk_directory_test");
        if(testFilePath == null) fail("Test resource not found.");
        String vectorFilePath = "./w2v_directory_test.kv";
        assertTrue(pythonServer.trainWord2VecModel(vectorFilePath, testFilePath, new Word2VecConfiguration(Word2VecType.SG)));
        File vectorFile = new File(vectorFilePath);
        File modelFile = new File(vectorFilePath.substring(0, vectorFilePath.length() - 3));
        assertTrue(vectorFile.exists(), "No vector file was written.");
        assertTrue(modelFile.exists(), "No model file was written.");

        //contains "Hymne" (count = 1) in file "an_die_freude.txt"
        assertTrue(pythonServer.isInVocabulary("Hymne", vectorFilePath));

        //contains "Freude" (count > 3) in file "an_die_freude.txt"
        assertTrue(pythonServer.isInVocabulary("Freude", vectorFilePath));

        // contains "Stolz" (count = 1) in file "auf_die_europa.txt"
        assertTrue(pythonServer.isInVocabulary("Stolz", vectorFilePath));

        // contains "Europen" (count = 1) in file "auf_die_europa.txt"
        assertTrue(pythonServer.isInVocabulary("Europen", vectorFilePath));

        // cleaning up
        modelFile.delete();
        vectorFile.delete();
    }

    @Test
    void trainWord2VecModelCBOW() {
        String testFilePath = getPathOfResource("testInputForWord2Vec.txt");
        String vectorFilePath = "./freudeWord2vec.kv";
        assertTrue(pythonServer.trainWord2VecModel(vectorFilePath, testFilePath, new Word2VecConfiguration(Word2VecType.CBOW)));

        File vectorFile = new File(vectorFilePath);
        File modelFile = new File(vectorFilePath.substring(0, vectorFilePath.length() - 3));
        assertTrue(vectorFile.exists(), "No vector file was written.");
        assertTrue(modelFile.exists(), "No model file was written.");
        double similarity = pythonServer.getSimilarity("Menschen", "Brüder", vectorFilePath);
        assertTrue(similarity > -1.0, "Problem with the similarity. Similarity: " + similarity);

        // cleaning up
        modelFile.delete();
        vectorFile.delete();
    }

    @Test
    void externalResourcesDirectory(){
        // shut down
        PythonServer.shutDown();

        // reinitialize
        File externalResourcesDirectory = new File("./ext/");
        pythonServer = PythonServer.getInstance(externalResourcesDirectory);
        File serverFile = new File(externalResourcesDirectory, "python_server_melt.py");
        assertTrue(serverFile.exists());

        // shut down again to keep using default resources directory
        PythonServer.shutDown();

        // we need to restart for subsequent tests
        pythonServer = PythonServer.getInstance();

        try {
            FileUtils.deleteDirectory(externalResourcesDirectory);
        } catch (IOException e) {
            LOGGER.error("Failed to clean up external resources directory.", e);
        }
    }

    @Test
    void getVocabularyTerms(){
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        Set<String> result = pythonServer.getVocabularyTerms(pathToVectorFile);
        assertTrue(result.size() > 0);
        assertTrue(result.contains("Europe"));
    }

    @Test
    void writeVocabularyToFile() {
        File vocabFile = new File("./gensim_vocab.txt");
        vocabFile.deleteOnExit();
        pythonServer.writeVocabularyToFile(getPathOfResource("test_model_vectors.kv"), vocabFile);
        assertTrue(vocabFile.exists());

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(vocabFile), StandardCharsets.UTF_8));
            Set<String> vocabulary = new HashSet<>();
            String line;
            while((line = reader.readLine()) != null){
                vocabulary.add(line);
            }
            assertTrue(vocabulary.contains("Europe"));
        } catch (IOException  e) {
            fail(e);
        }
    }

    /**
     * Helper method to obtain the canonical path of a (test) resource.
     * @param resourceName File/directory name.
     * @return Canonical path of resource.
     */
    private String getPathOfResource(String resourceName){
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
            LOGGER.error("File not found " + file.getAbsolutePath(), fnfe);
        } catch (IOException ioe){
            LOGGER.error("IOException while trying to get number of lines of file " + file.getAbsolutePath(), ioe);
        }
        return linesRead;
    }

}