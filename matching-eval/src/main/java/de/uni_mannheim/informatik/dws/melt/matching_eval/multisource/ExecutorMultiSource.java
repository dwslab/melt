
package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractorUrlPattern;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MatcherMultiSourceURL;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.Track;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ReflexiveCorrespondenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.TransitiveClosure;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;


public class ExecutorMultiSource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorMultiSource.class);
  
    
    public static ExecutionResultSet execute(Track track, Map<String, MatcherMultiSourceURL> matchers){
        return execute(track.getTestCases(), matchers);
    }
    
    public static ExecutionResultSet execute(List<TestCase> testCases, Map<String, MatcherMultiSourceURL> matchers){
        ExecutionResultSet resultSet = new ExecutionResultSet();        
        for(Entry<Track, List<TestCase>> trackToTestcases : groupTestCasesByTrack(testCases).entrySet()){
            Track track = trackToTestcases.getKey();
            List<TestCase> trackTestCases = trackToTestcases.getValue();
            List<URL> distinctOntologies = Track.getDistinctOntologies(trackTestCases);
            for(Entry<String, MatcherMultiSourceURL> matcher : matchers.entrySet()){
                resultSet.addAll(execute(trackTestCases, matcher.getValue(), matcher.getKey(), distinctOntologies, getMostSpecificPartitioner(track)));
            }
        }
        return resultSet;
    }
    
    public static ExecutionResultSet execute(Track track, MatcherMultiSourceURL matcher){
        return execute(track.getTestCases(), matcher,Executor.getMatcherName(matcher));
    }
    
    public static ExecutionResultSet execute(List<TestCase> testCases, MatcherMultiSourceURL matcher){
        return execute(testCases, matcher,Executor.getMatcherName(matcher));
    }
    
    public static ExecutionResultSet execute(List<TestCase> testCases, MatcherMultiSourceURL matcher, String matcherName){
        ExecutionResultSet resultSet = new ExecutionResultSet();        
        for(Entry<Track, List<TestCase>> trackToTestcases : groupTestCasesByTrack(testCases).entrySet()){
            Track track = trackToTestcases.getKey();
            List<TestCase> trackTestCases = trackToTestcases.getValue();
            List<URL> distinctOntologies = Track.getDistinctOntologies(trackTestCases);
            resultSet.addAll(execute(trackTestCases, matcher, matcherName, distinctOntologies, getMostSpecificPartitioner(track)));
        }
        return resultSet;
    }
    
    
    public static ExecutionResultSet executeWithAdditionalGraphs(Track track, MatcherMultiSourceURL matcher, String matcherName, List<URL> additionalGraphs, Partitioner partitioner){
        List<URL> allGraphs = track.getDistinctOntologies();
        allGraphs.addAll(additionalGraphs);
        return execute(track.getTestCases(), matcher, matcherName, allGraphs, partitioner);
    }
    
    public static ExecutionResultSet execute(List<TestCase> testCases, MatcherMultiSourceURL matcher, String matcherName, 
            List<URL> allGraphs, Partitioner partitioner){
        Set<String> trackNames = getTrackNames(testCases);
        LOGGER.info("Running multi source matcher {} on track(s) {}.", matcherName, trackNames);

        long runTime;
        URL resultingAlignment = null;
        long startTime = System.nanoTime();
        try {
            resultingAlignment = matcher.match(allGraphs, null, null);
        } catch (Exception ex) {
            LOGGER.error("Exception during matching (matcher " + matcherName + " on track(s) " +  trackNames + ").", ex);
            return new ExecutionResultSet();
        }
        finally
        {
            runTime = System.nanoTime() - startTime;  
            LOGGER.info("Running matcher {} on track(s) {} completed in {}.", matcherName, trackNames, DurationFormatUtils.formatDurationWords((runTime/1_000_000), true, true));
        }
        
        if(resultingAlignment == null) {
            LOGGER.error("Matching task unsuccessful: output alignment equals null. (matcher: {} track(s): {})", matcherName, trackNames);
            return new ExecutionResultSet();
        }
        
        File alignmentFile = null;
        try {
            alignmentFile = new File(resultingAlignment.toURI());
            alignmentFile.deleteOnExit();
        }catch (URISyntaxException | IllegalArgumentException ex) {
            LOGGER.error("Original system alignment does not point to a file. The file can not be deleted and an empty execution result is returned.", ex);
            return new ExecutionResultSet();
        }
        
        return fromAlignmentFile(alignmentFile, testCases, matcherName, runTime, matcher.needsTransitiveClosureForEvaluation(), partitioner);
    }
    
    
    public static ExecutionResultSet fromAlignmentFile(File fullAlignmentFile, List<TestCase> testCases, String matcherName, long runTime, boolean computeTransitiveClosure, Partitioner partitioner){
        Alignment fullAlignment = null;
        try {
            fullAlignment = AlignmentParser.parse(fullAlignmentFile);
        } catch (SAXException  ex) {
            LOGGER.error("The produced alignment file cannot be parsed. An empty execution result is returned", ex);
            return new ExecutionResultSet();
        } catch (IOException ex) {
            LOGGER.error("The produced alignment file cannot be read. An empty execution result is returned", ex);
            return new ExecutionResultSet();
        }
        
        //remove reflexive edges
        fullAlignment = ReflexiveCorrespondenceFilter.removeReflexiveCorrespondences(fullAlignment);
        
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
        
        ExecutionResultSet resultSet = new ExecutionResultSet();
        for(TestCase testCase : testCases){
            resultSet.add(new ExecutionResult(
                    testCase, 
                    matcherName, 
                    null,
                    runTime,
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
    
    /**
     * Returns the most specific partitioner for a given track
     * @param track the track
     * @return the most specific partitioner
     */
    public static Partitioner getMostSpecificPartitioner(Track track){
        if(KG_TRACKS.contains(track)){
            return new PartitionerFromDatasetIdExtractor(track, DatasetIDExtractorUrlPattern.KG_TRACK_EXTRACTOR);
        }else if(CONFERENCE_TRACKS.contains(track)){
            return new PartitionerFromDatasetIdExtractor(track, DatasetIDExtractorUrlPattern.CONFERENCE_TRACK_EXTRACTOR);
        } else{
            return new PartitionerDefault(track);
        }
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
    
}