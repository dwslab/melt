package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning.GridSearch;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

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
    public void optimizeMatcher(){
        GridSearch gridSearch = new GridSearch(LevenshteinMatcher.class);
        gridSearch.addParameter("threshold", Arrays.asList(0.1, 0.3, 0.5, 0.7, 1.0));
        ExecutionResultSet executionResultSet = gridSearch.runGridSequential(TrackRepository.Conference.V1.getTestCase("cmt-conference"));
        Assertions.assertTrue(executionResultSet.size() > 0);
        EvaluatorCSV evaluator = new EvaluatorCSV(executionResultSet);
        evaluator.writeToDirectory();
    }
    
}
