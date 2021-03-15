package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept;

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
import java.util.Set;

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