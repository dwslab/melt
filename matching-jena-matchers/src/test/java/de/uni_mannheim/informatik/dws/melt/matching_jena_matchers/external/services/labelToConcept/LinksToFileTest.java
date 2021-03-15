package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.ValueExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet.WordNetKnowledgeSource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet.WordNetLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.valueExtractors.ValueExtractorAllAnnotationProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations.readSetFromFile;
import static org.junit.jupiter.api.Assertions.*;

class LinksToFileTest {


    @AfterAll
    static void cleanUp(){
        File fileToBeDeleted = new File("./trackLinks.txt");
        if(fileToBeDeleted.exists()){
            fileToBeDeleted.delete();
        }
        fileToBeDeleted = new File("./testCaseLinks.txt");
        if(fileToBeDeleted.exists()){
            fileToBeDeleted.delete();
        }
    }

    @Test
    void writeLinksToFileListTestCase(){
        List<TestCase> myList = new LinkedList<>();
        myList.add(TrackRepository.Conference.V1.getFirstTestCase());
        myList.add(TrackRepository.Anatomy.Default.getFirstTestCase());
        File fileToBeWritten = new File("./tc_list_links.txt");
        fileToBeWritten.deleteOnExit();
        ValueExtractor extractor = new ValueExtractorAllAnnotationProperties();
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
        ValueExtractor extractor = new ValueExtractorAllAnnotationProperties();
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
            ValueExtractor extractor = new ValueExtractorAllAnnotationProperties();
            LabelToConceptLinker linker = new WordNetLinker(new WordNetKnowledgeSource());
            LinksToFile.writeLinksToFile(fileToBeWritten, myList, extractor, linker, 100);
            assertFalse(fileToBeWritten.exists());
        } catch (Exception e){
            fail(e);
        }
    }

    @Test
    void typeAxiom(){
        List<String> myList = new LinkedList<>();
        myList.add("Hello");
        assertEquals(String.class, myList.get(0).getClass());
    }

    @Test
    void writeLinksToFile() {
        try {
            File fileToBeWritten = new File("./trackLinks.txt");
            fileToBeWritten.deleteOnExit();
            ValueExtractor extractor = new ValueExtractorAllAnnotationProperties();
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
        ValueExtractor extractor = new ValueExtractorAllAnnotationProperties();
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
}