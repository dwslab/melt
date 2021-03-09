package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MatcherMultiSourceURL;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrix;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrixMetric;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ForwardMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MultiSourceDispatcherAllPairs;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MultiSourceDispatcherUnionToUnion;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class DispatcherTest {


    @Test
    void testDispatchersAllPairsAndUnionToUnion() {
        //testcases with 100% reference alignment as input alignment.
        List<TestCase> conferenceWithReferenceAlignment = TrackRepository.generateTrackWithSampledReferenceAlignment(TrackRepository.Conference.V1, 1.0, 1234, false);
        for(TestCase tc : conferenceWithReferenceAlignment){
            assertEquals(
                    tc.getParsedInputAlignment(), 
                    tc.getParsedReferenceAlignment()
            );
        }
        //just uses the input alignment which is in our case the reference alignment.
        ForwardMatcher oneToOne = new ForwardMatcher();

        List<MatcherMultiSourceURL> matchers = new ArrayList<>();
        matchers.add(new MultiSourceDispatcherAllPairs(oneToOne));
        matchers.add(new MultiSourceDispatcherUnionToUnion(oneToOne));
        //matchers.add(new MultiSourceDispatcherIncrementalMergeByOrder(oneToOne, MultiSourceDispatcherIncrementalMergeByOrder.IDENTITY));
        //matchers.add(new MultiSourceDispatcherIncrementalMergeByClusterText(oneToOne, ClusterLinkage.SINGLE));
        
        
        for(MatcherMultiSourceURL multiSourceMatcher : matchers){
            ConfusionMatrixMetric confusionMatrixMetric = new ConfusionMatrixMetric();
            ExecutionResultSet s = ExecutorMultiSource.run(conferenceWithReferenceAlignment, multiSourceMatcher);
            for(ExecutionResult result : s){
                ConfusionMatrix cm = confusionMatrixMetric.compute(result);
                assertEquals(0, cm.getFalseNegativeSize());
                assertEquals(0, cm.getFalsePositiveSize());
                assertEquals(result.getReferenceAlignment().size(), cm.getTruePositiveSize());
            }
        }
    }
}

