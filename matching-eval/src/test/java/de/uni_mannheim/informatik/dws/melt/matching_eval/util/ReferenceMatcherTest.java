package de.uni_mannheim.informatik.dws.melt.matching_eval.util;

import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrix;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrixMetric;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReferenceMatcherTest {


    @Test
    void testReferenceMatcherOnConferenceTrack(){                
        Track track = TrackRepository.Conference.V1;
        ConfusionMatrixMetric confusionMatrixMetric = new ConfusionMatrixMetric();
        ExecutionResultSet s = Executor.run(track, new ReferenceMatcher(track));
        for(ExecutionResult result : s){
            ConfusionMatrix cm = confusionMatrixMetric.compute(result);
            assertEquals(0, cm.getFalseNegativeSize());
            assertEquals(0, cm.getFalsePositiveSize());
            assertEquals(result.getReferenceAlignment().size(), cm.getTruePositiveSize());
        }
    }
}
