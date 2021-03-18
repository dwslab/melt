package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class which stores the information for one multi source matching task.
 * It is mainly used in the parallel execution of multi source tasks.
 */
public class ExecutionRunnerMultiSource implements Callable<ExecutionResultSet>{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionRunnerMultiSource.class);
    
    private List<TestCase> testCases;
    private Object matcher;        
    private String matcherName;
    private List<URL> allGraphs;
    private Partitioner partitioner;
    private Properties additionalParameters;

    public ExecutionRunnerMultiSource(List<TestCase> testCases, Object matcher, String matcherName, List<URL> allGraphs, Partitioner partitioner, Properties additionalParameters) {
        this.testCases = testCases;
        this.matcher = matcher;
        this.matcherName = matcherName;
        this.allGraphs = allGraphs;
        this.partitioner = partitioner;
        this.additionalParameters = additionalParameters;
    }

    @Override
    public ExecutionResultSet call() {
        Thread.currentThread().setName(matcherName + "-" + getTrackNames());        
        return ExecutorMultiSource.run(testCases, matcher, matcherName, allGraphs, partitioner, additionalParameters);
    }

    private Set<String> getTrackNames(){
        Set<String> trackNames = new HashSet<>();
        for(TestCase testCase : testCases){
            trackNames.add(testCase.getTrack().getName());
        }
        return trackNames;
    }    
}
