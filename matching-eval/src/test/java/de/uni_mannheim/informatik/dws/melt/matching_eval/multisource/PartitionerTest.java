package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PartitionerTest {


    @Test
    void testConferenceTrackPartitioner() {
        testPartitioner(TrackRepository.Conference.V1);
    }
    
    //@Test // not to download KG track every time in CI
    void testKGTrackPartitioner() {
        testPartitioner(TrackRepository.Knowledgegraph.V3);
    }
    
    //@Test // not to download large bio track every time in CI
    void testLargeBioTrackPartitioner() {
        testPartitioner(TrackRepository.Largebio.V2016.ONLY_WHOLE);
    }
    
    private void testPartitioner(Track track){
        Partitioner partitioner = ExecutorMultiSource.getMostSpecificPartitioner(track);
        for(TestCase testCase : track.getTestCases()){
            for(Correspondence c : testCase.getParsedReferenceAlignment()){
                Map<TestCase, SourceTargetURIs> map = partitioner.partition(Arrays.asList(c.getEntityOne(), c.getEntityTwo()));
                SourceTargetURIs sourceTargetURIs = map.get(testCase);
                assertNotNull(sourceTargetURIs, "Not all URIs in track are correctly partitioned.");
                assertTrue(sourceTargetURIs.containsSourceAndTarget());
            }
        }
    }
}
