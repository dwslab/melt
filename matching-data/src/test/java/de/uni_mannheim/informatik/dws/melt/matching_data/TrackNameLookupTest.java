package de.uni_mannheim.informatik.dws.melt.matching_data;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackNameLookup;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackNameLookupTest {


    @Test
    public void getTestByString(){
        assertEquals(TrackRepository.Anatomy.Default, TrackNameLookup.getTrackByString("anatomy"));
        assertNull(TrackNameLookup.getTrackByString("does-not-exist"));
    }

    @Test
    public void getTrackOptions(){
        List<String> result = TrackNameLookup.getTrackOptions();
        assertNotNull(result);
        assertTrue(result.size() > 3);

        assertTrue(result.contains("instancematching"));
        assertFalse(result.contains("instancematching-geolink-cruise"));
    }

}