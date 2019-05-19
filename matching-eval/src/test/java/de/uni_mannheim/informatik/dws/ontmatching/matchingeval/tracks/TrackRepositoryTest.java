package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrackRepositoryTest {

    @Test
    public void testTracks(){
        // tests downloading process and implementation
        assertTrue(TrackRepository.Conference.V1.getTestCases().size() > 0);
        assertTrue(TrackRepository.Anatomy.Default.getTestCases().size() > 0);
    }

}