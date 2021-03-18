package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutorParallel;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes the multi source task in parallel.
 * This executor needs to be instantiated with the number of parallel tasks / threads.
 */
public class ExecutorMultiSourceParallel {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorParallel.class);
    
    private final int numberOfThreads;
    
    public ExecutorMultiSourceParallel(){
        this.numberOfThreads = Runtime.getRuntime().availableProcessors();
    }
    
    public ExecutorMultiSourceParallel(int numberOfThreads){
        this.numberOfThreads = numberOfThreads;
    }
    
   
    public ExecutionResultSet runMultipleMatchersMultipleTracks(List<Track> tracks, Map<String, Object> matchers){
        return runMultipleMatchersMultipleTracks(tracks, matchers, null);
    }
    
    public ExecutionResultSet runMultipleMatchersMultipleTracks(List<Track> tracks, Map<String, Object> matchers, Properties additionalParameters){
        ExecutorService exec = Executors.newFixedThreadPool(numberOfThreads);

        List<Future<ExecutionResultSet>> futures = new ArrayList<>(tracks.size() * matchers.size());
        for(Track track : tracks){
            List<TestCase> trackTestCases = track.getTestCases();
            List<URL> distinctOntologies = Track.getDistinctOntologies(trackTestCases);
            for(Entry<String, Object> matcher : matchers.entrySet()){
                futures.add(exec.submit(new ExecutionRunnerMultiSource(
                        trackTestCases, matcher.getValue(), matcher.getKey(), distinctOntologies, 
                        ExecutorMultiSource.getMostSpecificPartitioner(track), additionalParameters)));
            }
        }
        
        ExecutionResultSet results = new ExecutionResultSet();
        for (Future<ExecutionResultSet> f : futures) {
            try {
                results.addAll(f.get());// wait for a MatcherRunner to complete
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.warn("Error when waiting for parallel results of matcher execution.", ex);
            }
        }
        exec.shutdown();
        return results;
    }
    
    public ExecutionResultSet runMultipleMatchers(Track track, Map<String, Object> matchers){
        return runMultipleMatchers(track.getTestCases(), matchers);
    }
    
    public ExecutionResultSet runMultipleMatchers(List<TestCase> testCases, Map<String, Object> matchers){
        return runMultipleMatchers(testCases, matchers, null);
    }
    
    public ExecutionResultSet runMultipleMatchers(List<TestCase> testCases, Map<String, Object> matchers, Properties additionalParameters){
        ExecutorService exec = Executors.newFixedThreadPool(numberOfThreads);

        Map<Track, List<TestCase>> trackToTestCase = ExecutorMultiSource.groupTestCasesByTrack(testCases);
        
        List<Future<ExecutionResultSet>> futures = new ArrayList<>(trackToTestCase.size() * matchers.size());
        for(Map.Entry<Track, List<TestCase>> trackToTestcases : trackToTestCase.entrySet()){
            Track track = trackToTestcases.getKey();
            List<TestCase> trackTestCases = trackToTestcases.getValue();
            List<URL> distinctOntologies = Track.getDistinctOntologies(trackTestCases);
            for(Entry<String, Object> matcher : matchers.entrySet()){
                futures.add(exec.submit(new ExecutionRunnerMultiSource(
                        trackTestCases, matcher.getValue(), matcher.getKey(), distinctOntologies, 
                        ExecutorMultiSource.getMostSpecificPartitioner(track), additionalParameters)));
            }
        }
        
        ExecutionResultSet results = new ExecutionResultSet();
        for (Future<ExecutionResultSet> f : futures) {
            try {
                results.addAll(f.get());// wait for a MatcherRunner to complete
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.warn("Error when waiting for parallel results of matcher execution.", ex);
            }
        }
        exec.shutdown();
        return results;
    }
}
