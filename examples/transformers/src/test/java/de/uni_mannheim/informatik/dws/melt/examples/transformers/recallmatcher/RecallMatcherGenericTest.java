package de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.SimpleStringMatcher;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class RecallMatcherGenericTest {


    /**
     * Calculates the recall of the KG Track Recall matcher.
     */
    @Test
    @Disabled
    void recallCalculationAnatomy(){
        List<TestCase> tracks = new ArrayList<>();
        tracks.addAll(TrackRepository.Anatomy.Default.getTestCases());
        //tracks.addAll(TrackRepository.Conference.V1.getTestCases());
        
        Map<String, IOntologyMatchingToolBridge> matchers = new HashMap<>();
        matchers.put("SimpleStringMatcher", new SimpleStringMatcher());
        //matchers.put("RecallMatcherAnatomy", new RecallMatcherAnatomy());
        //matchers.put("RecallMatcherGeneric10", new RecallMatcherGeneric(10));
        matchers.put("RecallMatcherGeneric20onedirection", new RecallMatcherGeneric(20, false));
        matchers.put("RecallMatcherGeneric20twodirection", new RecallMatcherGeneric(20, true));
        //matchers.put("RecallMatcherGeneric50", new RecallMatcherGeneric(50));
        //matchers.put("RecallMatcherGeneric100", new RecallMatcherGeneric(100));
        
        matchers.put("RecallMatcherGeneric100onedirection", new RecallMatcherGeneric(100, false));
        matchers.put("RecallMatcherGeneric100twodirection", new RecallMatcherGeneric(100, true));
        EvaluatorCSV evaluator = new EvaluatorCSV(Executor.run(tracks, matchers));
        evaluator.writeToDirectory();
    }

}