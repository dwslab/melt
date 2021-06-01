package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia.DBpediaKnowledgeSource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia.DBpediaLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet.WordNetKnowledgeSource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet.WordNetLinker;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations.readSetFromFile;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorAllAnnotationProperties;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test requires a working internet connection.
 */
class LinksToFileTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(LinksToFileTest.class);

    @AfterAll
    static void cleanUp(){
        deleteFileIfExists("./trackLinks.txt");
        deleteFileIfExists("./testCaseLinks.txt");
        deleteFileIfExists("./testCaseLinksDBpedia.txt");
        deleteFileIfExists("./tc_list_links.txt");
        deletePersistenceDirectory();
    }

    /**
     * Delete the persistence directory.
     */
    public static void deletePersistenceDirectory() {
        PersistenceService.getService().closePersistenceService();
        File result = new File(PersistenceService.PERSISTENCE_DIRECTORY);
        if (result.exists() && result.isDirectory()) {
            try {
                FileUtils.deleteDirectory(result);
            } catch (IOException e) {
                LOGGER.error("Failed to remove persistence directory.", e);
            }
        }
    }

    static void deleteFileIfExists(String filePath){
        File fileToBeDeleted = new File(filePath);
        if(fileToBeDeleted.exists()){
            fileToBeDeleted.delete();
        }
    }

    @Test
    void writeLinksToFileListTestCase(){
        List<TestCase> myList = new LinkedList<>();
        myList.add(TrackRepository.Conference.V1.getTestCase("confof-iasted"));
        myList.add(TrackRepository.Anatomy.Default.getFirstTestCase());
        File fileToBeWritten = new File("./tc_list_links.txt");
        fileToBeWritten.deleteOnExit();
        TextExtractor extractor = new TextExtractorAllAnnotationProperties();
        LabelToConceptLinker linker = new WordNetLinker(new WordNetKnowledgeSource());
        LinksToFile.writeLinksToFile(fileToBeWritten, myList, extractor, linker, 1);

        // make sure that a file has been written
        assertTrue(fileToBeWritten.exists());

        Set<String> links = readSetFromFile(fileToBeWritten);
        assertTrue(links.size() > 10);

        // make sure we find terms from both test cases
        assertTrue(links.contains("common iliac vein"));
        assertTrue(links.contains("overhead projector"));
    }

    @Test
    void writeLinksToFileListTrack(){
        List<Track> myList = new LinkedList<>();
        myList.add(TrackRepository.Conference.V1);
        myList.add(TrackRepository.Anatomy.Default);
        File fileToBeWritten = new File("./track_list_links.txt");
        fileToBeWritten.deleteOnExit();
        TextExtractor extractor = new TextExtractorAllAnnotationProperties();
        LabelToConceptLinker linker = new WordNetLinker(new WordNetKnowledgeSource());
        LinksToFile.writeLinksToFile(fileToBeWritten, myList, extractor, linker, 1);

        // make sure that a file has been written
        assertTrue(fileToBeWritten.exists());

        Set<String> links = readSetFromFile(fileToBeWritten);
        assertTrue(links.size() > 10);

        // make sure we find terms from both test cases
        assertTrue(links.contains("common iliac vein"));
        assertTrue(links.contains("overhead projector"));
    }

    @Test
    void writeLinksToFileListErrorCase(){
        try {
            List<String> myList = new LinkedList<>();
            myList.add("Hello");
            File fileToBeWritten = new File("./should_not_be_written.txt");
            fileToBeWritten.deleteOnExit();
            TextExtractor extractor = new TextExtractorAllAnnotationProperties();
            LabelToConceptLinker linker = new WordNetLinker(new WordNetKnowledgeSource());
            LinksToFile.writeLinksToFile(fileToBeWritten, myList, extractor, linker, 100);
            assertFalse(fileToBeWritten.exists());
        } catch (Exception e){
            fail(e);
        }
    }

    @Test
    void writeLinksToFile() {
        try {
            File fileToBeWritten = new File("./trackLinks.txt");
            fileToBeWritten.deleteOnExit();
            TextExtractor extractor = new TextExtractorAllAnnotationProperties();
            LabelToConceptLinker linker = new WordNetLinker(new WordNetKnowledgeSource());
            LinksToFile.writeLinksToFile(fileToBeWritten, TrackRepository.Conference.V1, extractor, linker, 1000);
            assertTrue(fileToBeWritten.exists());
            int sizeBig, sizeSmall;

            // read high max length
            BufferedReader reader = new BufferedReader(new FileReader(fileToBeWritten));
            Set<String> resultSet = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                resultSet.add(line);
            }
            sizeBig = resultSet.size();
            assertTrue(sizeBig > 10);

            // read low max length
            LinksToFile.writeLinksToFile(fileToBeWritten, TrackRepository.Conference.V1, extractor, linker, 1);
            assertTrue(fileToBeWritten.exists());
            reader = new BufferedReader(new FileReader(fileToBeWritten));
            resultSet = new HashSet<>();
            while ((line = reader.readLine()) != null) {
                resultSet.add(line);
            }
            sizeSmall = resultSet.size();
            assertTrue(sizeSmall > 10);

            assertTrue(sizeBig > sizeSmall);
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void testWriteLinksToFile() {
        File fileToBeWritten = new File("./testCaseLinks.txt");
        fileToBeWritten.deleteOnExit();
        TextExtractor extractor = new TextExtractorAllAnnotationProperties();
        LabelToConceptLinker linker = new WordNetLinker(new WordNetKnowledgeSource());
        LinksToFile.writeLinksToFile(fileToBeWritten, TrackRepository.Conference.V1.getFirstTestCase(), extractor, linker, 1000);
        assertTrue(fileToBeWritten.exists());
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileToBeWritten));
            Set<String> resultSet = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                resultSet.add(line);
            }
            assertTrue(resultSet.size() > 10);
        } catch (IOException ioe){
            fail(ioe);
        }
    }

    @Test
    void testWriteLinksToFileMultiConceptLinkerWordnet() {
        File fileToBeWritten = new File("./testCaseLinksWordnet.txt");
        fileToBeWritten.deleteOnExit();
        TextExtractor extractor = new TextExtractorAllAnnotationProperties();
        LabelToConceptLinker linker = new WordNetLinker(new WordNetKnowledgeSource());
        LinksToFile.writeLinksToFile(fileToBeWritten, TrackRepository.Conference.V1.getFirstTestCase(), extractor,
                linker, 3);
        assertTrue(fileToBeWritten.exists());
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileToBeWritten));
            Set<String> resultSet = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                resultSet.add(line);
                // make sure we do not get multi concept links
                assertFalse(line.startsWith("#ML_"));
            }
            assertTrue(resultSet.size() > 10);
        } catch (IOException ioe){
            fail(ioe);
        }
    }

    /**
     * This test requires that the SPARQL DBpedia endpoint is online.
     * Disabled since this may cause problems in the runtime due to HTTP errors.
     */
    @Test
    @Disabled
    void testWriteLinksToFileMultiConceptLinkerDBpedia() {
        File fileToBeWritten = new File("./testCaseLinksDBpedia.txt");
        fileToBeWritten.deleteOnExit();
        TextExtractor extractor = new TextExtractorAllAnnotationProperties();
        LabelToConceptLinker linker = new DBpediaLinker(new DBpediaKnowledgeSource());
        LinksToFile.writeLinksToFile(fileToBeWritten, TrackRepository.Conference.V1.getFirstTestCase(), extractor,
                linker, 3);
        assertTrue(fileToBeWritten.exists());
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileToBeWritten));
            Set<String> resultSet = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                resultSet.add(line);
                // make sure we do not get multi concept links
                assertFalse(line.startsWith("#ML_"));
            }
            assertTrue(resultSet.size() > 10);
        } catch (IOException ioe){
            fail(ioe);
        }
    }
}