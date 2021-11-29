package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.resultsSimilarity;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class MatcherSimilarityTest {


    @Test
    void median(){
        LinkedList<Double> unevenList_1 = new LinkedList<>();
        unevenList_1.add(2.0);
        unevenList_1.add(1.0);
        unevenList_1.add(3.0);
        assertEquals(2.0, MatcherSimilarity.median(unevenList_1));

        LinkedList<Double> evenList_1 = new LinkedList<>();
        evenList_1.addAll(unevenList_1);
        evenList_1.add(5.0);
        assertEquals(2.5, MatcherSimilarity.median(evenList_1));
    }

    @Test
    //@EnabledOnOs({ MAC })
    void getMedianSimilariyWithoutSelfSimilarity(){
        Alignment alignment_1 = new Alignment();
        alignment_1.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_1.add("http://www.left.com/e2", "http://www.right.com/e2");

        Alignment alignment_2 = new Alignment();
        alignment_2.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_2.add("http://www.left.com/e3", "http://www.right.com/e3");

        Alignment alignment_3 = new Alignment();
        alignment_3.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_3.add("http://www.left.com/e4", "http://www.right.com/e4");

        TestCase anatomyTestCase = TrackRepository.Anatomy.Default.getTestCases().get(0);

        ExecutionResult result_1 = new ExecutionResult(anatomyTestCase, "Matcher_1", null, 1L, alignment_1, null, null, null, null);
        ExecutionResult result_2 = new ExecutionResult(anatomyTestCase, "Matcher_2", null, 1L, alignment_2, null, null, null, null);
        ExecutionResult result_3 = new ExecutionResult(anatomyTestCase, "Matcher_3", null, 1L, alignment_3, null, null, null, null);

        ExecutionResultSet set_1 = new ExecutionResultSet();
        set_1.add(result_1);
        set_1.add(result_2);
        set_1.add(result_3);

        MatcherSimilarityMetric metric_1 = new MatcherSimilarityMetric();
        MatcherSimilarity similarity_1 = metric_1.get(set_1, anatomyTestCase, null);

        assertEquals(1.0/3.0, similarity_1.getMedianSimilariyWithoutSelfSimilarity());
    }

    @Test
    //@EnabledOnOs({ MAC })
    void getMedianSimilariyWithSelfSimilarity_1(){
        Alignment alignment_1 = new Alignment();
        alignment_1.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_1.add("http://www.left.com/e2", "http://www.right.com/e2");

        Alignment alignment_2 = new Alignment();
        alignment_2.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_2.add("http://www.left.com/e3", "http://www.right.com/e3");

        Alignment alignment_3 = new Alignment();
        alignment_3.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_3.add("http://www.left.com/e4", "http://www.right.com/e4");

        TestCase anatomyTestCase = TrackRepository.Anatomy.Default.getTestCases().get(0);

        ExecutionResult result_1 = new ExecutionResult(anatomyTestCase, "Matcher_1", null, 1L, alignment_1, null, null, null, null);
        ExecutionResult result_2 = new ExecutionResult(anatomyTestCase, "Matcher_2", null, 1L, alignment_2, null, null, null, null);
        ExecutionResult result_3 = new ExecutionResult(anatomyTestCase, "Matcher_3", null, 1L, alignment_3, null, null, null, null);

        ExecutionResultSet set_1 = new ExecutionResultSet();
        set_1.add(result_1);
        set_1.add(result_2);
        set_1.add(result_3);

        MatcherSimilarityMetric metric_1 = new MatcherSimilarityMetric();
        MatcherSimilarity similarity_1 = metric_1.get(set_1, anatomyTestCase, null);

        assertEquals(2.0 / 3.0, similarity_1.getMedianSimiarity());
    }

    @Test
    //@EnabledOnOs({ MAC })
    void getMedianSimilariyWithOutSelfSimilarity_2(){
        Alignment alignment_1 = new Alignment();
        alignment_1.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_1.add("http://www.left.com/e2", "http://www.right.com/e2");

        Alignment alignment_2 = new Alignment();
        alignment_2.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_2.add("http://www.left.com/e3", "http://www.right.com/e3");

        TestCase anatomyTestCase = TrackRepository.Anatomy.Default.getTestCases().get(0);

        ExecutionResult result_1 = new ExecutionResult(anatomyTestCase, "Matcher_1", null, 1L, alignment_1, null, null, null, null);
        ExecutionResult result_2 = new ExecutionResult(anatomyTestCase, "Matcher_2", null, 1L, alignment_2, null, null, null, null);

        ExecutionResultSet set_1 = new ExecutionResultSet();
        set_1.add(result_1);
        set_1.add(result_2);

        MatcherSimilarityMetric metric_1 = new MatcherSimilarityMetric();
        MatcherSimilarity similarity_1 = metric_1.get(set_1, anatomyTestCase, null);

        assertEquals(1.0 / 3.0, similarity_1.getMedianSimilariyWithoutSelfSimilarity());
    }
}