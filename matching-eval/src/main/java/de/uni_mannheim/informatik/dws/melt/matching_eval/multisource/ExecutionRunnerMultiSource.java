package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherMultiSourceCaller;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class which stores the information for one multi source matching task.
 * It is mainly used in the parallel execution of multi source tasks.
 */
public class ExecutionRunnerMultiSource implements Callable<ExecutionResultMultiSource>{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionRunnerMultiSource.class);
    
    private List<TestCase> testCases;
    private Object matcher;        
    private String matcherName;
    private List<URL> allGraphs;
    private Partitioner partitioner;
    private Object inputAlignment;
    private Object parameters;

    public ExecutionRunnerMultiSource(List<TestCase> testCases, Object matcher, String matcherName, List<URL> allGraphs, Partitioner partitioner, Object inputAlignment, Object parameters) {
        this.testCases = testCases;
        this.matcher = matcher;
        this.matcherName = matcherName;
        this.allGraphs = allGraphs;
        this.partitioner = partitioner;
        this.inputAlignment = inputAlignment;
        this.parameters = parameters;
    }

    @Override
    public ExecutionResultMultiSource call() {
        Thread.currentThread().setName(matcherName + "-" + getTrackNames(testCases));        
        return run(testCases, matcher, matcherName, allGraphs, partitioner, inputAlignment, parameters);
    }

    public static ExecutionResultMultiSource run(List<TestCase> testCases, Object matcher, String matcherName, List<URL> allGraphs, Partitioner partitioner, Object inputAlignment, Object parameters){
        Set<String> trackNames = getTrackNames(testCases);
        LOGGER.info("Running multi source matcher {} on track(s) {}.", matcherName, trackNames);
        if(allTestCasesFromSameTrack(testCases) == false)
            LOGGER.warn("Not all test cases are from the same track. The runtime of the tracks will not be correctly computed and also the partitioner will not work cirrectly. Be warned.");
        
        long runTime;
        long startTime = System.nanoTime();
        AlignmentAndParameters result = null;
        try {
            result = GenericMatcherMultiSourceCaller.runMatcherMultiSourceSpecificType(matcher, allGraphs, inputAlignment, parameters);
        } catch (Exception ex) {
            LOGGER.error("Exception during matching (matcher " + matcherName + " on track(s) " +  trackNames + ").", ex);
            return null;
        }
        finally
        {
            runTime = System.nanoTime() - startTime;  
            LOGGER.info("Running matcher {} on track(s) {} completed in {}.", matcherName, trackNames, DurationFormatUtils.formatDurationWords((runTime/1_000_000), true, true));
        }
        if(result == null || result.getAlignment() == null){
            LOGGER.error("Result is null or contains no alignment. Returning empty ExecutionResultSetMultiSource.");
            return null;
        }
        return new ExecutionResultMultiSource(result.getAlignment(), result.getParameters(), matcher, matcherName, allGraphs, testCases, runTime, partitioner);
    }
    
    private static boolean allTestCasesFromSameTrack(List<TestCase> testCases){
        if(testCases.isEmpty())
            return true;
        Track compare = testCases.get(0).getTrack();
        for(int i = 1; i < testCases.size(); i++){
            if (!testCases.get(i).getTrack().equals(compare))
                return false;
        }
        return true;
    }
    
    private static Set<String> getTrackNames(List<TestCase> testCases){
        Set<String> trackNames = new HashSet<>();
        for(TestCase testCase : testCases){
            trackNames.add(testCase.getTrack().getName());
        }
        return trackNames;
    }
}
