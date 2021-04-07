
package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExecutorMultiSource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorMultiSource.class);
      
    public static ExecutionResultSetMultiSource runMultipleMatchersMultipleTracks(List<Track> tracks, Map<String, Object> matchers){
        return runMultipleMatchersMultipleTracks(tracks, matchers, null);
    }
    
    public static ExecutionResultSetMultiSource runMultipleMatchersMultipleTracks(List<Track> tracks, Map<String, Object> matchers, Properties additionalParameters){
        ExecutionResultSetMultiSource results = new ExecutionResultSetMultiSource();
        for(Track track : tracks){
            results.addAll(runMultipleMatchers(track.getTestCases(), matchers, additionalParameters));
        }
        return results;
    }
    
    public static ExecutionResultSetMultiSource runMultipleMatchers(Track track, Map<String, Object> matchers){
        return runMultipleMatchers(track.getTestCases(), matchers);
    }
    
    public static ExecutionResultSetMultiSource runMultipleMatchers(List<TestCase> testCases, Map<String, Object> matchers){
        return runMultipleMatchers(testCases, matchers, null);
    }
    
    public static ExecutionResultSetMultiSource runMultipleMatchers(List<TestCase> testCases, Map<String, Object> matchers, Properties additionalParameters){
        ExecutionResultSetMultiSource resultSet = new ExecutionResultSetMultiSource();        
        for(Entry<Track, List<TestCase>> trackToTestcases : groupTestCasesByTrack(testCases).entrySet()){
            Track track = trackToTestcases.getKey();
            List<TestCase> trackTestCases = trackToTestcases.getValue();
            List<URL> distinctOntologies = Track.getDistinctOntologies(trackTestCases);
            for(Entry<String, Object> matcher : matchers.entrySet()){
                ExecutionResultMultiSource r = run(trackTestCases, matcher.getValue(), matcher.getKey(), distinctOntologies, getMostSpecificPartitioner(track), additionalParameters);
                if(r != null)
                    resultSet.add(r);
            }
        }
        return resultSet;
    }
    
    public static ExecutionResultSetMultiSource run(Track track, Object matcher){
        return run(track.getTestCases(), matcher,Executor.getMatcherName(matcher));
    }
    
    public static ExecutionResultSetMultiSource run(List<TestCase> testCases, Object matcher){
        return run(testCases, matcher,Executor.getMatcherName(matcher));
    }
    
    public static ExecutionResultSetMultiSource run(List<TestCase> testCases, Object matcher, String matcherName){
        ExecutionResultSetMultiSource resultSet = new ExecutionResultSetMultiSource();        
        for(Entry<Track, List<TestCase>> trackToTestcases : groupTestCasesByTrack(testCases).entrySet()){
            Track track = trackToTestcases.getKey();
            List<TestCase> trackTestCases = trackToTestcases.getValue(); // in case not all testcases of a track are included.
            List<URL> distinctOntologies = Track.getDistinctOntologies(trackTestCases);
            ExecutionResultMultiSource r = ExecutorMultiSource.run(trackTestCases, matcher, matcherName, distinctOntologies, getMostSpecificPartitioner(track), null);
            if(r != null)
                resultSet.add(r);
        }
        return resultSet;
    }
    
    
    public static ExecutionResultMultiSource runWithAdditionalGraphs(Track track, Object matcher, String matcherName, List<URL> additionalGraphs, Partitioner partitioner){
        List<URL> allGraphs = track.getDistinctOntologies();
        allGraphs.addAll(additionalGraphs);
        return run(track.getTestCases(), matcher, matcherName, allGraphs, partitioner, null);
    }
    
    
    public static ExecutionResultMultiSource run(List<TestCase> testCases, Object matcher, String matcherName, List<URL> allGraphs, Partitioner partitioner, Properties additionalParameters){
        Properties parameters = getParameters(testCases);
        if(additionalParameters != null)
            parameters.putAll(additionalParameters);
        return ExecutionRunnerMultiSource.run(testCases, matcher, matcherName, allGraphs, partitioner, getCombinedInputAlignment(testCases), parameters);
    }
    
    public static ExecutionResultMultiSource fromAlignment(Alignment fullAlignment, List<TestCase> testCases, String matcherName, long totalRuntime, boolean computeTransitiveClosure, Partitioner partitioner){
        return new ExecutionResultMultiSource(fullAlignment, matcherName, testCases, totalRuntime, computeTransitiveClosure, partitioner);
    }
    
    
    public static Alignment getCombinedInputAlignment(List<TestCase> testCases){
        Alignment inputAlignment = new Alignment();
        for(TestCase testCase : testCases){
            inputAlignment.addAll(testCase.getParsedInputAlignment());
        }
        return inputAlignment;
    }
    
    private static Properties getParameters(List<TestCase> testCases){
        Properties parameters = new Properties();
        for(TestCase testCase : testCases){
            // will override already existent parameters
            parameters.putAll(testCase.getParsedParameters(Properties.class)); 
        }
        return parameters;
    }
    
    
    private static final Set<Track> KG_TRACKS = TrackRepository.retrieveDefinedTracks(TrackRepository.Knowledgegraph.class);
    private static final Set<Track> CONFERENCE_TRACKS = TrackRepository.retrieveDefinedTracks(TrackRepository.Conference.class);
    private static final Set<Track> LARGE_BIO_TRACKS = TrackRepository.retrieveDefinedTracks(TrackRepository.Largebio.V2016.class);

    /**
     * Returns the most specific partitioner for a given track
     * @param track the track
     * @return the most specific partitioner
     */
    public static Partitioner getMostSpecificPartitioner(Track track){
        DatasetIDExtractor idExtractor = getMostSpecificDatasetIdExtractor(track);
        if(idExtractor == null)
            return new PartitionerDefault(track);
        return new PartitionerFromDatasetIdExtractor(track, idExtractor);
    }
    
    /**
     * Returns the most specific partitioner for a given track
     * @param track the track
     * @return the most specific partitioner or null if none is available
     */
    public static DatasetIDExtractor getMostSpecificDatasetIdExtractor(Track track){
        if(KG_TRACKS.contains(track)){
            return DatasetIDExtractor.KG_TRACK_EXTRACTOR;
        }else if(CONFERENCE_TRACKS.contains(track)){
            return DatasetIDExtractor.CONFERENCE_TRACK_EXTRACTOR;
        }else if(LARGE_BIO_TRACKS.contains(track)){
            if(track.equals(TrackRepository.Largebio.V2016.ONLY_WHOLE) == false)
                LOGGER.warn("Makeing a multisource experiment with Large Bio is only possible with TrackRepository.Largebio.V2016.ONLY_WHOLE because"
                        + "other tracks contains multiple different ontologies (subsets which are not equal).");
            return DatasetIDExtractor.LARGE_BIO_TRACK_EXTRACTOR;
        }else{
            return null;
        }
    }
    
    
    public static Map<Track, List<TestCase>> groupTestCasesByTrack(List<TestCase> testCases){
        Map<Track, List<TestCase>> map = new HashMap<>();
        for(TestCase testCase : testCases){
            map.computeIfAbsent(testCase.getTrack(), __-> new ArrayList<>()).add(testCase);
        }
        return map;
    }
    
    public static ExecutionResultSetMultiSource runMatcherOnTop(ExecutionResultSetMultiSource oldResultSet, String oldMatcherName, String newMatcherName, Object matcher){
        ExecutionResultSetMultiSource newResultSet = new ExecutionResultSetMultiSource();        
        for(ExecutionResultMultiSource oldResult : oldResultSet){
            if(oldResult.getMatcherName().equals(oldMatcherName)){
                ExecutionResultMultiSource newResult = ExecutionRunnerMultiSource.run(oldResult.getTestCases(), matcher, newMatcherName, oldResult.getAllGraphs(), oldResult.getPartitioner(), oldResult.getAlignment(), oldResult.getParameters());
                if(newResult != null){
                    //update the runtime and compute closure
                    newResult.addRuntime(oldResult.getTotalRuntime());
                    //in case old or new result wants to compute transitive closure, then also the new result computes the transitive closure
                    newResult.updateComputeTransitiveClosure(oldResult.isComputeTransitiveClosure());
                    newResultSet.add(newResult);
                }
            }
        }
        oldResultSet.addAll(newResultSet);
        return oldResultSet;
    }
    
    public static int numberOfSources(ExecutionResultMultiSource result, DatasetIDExtractor idExtractor){
        Alignment alignment = result.getAlignment(Alignment.class);
        if(alignment == null){
            LOGGER.error("Could not analyze number of sources because alignment is null (maybe could not be transformed to alignment class)");
            return 0;
        }
        Set<String> sources = new HashSet<>();
        for(Correspondence c : alignment){
            sources.add(idExtractor.getDatasetID(c.getEntityOne()));
            sources.add(idExtractor.getDatasetID(c.getEntityTwo()));
        }
        return sources.size();
    }
}