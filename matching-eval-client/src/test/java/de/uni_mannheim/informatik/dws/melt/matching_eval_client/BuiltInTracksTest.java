package de.uni_mannheim.informatik.dws.melt.matching_eval_client;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuiltInTracksTest {


    @Test
    public void getTestByString(){
        assertEquals(TrackRepository.Anatomy.Default, BuiltInTracks.getTrackByString("anatomy"));
        assertNull(BuiltInTracks.getTrackByString("does-not-exist"));
    }

    @Test
    public void getTrackOptions(){
        List<String> result = BuiltInTracks.getTrackOptions();
        assertNotNull(result);
        assertTrue(result.size() > 3);

        for(String s : result){
            System.out.println(s + "\n");
        }
    }

}