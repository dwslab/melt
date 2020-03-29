package de.uni_mannheim.informatik.dws.melt.matching_eval.tracks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.MAC;

/**
 * Developer note:
 * - This test requires a working internet connection.
 * - The SEALS servers must be online.
 * - While it might be desirable to test all tracks, note that in the testing step of the continuous integration pipeline, all test cases
 * are re-downloaded which significantly slows down the build process.
 * - The track repository is down.
 */
class TrackRepositoryTest {

    @Test
    //@EnabledOnOs({ MAC })
    public void testTracks(){
        // tests downloading process and implementation
        assertTrue(TrackRepository.Anatomy.Default.getTestCases().size() > 0);
    }

    @Test
    //@EnabledOnOs({ MAC })
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
    //@EnabledOnOs({ MAC })
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

    
    //@Test
    public void testMeltRepository(){
        //LargeBio
        assertEquals(6, TrackRepository.Largebio.V2016.ALL.getTestCases().size());
        assertEquals(1, TrackRepository.Largebio.V2016.FMA_NCI_SMALL.getTestCases().size());
        assertEquals(1, TrackRepository.Largebio.V2016.FMA_NCI_WHOLE.getTestCases().size());
        assertEquals(1, TrackRepository.Largebio.V2016.FMA_SNOMED_SMALL.getTestCases().size());
        assertEquals(1, TrackRepository.Largebio.V2016.FMA_SNOMED_WHOLE.getTestCases().size());
        assertEquals(1, TrackRepository.Largebio.V2016.SNOMED_NCI_SMALL.getTestCases().size());
        assertEquals(1, TrackRepository.Largebio.V2016.SNOMED_NCI_WHOLE.getTestCases().size());
        
        //multifarm
        assertEquals(25, TrackRepository.Multifarm.getSpecificMultifarmTrack("de-en").getTestCases().size());
        
        //phenotype
        assertEquals(1, TrackRepository.Phenotype.V2017.DOID_ORDO.getTestCases().size());
        assertEquals(1, TrackRepository.Phenotype.V2017.HP_MP.getTestCases().size());
        
        //anatomy
        assertEquals(1, TrackRepository.Anatomy.Default.getTestCases().size());
        
        //conference
        assertEquals(21, TrackRepository.Conference.V1.getTestCases().size());
        
        //knowledge graph
        assertEquals(5, TrackRepository.Knowledgegraph.V3.getTestCases().size());
        
        //IIMB
        assertEquals(80, TrackRepository.IIMB.V1.getTestCases().size());
        
        //Biodiv
        assertEquals(2, TrackRepository.Biodiv.Default.getTestCases().size());
        
        //Link        
        assertEquals(11, TrackRepository.Link.Default.getTestCases().size());
        
        //Complex
        assertEquals(1, TrackRepository.Complex.GeoLink.getTestCases().size());
        assertEquals(1, TrackRepository.Complex.PopgeoLink.getTestCases().size());
        assertEquals(4, TrackRepository.Complex.Hydrography.getTestCases().size());
    }
}