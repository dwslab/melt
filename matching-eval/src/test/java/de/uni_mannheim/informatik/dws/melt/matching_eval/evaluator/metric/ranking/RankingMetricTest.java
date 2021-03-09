package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class RankingMetricTest {


    @Test
    void testAlphabetically() throws Exception {
        Alignment system = new Alignment();
        system.add(new Correspondence("a", "b"));
        system.add(new Correspondence("c", "d"));
        system.add(new Correspondence("e", "f"));
        system.add(new Correspondence("g", "h"));
        system.add(new Correspondence("i", "j"));
        system.add(new Correspondence("k", "l"));
        system.add(new Correspondence("m", "n"));
        system.add(new Correspondence("o", "p"));
        system.add(new Correspondence("q", "r"));
        system.add(new Correspondence("s", "t"));
        
        Alignment reference = new Alignment();
        reference.add(new Correspondence("a", "b"));
        reference.add(new Correspondence("e", "f"));
        reference.add(new Correspondence("k", "l"));
        reference.add(new Correspondence("q", "r"));
        reference.add(new Correspondence("s", "t"));        

        RankingMetric ranker = new RankingMetric(SameConfidenceRanking.ALPHABETICALLY);
        TestCase tcDummy = TrackRepository.Anatomy.Default.getFirstTestCase();
        RankingResult result = ranker.get(new ExecutionResult(tcDummy, "TestMatcher", system, reference));
        
        assertEquals(2.44, result.getDcg(), 0.1d);
        assertEquals(0.53, result.getNdcg(), 0.1d);
        assertEquals(0.62, result.getAveragePrecision(), 0.1d);
        assertEquals(2.0 / 5.0, result.getrPrecision());
        assertEquals(2.0 / 5.0, result.getPrecisionAtK());
        assertEquals(2.0 / 5.0, result.getRecallAtK());
        assertEquals(5, result.getkOfHitsAtK()); // will take the size of the reference alignment as K
        assertEquals(2, result.getHitsAtK());
        assertEquals(1.0, result.getReciprocalRank());
        double f1atK = (2.0*(4.0 / 25.0))/(2*(2.0 / 5.0));
        assertEquals(f1atK, result.getF1AtK(), 0.00000001);
        assertEquals(f1atK, result.getFmeasureAtK(1.0), 0.00000001);
    }
    
}
