package de.uni_mannheim.informatik.dws.ontmatching.demomatcher;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.Executor;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.paramtuning.GridSearch;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
import java.util.Arrays;
import org.junit.*;

/**
 *
 * @author Sven Hertling
 */
public class EvaluateMatcher {
    
    @Test
    public void evalSimpleMatcher(){        
        ExecutionResultSet result = Executor.run(TrackRepository.Anatomy.Default, new SimpleStringMatcher());
        ExecutionResult r = result.iterator().next();
        System.out.print(r.getSystemAlignment());
    }
    
    @Test
    public void evalResourceMatcher(){        
        ExecutionResultSet result = Executor.run(TrackRepository.Anatomy.Default, new LoadResourceMatcher());
    }
    
    @Test
    public void optimzeMatcher(){
        GridSearch search = new GridSearch(LevenshteinMatcher.class);
        search.addParameter("threshold", Arrays.asList(0.1, 0.3, 0.5, 0.7, 1.0));
        ExecutionResultSet r = search.runGrid(TrackRepository.Conference.V1.getTestCase("cmt-conference"));
        EvaluatorCSV eval = new EvaluatorCSV(r);
        eval.write();
    }
    
}
