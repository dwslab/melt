package de.uni_mannheim.informatik.dws.ontmatching.matchingeval;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import static com.googlecode.cqengine.query.QueryFactory.noQueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.refinement.Refiner;

/**
 * A collection of individual {@link ExecutionResult} instances that are typically returned by an {@link Executor}.
 *
 * @author Sven Hertling
 * @author Jan Portisch
 */
public class ExecutionResultSet extends ConcurrentIndexedCollection<ExecutionResult> implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionResultSet.class);
    
    private HashIndex matcherIndex;
    private HashIndex testCaseIndex;
    private HashIndex trackIndex;
    private HashIndex refinementSetIndex;


    /**
     * Constructor
     */
    public ExecutionResultSet(){
        this.matcherIndex = HashIndex.onAttribute(ExecutionResult.MATCHER);
        this.addIndex(matcherIndex);

        this.testCaseIndex = HashIndex.onAttribute(ExecutionResult.TEST_CASE);
        this.addIndex(testCaseIndex);

        this.trackIndex = HashIndex.onAttribute(ExecutionResult.TRACK);
        this.addIndex(trackIndex);

        this.refinementSetIndex = HashIndex.onAttribute(ExecutionResult.REFINEMENT_SET);
        this.addIndex(refinementSetIndex);
    }


    /**
     * Get a specific {@link ExecutionResult} which fulfills the specified parameters (testCase, matcherName) from
     * the ExecutionResultSet. Note that refinements will be executed on the fly if they have not been executed
     * before.
     * @param testCase The test case that shall match.
     * @param matcherName The matcher name shall match.
     * @param refinements The refinements.
     * @return
     */
    public ExecutionResult get(TestCase testCase, String matcherName, Refiner... refinements){
        ResultSet<ExecutionResult> r = this.retrieve(query(testCase,matcherName, refinements));
        if(r.isNotEmpty())
            return r.uniqueResult();
        
        r = this.retrieve(query(testCase, matcherName, EMPTY_REFINEMENT));
        if(r.isEmpty()){
            LOGGER.info("No raw execution result is contained in executionResultSet");
            return null;
        }        
        ExecutionResult er = r.uniqueResult();
        for(Refiner refinement : refinements){
            er = refinement.refine(er);
        }
        this.add(er);
        return er;
    }
    
    public ExecutionResult get(ExecutionResult basisResult, Refiner... refinements){
        return get(basisResult.getTestCase(), basisResult.getMatcherName(), refinements);
    }
    
    public Set<ExecutionResult> getUnrefinedResults(){
        ResultSet<ExecutionResult> basisResults = this.retrieve(query(EMPTY_REFINEMENT));        
        Set<ExecutionResult> list = new HashSet<>(basisResults.size());
        for(ExecutionResult c : basisResults){
            list.add(c);
        }        
        return list;
    }

    public Set<ExecutionResult> getGroup(TestCase testCase, Refiner... refinements){
        ResultSet<ExecutionResult> basisResults = this.retrieve(query(testCase, EMPTY_REFINEMENT));
        return getGroup(basisResults, refinements);
    }

    
    public Set<ExecutionResult> getGroup(String matcher, Refiner... refinements){
        ResultSet<ExecutionResult> basisResults = this.retrieve(query(matcher, EMPTY_REFINEMENT));
        return getGroup(basisResults, refinements);
    }


    public Set<ExecutionResult> getGroup(Track track, Refiner... refinements){
        ResultSet<ExecutionResult> basisResults = this.retrieve(query(track, EMPTY_REFINEMENT));
        return getGroup(basisResults, refinements);
    }


    public Set<ExecutionResult> getGroup(Track track, String matcher, Refiner... refinements){
        ResultSet<ExecutionResult> basisResults = this.retrieve(query(track, matcher, EMPTY_REFINEMENT));
        return getGroup(basisResults, refinements);
    }


    public Set<ExecutionResult> getGroup(ResultSet<ExecutionResult> basisResults, Refiner... refinements){
        if(refinements.length == 0){
            return basisResults.stream().collect(Collectors.toSet());
        }
        Set<ExecutionResult> result = new HashSet<>();
        for(ExecutionResult basisResult : basisResults){
            ResultSet<ExecutionResult> r = this.retrieve(query(basisResult.getTestCase(), basisResult.getMatcherName(), refinements));
            if(r.isEmpty()){
                ExecutionResult tobeRefined = basisResult;
                for(Refiner refinement : refinements){
                    tobeRefined = refinement.refine(tobeRefined);
                }
                this.add(tobeRefined);
                result.add(tobeRefined);
            }else{
                result.add(r.uniqueResult());
            }
        }
        return result;
    }


    /**
     * Obtain the distinct matcher names in this execution result set.
     * @return Iterable over distinct matcher names.
     */
    public Iterable<String> getDistinctMatchers(){
        //see https://github.com/npgall/cqengine/issues/168
        return this.matcherIndex.getDistinctKeys(noQueryOptions());
    }


    /**
     * Obtain the distinct matcher names in this execution result set that were ran on the specified track.
     * @param track The track on which the matcher ran.
     * @return Iterable over distinct matcher names.
     */
    public Iterable<String> getDistinctMatchers(Track track){
        return this.retrieve(query(track))
                .stream()
                .map(ExecutionResult::getMatcherName)
                .collect(Collectors.toSet());
    }


    /**
     * Given a distinct test case, return the distinct names of matchers that were run on this particular test case.
     * @param testCase The test case for which the matchers that were run shall be retrieved.
     * @return A set of unique matcher names.
     */
    public Iterable<String> getDistinctMatchers(TestCase testCase){
        return this.retrieve(query(testCase))
                .stream()
                .map(ExecutionResult::getMatcherName)
                .collect(Collectors.toSet());
    }


    /**
     * Get the distinct test cases that used in this ExecutionResultSet.
     * @return {@link TestCase} instances as iterable.
     */
    public Iterable<TestCase> getDistinctTestCases(){
        return this.testCaseIndex.getDistinctKeys(noQueryOptions());
    }
    
    public Iterable<TestCase> getDistinctTestCases(String matcher){
        return this.retrieve(query(matcher))
                .stream()
                .map(ExecutionResult::getTestCase)
                .collect(Collectors.toSet());
    }

    /**
     * Get the distinct test cases that used in this ExecutionResultSet for the specified track.
     * @param track The track for which the test cases shall be retrieved.
     * @return An iterable over the resulting test cases for the specified track.
     */
    public Iterable<TestCase> getDistinctTestCases(Track track){
        return this.retrieve(query(track))
                .stream()
                .map(ExecutionResult::getTestCase)
                .collect(Collectors.toSet());
    }

    /**
     * Get the distinct test cases that used in this ExecutionResultSet for the specified track.
     * @param track The track for which the test cases shall be retrieved.
     * @param matcher The matcher for which the test cases shall be retrieved.
     * @return An iterable over the resulting test cases for the specified track.
     */
    public Iterable<TestCase> getDistinctTestCases(Track track, String matcher){
        return this.retrieve(query(track, matcher))
                .stream()
                .map(ExecutionResult::getTestCase)
                .collect(Collectors.toSet());
    }


    /**
     * Get the distinct tracks that used in this ExecutionResultSet.
     * @return Tracks as iterable.
     */
    public Iterable<Track> getDistinctTracks(){
        return this.trackIndex.getDistinctKeys(noQueryOptions());
    }

    /**
     * The distinct tracks on which the specified matcher was run.
     * @param matcher The matcher for which the tracks shall be obtained.
     * @return An iterable over the tracks on which the specified matcher was run.
     */
    public Iterable<Track> getDistinctTracks(String matcher){
       return this.retrieve(query(matcher))
                .stream()
                .map(ExecutionResult::getTrack)
                .collect(Collectors.toSet());
    }


    /**
     * Constant for an empty refinement.
     */
    private final static Refiner[] EMPTY_REFINEMENT = {};

    //-------------------------------------------
    // often used queries:
    //-------------------------------------------

    private static Query<ExecutionResult> query(TestCase testCase, String matcherName, Refiner[] refinements){
        return QueryFactory.and(query(testCase),query(matcherName),query(refinements));
    }
    private static Query<ExecutionResult> query(Track track, String matcherName, Refiner[] refinements){
        return QueryFactory.and(query(track),query(matcherName),query(refinements));
    }
    private static Query<ExecutionResult> query(TestCase testCase, Refiner[] refinements){
        return QueryFactory.and(query(testCase),query(refinements));
    }    
    private static Query<ExecutionResult> query(String matcher, Refiner[] refinements){
        return QueryFactory.and(query(matcher),query(refinements));
    }
    private static Query<ExecutionResult> query(Track track, Refiner[] refinements){
        return QueryFactory.and(query(track),query(refinements));
    }
    private static Query<ExecutionResult> query(Track track, String matcher){
        return QueryFactory.and(query(track),query(matcher));
    }
    private static Query<ExecutionResult> query(Refiner[] refinements){
        return QueryFactory.equal(ExecutionResult.REFINEMENT_SET, new HashSet<>(Arrays.asList(refinements)));
    }
    private static Query<ExecutionResult> query(String matcher){
        return QueryFactory.equal(ExecutionResult.MATCHER, matcher);
    }
    private static Query<ExecutionResult> query(Track track){
        return QueryFactory.equal(ExecutionResult.TRACK, track);
    }
    private static Query<ExecutionResult> query(TestCase testCase){
        return QueryFactory.equal(ExecutionResult.TEST_CASE, testCase);
    }
}
