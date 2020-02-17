package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.LocalTrack;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluateMatcher {
    
    private static Logger LOGGER = LoggerFactory.getLogger(EvaluateMatcher.class);
    
    public static void main(String[] args){
        
        String pathToTrack = EvaluateMatcher.class.getClassLoader().getResource("localtrack").getPath();        
        Track localTrack = new LocalTrack("LocalTrack", "1.0", pathToTrack);
        LOGGER.info("Track: {}", localTrack.getFirstTestCase());
        ExecutionResultSet executionResultSet = Executor.run(localTrack, new SimpleStringMatcher());
        EvaluatorCSV evaluator = new EvaluatorCSV(executionResultSet);
        evaluator.writeToDirectory();
    }
}
