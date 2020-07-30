package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.hobbit.HobbitAPI;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EvaluateDemoPythonMatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluateDemoPythonMatcher.class);  
    
    public static void main(String[] args){
        //in python environment rdflib is required.
        //do not forget to remove the eval dependency in pom.xml when building the seals package (of course you have to comment this whole class too).
        ExecutionResultSet result = Executor.run(TrackRepository.Anatomy.Default, new DemoPythonMatcher());
        ExecutionResult r = result.iterator().next();
        LOGGER.info("Python matcher run returned {} correspondences.", r.getSystemAlignment().size());
        
        //in case you run your matcher in HOBBIT you can access the logs with:
        HobbitAPI.setAccessTokenByMavenCredentials(); //in case you have stored your account in .m2/settings.xml otherwise use HobbitAPI.setAccessTokenByCredentials(username, password);
        HobbitAPI.printSystemLog("experimentID");//replace experimentID with your experiment id.
    }
    
}
