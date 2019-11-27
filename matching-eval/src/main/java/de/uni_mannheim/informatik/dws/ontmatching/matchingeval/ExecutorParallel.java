package de.uni_mannheim.informatik.dws.ontmatching.matchingeval;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor to run matchers in parallel.
 */
public class ExecutorParallel {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorParallel.class);
    
    private int numberOfThreads;
    
    
    public ExecutorParallel(){
        this.numberOfThreads = Runtime.getRuntime().availableProcessors();
    }
    
    public ExecutorParallel(int numberOfThreads){
        this.numberOfThreads = numberOfThreads;
    }
    
    
    /**
     * Run the given matchers in parallel. Make sure the matcher can be run in parallel.
     * @param testCases the testcases on which the matcher should be run
     * @param matchers the matchers
     * @return ExecutionResultSet
     */
    public ExecutionResultSet run(List<TestCase> testCases, Map<String, IOntologyMatchingToolBridge> matchers) {

        ExecutorService exec = Executors.newFixedThreadPool(numberOfThreads);

        List<Future<ExecutionResult>> futures = new ArrayList<>(testCases.size() * matchers.size());
        for (TestCase tc : testCases) {
            for (Map.Entry<String, IOntologyMatchingToolBridge> matcher : matchers.entrySet()) {
                futures.add(exec.submit(new ExecutionRunner(tc, matcher.getValue(), matcher.getKey())));
            }
        }

        ExecutionResultSet results = new ExecutionResultSet();
        for (Future<ExecutionResult> f : futures) {
            try {
                results.add(f.get());// wait for a MatcherRunner to complete
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.warn("Error when waiting for parallel results of matcher execution.", ex);
            }
        }
        exec.shutdown();
        return results;
    }
    
    public ExecutionResultSet run(Track track, Map<String, IOntologyMatchingToolBridge> matchers) {
        return run(track.getTestCases(), matchers);
    }
    
    public ExecutionResultSet runTracks(List<Track> tracks, Map<String, IOntologyMatchingToolBridge> matchers) {
        List<TestCase> testCases = new ArrayList<>();
        for(Track t : tracks){
            testCases.addAll(t.getTestCases());
        }
        return run(testCases, matchers);
    }
    
}
//to run in parallel use ExecutorService 
//https://stackoverflow.com/questions/21156599/javas-fork-join-vs-executorservice-when-to-use-which
//https://stackoverflow.com/questions/30585064/grid-search-better-performance-using-threads
//https://stackoverflow.com/questions/30646474/process-list-of-n-items-with-multiple-threads
//https://www.baeldung.com/java-executor-service-tutorial
//https://www.baeldung.com/java-executor-wait-for-threads
//https://stackoverflow.com/questions/46958118/synchronized-and-executorservice-on-java-8
//https://dzone.com/articles/basics-of-using-java-future-and-executor-service
