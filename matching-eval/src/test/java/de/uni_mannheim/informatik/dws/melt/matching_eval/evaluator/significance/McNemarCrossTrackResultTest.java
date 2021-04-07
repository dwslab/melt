package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.significance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class McNemarCrossTrackResultTest {


    @Test
    void testEquals() {
        McNemarCrossTrackResult ctr1 = new McNemarCrossTrackResult("M1", "M2", 0.5);
        McNemarCrossTrackResult ctr2 = new McNemarCrossTrackResult("M1", "M2", 0.5);
        McNemarCrossTrackResult ctr3 = new McNemarCrossTrackResult("M1", "M5", 0.5);

        assertEquals(ctr1, ctr2);
        assertNotEquals(ctr1, ctr3);
        assertNotEquals(ctr2, ctr3);
    }
}