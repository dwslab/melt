
package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractorUrlPattern;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherMultiSourceCaller;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.Track;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ReflexiveCorrespondenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.TransitiveClosure;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExecutorMultiSource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorMultiSource.class);
      
    public static ExecutionResultSet runMultipleMatchersMultipleTracks(List<Track> tracks, Map<String, Object> matchers){
        ExecutionResultSet results = new ExecutionResultSet();
        for(Track track : tracks){
            results.addAll(ExecutorMultiSource.runMultipleMatchers(track.getTestCases(), matchers));
        }
        return results;
    }
    
    public static ExecutionResultSet runMultipleMatchers(Track track, Map<String, Object> matchers){
        return ExecutorMultiSource.runMultipleMatchers(track.getTestCases(), matchers);
    }
    
    public static ExecutionResultSet runMultipleMatchers(List<TestCase> testCases, Map<String, Object> matchers){
        ExecutionResultSet resultSet = new ExecutionResultSet();        
        for(Entry<Track, List<TestCase>> trackToTestcases : groupTestCasesByTrack(testCases).entrySet()){
            Track track = trackToTestcases.getKey();
            List<TestCase> trackTestCases = trackToTestcases.getValue();
            List<URL> distinctOntologies = Track.getDistinctOntologies(trackTestCases);
            for(Entry<String, Object> matcher : matchers.entrySet()){
                resultSet.addAll(ExecutorMultiSource.run(trackTestCases, matcher.getValue(), matcher.getKey(), distinctOntologies, getMostSpecificPartitioner(track)));
            }
        }
        return resultSet;
    }
    
    public static ExecutionResultSet run(Track track, Object matcher){
        return ExecutorMultiSource.run(track.getTestCases(), matcher,Executor.getMatcherName(matcher));
    }
    
    public static ExecutionResultSet run(List<TestCase> testCases, Object matcher){
        return ExecutorMultiSource.run(testCases, matcher,Executor.getMatcherName(matcher));
    }
    
    public static ExecutionResultSet run(List<TestCase> testCases, Object matcher, String matcherName){
        ExecutionResultSet resultSet = new ExecutionResultSet();        
        for(Entry<Track, List<TestCase>> trackToTestcases : groupTestCasesByTrack(testCases).entrySet()){
            Track track = trackToTestcases.getKey();
            List<TestCase> trackTestCases = trackToTestcases.getValue(); // in case not all testcases of a track are included.
            List<URL> distinctOntologies = Track.getDistinctOntologies(trackTestCases);
            resultSet.addAll(ExecutorMultiSource.run(trackTestCases, matcher, matcherName, distinctOntologies, getMostSpecificPartitioner(track)));
        }
        return resultSet;
    }
    
    
    public static ExecutionResultSet runWithAdditionalGraphs(Track track, Object matcher, String matcherName, List<URL> additionalGraphs, Partitioner partitioner){
        List<URL> allGraphs = track.getDistinctOntologies();
        allGraphs.addAll(additionalGraphs);
        return ExecutorMultiSource.run(track.getTestCases(), matcher, matcherName, allGraphs, partitioner);
    }
    
    public static ExecutionResultSet run(List<TestCase> testCases, Object matcher, String matcherName, 
            List<URL> allGraphs, Partitioner partitioner){
        Set<String> trackNames = getTrackNames(testCases);
        Alignment inputAlignment = getInputAlignment(testCases);
        Properties parameters = getParameters(testCases);
        LOGGER.info("Running multi source matcher {} on track(s) {}.", matcherName, trackNames);
        
        long runTime;
        Alignment resultingAlignment = null;
        long startTime = System.nanoTime();
        try {
            AlignmentAndParameters result = GenericMatcherMultiSourceCaller.runMatcherMultiSourceSpecificType(matcher, allGraphs, inputAlignment, parameters);
            resultingAlignment = result.getAlignment(Alignment.class);
            //resultingAlignment = matcher.match(allGraphs, null, null);
        } catch (Exception ex) {
            LOGGER.error("Exception during matching (matcher " + matcherName + " on track(s) " +  trackNames + ").", ex);
            return new ExecutionResultSet();
        }
        finally
        {
            runTime = System.nanoTime() - startTime;  
            LOGGER.info("Running matcher {} on track(s) {} completed in {}.", matcherName, trackNames, DurationFormatUtils.formatDurationWords((runTime/1_000_000), true, true));
        }
        
        boolean needsTransitiveClosure = GenericMatcherMultiSourceCaller.needsTransitiveClosureForEvaluation(matcher);
        return fromAlignment(resultingAlignment, testCases, matcherName, runTime, needsTransitiveClosure, partitioner);
    }
    
    
    public static ExecutionResultSet fromAlignment(Alignment fullAlignment, List<TestCase> testCases, String matcherName, long totalRuntime, boolean computeTransitiveClosure, Partitioner partitioner){
        //remove reflexive edges
        fullAlignment = ReflexiveCorrespondenceFilter.removeReflexiveCorrespondences(fullAlignment);
        
        if(allTestCasesFromSameTrack(testCases) == false)
            LOGGER.warn("Not all test cases are from the same track. The runtime of the tracks will not be correctly computed. Be warned.");
        
        Map<TestCase, Alignment> testcaseToAlignment = new HashMap<>();
        if(computeTransitiveClosure){
            TransitiveClosure<String> alignmentClosure = new TransitiveClosure<>();
            for(Correspondence c : fullAlignment){
                alignmentClosure.add(c.getEntityOne(), c.getEntityTwo());
            }
            for(Set<String> sameAs : alignmentClosure.getClosure()){
                Map<TestCase, SourceTargetURIs> map = partitioner.partition(sameAs);
                for(Entry<TestCase, SourceTargetURIs> entry : map.entrySet()){
                    SourceTargetURIs sourceTargetUris = entry.getValue();
                    if(sourceTargetUris.containsSourceAndTarget() == false)
                        continue;
                    Alignment alignment = testcaseToAlignment.computeIfAbsent(entry.getKey(), __->new Alignment());
                    for(String sourceURI : sourceTargetUris.getSourceURIs()){
                        for(String targetURI : sourceTargetUris.getTargetURIs()){
                            //TODO: confidence extensions etc
                            alignment.add(sourceURI, targetURI);
                        }
                    }
                }
            }
        }else{
            for(Correspondence c : fullAlignment){
                Map<TestCase, SourceTargetURIs> map = partitioner.partition(Arrays.asList(c.getEntityOne(), c.getEntityTwo()));
                for(Entry<TestCase, SourceTargetURIs> entry : map.entrySet()){
                    SourceTargetURIs sourceTargetUris = entry.getValue();
                    if(sourceTargetUris.containsSourceAndTarget() == false)
                        continue;
                    Alignment alignment = testcaseToAlignment.computeIfAbsent(entry.getKey(), __->new Alignment());
                    for(String sourceURI : sourceTargetUris.getSourceURIs()){
                        for(String targetURI : sourceTargetUris.getTargetURIs()){
                            alignment.add(sourceURI, targetURI, c.getConfidence(), c.getRelation(), c.getExtensions());
                        }
                    }
                }
            }
        }
        
        long runtimePerTestCase = totalRuntime / testCases.size();
        
        ExecutionResultSet resultSet = new ExecutionResultSet();
        for(TestCase testCase : testCases){
            resultSet.add(new ExecutionResult(
                    testCase, 
                    matcherName, 
                    null,
                    runtimePerTestCase,
                    testcaseToAlignment.getOrDefault(testCase, new Alignment()), 
                    testCase.getParsedReferenceAlignment(),
                    null,
                    null
            ));
        }
        return resultSet;
    }
    
    private static final Set<Track> KG_TRACKS = TrackRepository.retrieveDefinedTracks(TrackRepository.Knowledgegraph.class);
    private static final Set<Track> CONFERENCE_TRACKS = TrackRepository.retrieveDefinedTracks(TrackRepository.Conference.class);
    private static final Set<Track> LARGE_BIO_TRACKS = TrackRepository.retrieveDefinedTracks(TrackRepository.Largebio.V2016.class);
    
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
    
    public static long getSummedRuntimeOfAllUnrefinedResults(ExecutionResultSet results){
        long summedRuntime = 0;
        for(ExecutionResult r : results.getUnrefinedResults()){
            summedRuntime += r.getRuntime();
        }
        return summedRuntime;
    }
    
    
    private static Map<Track, List<TestCase>> groupTestCasesByTrack(List<TestCase> testCases){
        Map<Track, List<TestCase>> map = new HashMap<>();
        for(TestCase testCase : testCases){
            map.computeIfAbsent(testCase.getTrack(), __-> new ArrayList<>()).add(testCase);
        }
        return map;
    }
    
    private static Set<String> getTrackNames(List<TestCase> testCases){
        Set<String> trackNames = new HashSet<>();
        for(TestCase testCase : testCases){
            trackNames.add(testCase.getTrack().getName());
        }
        return trackNames;
    }
    
    private static Alignment getInputAlignment(List<TestCase> testCases){
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
}