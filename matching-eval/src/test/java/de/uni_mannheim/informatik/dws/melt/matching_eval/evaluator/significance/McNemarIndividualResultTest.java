package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.significance;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class McNemarIndividualResultTest {


    @Test
    void equalsTest() {
        McNemarIndividualResult ir1 = new McNemarIndividualResult("M1", "M2", "TC1", "T1", 0.5);
        McNemarIndividualResult ir2 = new McNemarIndividualResult("M1", "M2", "TC1", "T1", 0.5);
        McNemarIndividualResult ir3 = new McNemarIndividualResult("M1", "M2", "TC2", "T1", 0.5);
        assertEquals(ir1, ir2);
        assertNotEquals(ir1, ir3);
        assertNotEquals(ir2, ir3);

        // assertions using transformations
        assertEquals(ir2.getTrackResult(), ir3.getTrackResult());
        assertEquals(ir2.getCrossTrackResult(), ir3.getCrossTrackResult());
    }

    @Test
    void getDistinctTracks() {
        McNemarIndividualResult ir1 = new McNemarIndividualResult("M1", "M2", "TC1", "T1", 0.5);
        McNemarIndividualResult ir2 = new McNemarIndividualResult("M2", "M2", "TC2", "T2", 0.5);
        McNemarIndividualResult ir3 = new McNemarIndividualResult("M3", "M2", "TC2", "T1", 0.5);

        Map<McNemarIndividualResult, Double> p = new HashMap<>();
        p.put(ir1, 1.0);
        p.put(ir2, 1.0);
        p.put(ir3, 1.0);

        Set<String> tracks = McNemarIndividualResult.getDistinctTracks(p);
        assertEquals(2, tracks.size());
        assertTrue(tracks.contains("T1"));
        assertTrue(tracks.contains("T2"));
    }

    @Test
    void getEntriesForTrack(){
        McNemarIndividualResult ir1 = new McNemarIndividualResult("M1", "M2", "TC1", "T1", 0.5);
        McNemarIndividualResult ir2 = new McNemarIndividualResult("M2", "M2", "TC2", "T2", 0.5);
        McNemarIndividualResult ir3 = new McNemarIndividualResult("M3", "M2", "TC2", "T1", 0.5);

        Map<McNemarIndividualResult, Double> p = new HashMap<>();
        p.put(ir1, 1.0);
        p.put(ir2, 1.0);
        p.put(ir3, 1.0);

        Map<McNemarIndividualResult, Double> result = McNemarIndividualResult.getEntriesForTrack(p, "T2");
        assertEquals(1, result.size());
        for(Map.Entry<McNemarIndividualResult, Double> entry: result.entrySet()){
            assertEquals("M2", entry.getKey().matcherName1);
            assertEquals("M2", entry.getKey().matcherName2);
        }

        result = McNemarIndividualResult.getEntriesForTrack(p, "T1");
        assertEquals(2, result.size());

        result = McNemarIndividualResult.getEntriesForTrack(p, "DOES NOT EXIST");
        assertEquals(0, result.size());
    }

    @Test
    void getNumberOfTestcases(){
        McNemarIndividualResult ir1 = new McNemarIndividualResult("M1", "M2", "TC1", "T1", 0.5);
        McNemarIndividualResult ir2 = new McNemarIndividualResult("M2", "M2", "TC2", "T2", 0.5);
        McNemarIndividualResult ir3 = new McNemarIndividualResult("M3", "M2", "TC2", "T1", 0.5);

        Map<McNemarIndividualResult, Double> p = new HashMap<>();
        p.put(ir1, 1.0);
        p.put(ir2, 1.0);
        p.put(ir3, 1.0);

        assertEquals(2, McNemarIndividualResult.getNumberOfTestCases(p));
    }
}