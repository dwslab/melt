package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Developer note:
 * - This test requires a working internet connection.
 * - The SEALS servers must be online.
 * - While it might be desirable to test all tracks, note that in the testing step of the continuous integration pipeline, all test cases
 * are re-downloaded which significantly slows down the build process.
 */
class TrackRepositoryTest {

    @Test
    public void testTracks(){
        // tests downloading process and implementation
        assertTrue(TrackRepository.Anatomy.Default.getTestCases().size() > 0);
    }

    @Test
    public void getMultifarmTrackForLanguage(){
        assertTrue(TrackRepository.Multifarm.getMultifarmTrackForLanguage("de").size() == 9);
        assertTrue(TrackRepository.Multifarm.getMultifarmTrackForLanguage("DE").size() == 9);
        assertTrue(TrackRepository.Multifarm.getMultifarmTrackForLanguage("en").size() == 9);
        assertTrue(TrackRepository.Multifarm.getMultifarmTrackForLanguage("EN").size() == 9);
        assertTrue(TrackRepository.Multifarm.getMultifarmTrackForLanguage("ENG").size() == 0);

        boolean appears = false;
        for(Track track : TrackRepository.Multifarm.getMultifarmTrackForLanguage("de")){
            if(track.getName().equals("de-en")) appears = true;
            assertFalse(track.getName().equals("ar-cn"));
        }
        assertTrue(appears, "The method does not return track de-en which should be contained when querying for 'de'.");
    }


    @Test
    public void getSpecificMultifarmTrack(){
        assertTrue(TrackRepository.Multifarm.getSpecificMultifarmTrack("de-en").getName().equals("de-en"));
        assertTrue(TrackRepository.Multifarm.getSpecificMultifarmTrack("de-EN").getName().equals("de-en"));
        assertTrue(TrackRepository.Multifarm.getSpecificMultifarmTrack("DE-EN").getName().equals("de-en"));
        assertNull(TrackRepository.Multifarm.getSpecificMultifarmTrack("ABCXYZ"));

        assertTrue(TrackRepository.Multifarm.getSpecificMultifarmTrack("de", "en").getName().equals("de-en"));
        assertTrue(TrackRepository.Multifarm.getSpecificMultifarmTrack("de", "EN").getName().equals("de-en"));
        assertTrue(TrackRepository.Multifarm.getSpecificMultifarmTrack("DE", "EN").getName().equals("de-en"));
        assertNull(TrackRepository.Multifarm.getSpecificMultifarmTrack("ABC", "XYZ"));
    }

}