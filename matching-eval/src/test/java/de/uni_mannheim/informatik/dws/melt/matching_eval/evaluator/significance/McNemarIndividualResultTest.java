package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.significance;

import org.junit.jupiter.api.Test;

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
}