package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.demomatcher.DemoPythonMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EvaluateDemoPythonMatcher {
    
    @Test
    public void evalMatcher(){
        //in python environment rdflib is required.
        ExecutionResultSet result = Executor.run(TrackRepository.Anatomy.Default, new DemoPythonMatcher());
        assertEquals(1, result.size());        
        ExecutionResult r = result.iterator().next();
        assertTrue(r.getSystemAlignment().size() > 1);  
    }
    
}
