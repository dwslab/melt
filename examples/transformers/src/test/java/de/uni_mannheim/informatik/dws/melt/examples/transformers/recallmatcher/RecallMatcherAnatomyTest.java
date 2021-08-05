package de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecallMatcherAnatomyTest {


    /**
     * Calculates the recall of the KG Track Recall matcher.
     */
    @Test
    @Disabled
    void recallCalculation(){
        List<TestCase> tracks = new ArrayList<>();
        tracks.addAll(TrackRepository.Anatomy.Default.getTestCases());
        tracks.addAll(TrackRepository.Conference.V1.getTestCases());
        EvaluatorCSV evaluator = new EvaluatorCSV(Executor.run(tracks, new RecallMatcherAnatomy()));
        evaluator.writeToDirectory();
    }

}