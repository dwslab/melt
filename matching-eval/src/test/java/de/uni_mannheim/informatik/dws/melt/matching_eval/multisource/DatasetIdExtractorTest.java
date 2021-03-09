package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DatasetIdExtractorTest {


    @Test
    void testConferenceIDExtractor() {
        testIDExtractor(TrackRepository.Conference.V1);
        //writeDatasetIds(TrackRepository.Conference.V1, 20);
    }
    
    //@Test // not to download KG track every time in CI
    void testKGTrackIDExtractor() {
        testIDExtractor(TrackRepository.Knowledgegraph.V3);
        //writeDatasetIds(TrackRepository.Knowledgegraph.V3, 500);
    }
    
    //@Test // not to download largebio track every time in CI
    void testLargebio() {
        testIDExtractor(TrackRepository.Largebio.V2016.ONLY_WHOLE);        
        //writeDatasetIds(TrackRepository.Largebio.V2016.ONLY_WHOLE, 100);        
    }
    
    private void testIDExtractor(Track track){
        DatasetIDExtractor extractor = ExecutorMultiSource.getMostSpecificDatasetIdExtractor(track);
        
        for(TestCase testCase : track.getTestCases()){
            Set<String> sourceIds = new HashSet<>();
            Set<String> targetIds = new HashSet<>();
            for(Correspondence c : testCase.getParsedReferenceAlignment()){
                sourceIds.add(extractor.getDatasetID(c.getEntityOne()));
                targetIds.add(extractor.getDatasetID(c.getEntityTwo()));
            }
            assertEquals(1, sourceIds.size());
            assertEquals(1, targetIds.size());
        }
    }
    
    private void writeDatasetIds(Track track, int uriPerLine){
        DatasetIDExtractor extractor = ExecutorMultiSource.getMostSpecificDatasetIdExtractor(track);
        Map<String, Set<String>> datssets = new HashMap<>();
        for(TestCase testCase : track.getTestCases()){
            extractDatasetID(testCase.getSourceOntology(OntModel.class), extractor, datssets);
            extractDatasetID(testCase.getTargetOntology(OntModel.class), extractor, datssets);
        }
        
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("datasetIds.txt"))){
            for(Entry<String, Set<String>> entry : datssets.entrySet()){
                writer.write(entry.getKey());writer.newLine();
                write(entry.getValue(), writer, uriPerLine);
            }
        } catch (IOException ex) {}
    }
    
    
    private void write(Set<String> content, BufferedWriter writer, int uriPerLine) throws IOException{
        Iterator<String> it = content.iterator();
        int i = 0;
        writer.write("\t\t");
        while(it.hasNext()){
           if(i % uriPerLine == 0 && i > 0){
               writer.newLine();
               writer.write("\t\t");
           }
           writer.write(it.next());
           writer.write(", ");
           i++;
        }
        writer.newLine();
    }
    
    private void extractDatasetID(OntModel model, DatasetIDExtractor extractor, Map<String, Set<String>> datssets){
        ResIterator i = model.listSubjects();
        while(i.hasNext()){
            Resource r = i.next();
            if(r.isURIResource() == false)
                continue;
            String uri = r.getURI();

            String datsetId = extractor.getDatasetID(uri);                    
            datssets.computeIfAbsent(datsetId, __-> new HashSet<>()).add(uri);
        }
    }
}
