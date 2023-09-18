package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import org.junit.jupiter.api.Test;


public class EvaluateMatcher {
    
    @Test
    public void evalSimpleMatcher(){        
        ExecutionResultSet result = Executor.run(TrackRepository.Anatomy.Default, new SSSOMMatcher());
        ExecutionResult r = result.iterator().next();
        System.out.print(r.getSystemAlignment());
    }
}
