package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.resultsSimilarity;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;


import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.MAC;

class MatcherSimilarityMetricTest {

    @Test
    void computeSimilarityExecutionResult(){
        Alignment alignment_1 = new Alignment();
        alignment_1.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_1.add("http://www.left.com/e2", "http://www.right.com/e2");

        Alignment alignment_2 = new Alignment();
        alignment_2.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_2.add("http://www.left.com/e3", "http://www.right.com/e3");

        ExecutionResult result_1 = new ExecutionResult(null, null, null, 1L, alignment_1, null, null, null);
        ExecutionResult result_2 = new ExecutionResult(null, null, null, 1L, alignment_2, null, null, null);

        assertEquals(1.0 / 3.0, MatcherSimilarityMetric.computeSimilarity(result_1, result_2));
    }


    @Test
    void computeSimilarityAlignment(){
        Alignment alignment_1 = new Alignment();
        alignment_1.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_1.add("http://www.left.com/e2", "http://www.right.com/e2");

        Alignment alignment_2 = new Alignment();
        alignment_2.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_2.add("http://www.left.com/e3", "http://www.right.com/e3");

        assertEquals(1.0 / 3.0, MatcherSimilarityMetric.computeSimilarity(alignment_1, alignment_2));
    }


    @Test
    //@EnabledOnOs({ MAC })
    void get(){
        Alignment alignment_1 = new Alignment();
        alignment_1.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_1.add("http://www.left.com/e2", "http://www.right.com/e2");

        Alignment alignment_2 = new Alignment();
        alignment_2.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_2.add("http://www.left.com/e3", "http://www.right.com/e3");

        TestCase anatomyTestCase = TrackRepository.Anatomy.Default.getTestCases().get(0);

        ExecutionResult result_1 = new ExecutionResult(anatomyTestCase, "Matcher_1", null, 1L, alignment_1, null, null, null);
        ExecutionResult result_2 = new ExecutionResult(anatomyTestCase, "Matcher_2", null, 1L, alignment_2, null, null, null);

        ExecutionResultSet set_1 = new ExecutionResultSet();
        set_1.add(result_1);
        set_1.add(result_2);

        MatcherSimilarityMetric metric_1 = new MatcherSimilarityMetric();
        MatcherSimilarity similarity_1 = metric_1.get(set_1, anatomyTestCase, null);

        assertEquals(1.0, similarity_1.getMatcherSimilarity(result_1, result_1), "Similarity between the same matcher is not 1.0.");
        assertEquals(1.0, similarity_1.getMatcherSimilarity(result_2, result_2), "Similarity between the same matcher is not 1.0.");
        assertEquals(1.0 / 3.0, similarity_1.getMatcherSimilarity(result_1, result_2));
        assertEquals(1.0 / 3.0, similarity_1.getMatcherSimilarity(result_2, result_1));
    }


}