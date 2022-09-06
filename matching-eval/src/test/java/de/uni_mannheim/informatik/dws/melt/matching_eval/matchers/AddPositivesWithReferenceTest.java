package de.uni_mannheim.informatik.dws.melt.matching_eval.matchers;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrix;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrixMetric;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class AddPositivesWithReferenceTest {
    
   @Test
    public void testMatcher(){
        for(double referenceFraction : Arrays.asList(0.2, 0.5, 0.9)){
            for(TestCase tc : Arrays.asList(TrackRepository.Anatomy.Default.getFirstTestCase()
                                            //,TrackRepository.Conference.V1.getFirstTestCase()
                                            )){
                AddPositivesWithReference matcher = new AddPositivesWithReference(referenceFraction);
                ExecutionResult result = Executor.runSingle(tc, matcher);

                ConfusionMatrixMetric m = new ConfusionMatrixMetric();
                ConfusionMatrix matrix = m.compute(result);

                System.out.println(matrix.getRecall());
                assertEquals(1.0, matrix.getPrecision());
                assertTrue((referenceFraction - 0.05) < matrix.getRecall() && matrix.getRecall() < (referenceFraction + 0.05));
            }
        }
    }
    
}
