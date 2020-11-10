package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RankingMetricGroupTest {

    private static Alignment system1 = createSystemAlignment1();
    private static Alignment createSystemAlignment1(){
        Alignment a = new Alignment();
        a.add("a", "a");
        a.add("a", "b");
        a.add("b", "b");
        a.add("b", "c");
        a.add("b", "d");
        return a;
    }

    private static Alignment reference1 = createReferenceAlignment1();
    private static Alignment createReferenceAlignment1(){
        Alignment a = new Alignment();
        a.add("a", "b");
        a.add("b", "c");
        a.add("b", "d");
        return a;
    }

    @Test
    void compute(){
        RankingMetricGroup metric = new RankingMetricGroup(SameConfidenceRanking.ALPHABETICALLY, 2);
        ExecutionResult executionResult = new ExecutionResult(TrackRepository.Anatomy.Default.getFirstTestCase(),
                "Test", system1, reference1);
        RankingResult result = metric.compute(executionResult);
        assertEquals(1, result.getHitsAtK());
        assertEquals(( 1.0 + 0.5 ) / 2, result.getRecallAtK());
        assertEquals(0.5, result.getPrecisionAtK());
        assertEquals((2.0 * 0.75 * 0.5)/(0.5 + 0.75), result.getF1AtK());
        assertEquals((2.0 * 0.75 * 0.5)/(0.5 + 0.75), result.getFmeasureAtK(1.0));
        assertEquals((0.5), result.getReciprocalRank());
        assertEquals((0.5 + (0.5 + 2.0/3)*0.5) / 2, result.getAveragePrecision());
    }

}