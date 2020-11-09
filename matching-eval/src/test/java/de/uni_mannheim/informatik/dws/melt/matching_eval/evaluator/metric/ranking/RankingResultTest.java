package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RankingResultTest {

    @Test
    void normalizeAllScores() {
        RankingResult result = new RankingResult(3.0, 5.0, 1.0, 0.25, 0.1, 0.5, 0.3, 0.11, 3);
        result.normalizeAllScores(5);
        assertEquals(3.0/5.0, result.getDcg());
        assertEquals(5.0/5.0, result.getNdcg());
        assertEquals(1.0/5.0, result.getAveragePrecision());
        assertEquals(0.3/5.0, result.getPrecisionAtK());
        assertEquals(0.25/5.0, result.getReciprocalRank());
        assertEquals(0.1/5.0, result.getrPrecision());
        assertEquals(0.11/5.0, result.getRecallAtK());
    }

    @Test
    void addScores(){
        RankingResult result = new RankingResult(3.0, 5.0, 1.0, 0.25, 0.1, 0.5, 0.3, 0.11, 3);
        RankingResult other = new RankingResult(3.0, 5.0, 1.0, 0.25, 0.1, 0.5, 0.3, 0.11, 3);
        result.addScores(other);
        assertEquals(3.0*2, result.getDcg());
        assertEquals(5.0*2, result.getNdcg());
        assertEquals(1.0*2, result.getAveragePrecision());
        assertEquals(0.3*2, result.getPrecisionAtK());
        assertEquals(0.25*2, result.getReciprocalRank());
        assertEquals(0.1*2, result.getrPrecision());
        assertEquals(0.11*2, result.getRecallAtK());

        // make sure other is unchanged
        assertEquals(3.0, other.getDcg());
        assertEquals(5.0, other.getNdcg());
        assertEquals(1.0, other.getAveragePrecision());
        assertEquals(0.3, other.getPrecisionAtK());
        assertEquals(0.25,other.getReciprocalRank());
        assertEquals(0.1, other.getrPrecision());
        assertEquals(0.11, other.getRecallAtK());
    }

}