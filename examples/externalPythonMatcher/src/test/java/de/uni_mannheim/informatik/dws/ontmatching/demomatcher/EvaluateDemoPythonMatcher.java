package de.uni_mannheim.informatik.dws.ontmatching.demomatcher;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.Executor;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
import org.junit.*;

/**
 *
 * @author Sven Hertling
 */
public class EvaluateDemoPythonMatcher {
    
    @Test
    public void evalMatcher(){
        ExecutionResultSet result = Executor.run(TrackRepository.Anatomy.Default, new DemoPythonMatcher());
        ExecutionResult r = result.iterator().next();
        System.out.print(r.getSystemAlignment());
    }
    
}
