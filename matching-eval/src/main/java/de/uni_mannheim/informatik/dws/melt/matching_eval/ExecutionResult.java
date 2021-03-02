package de.uni_mannheim.informatik.dws.melt.matching_eval;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import de.uni_mannheim.informatik.dws.melt.matching_eval.refinement.Refiner;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.Track;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * This class represents the result of a matcher execution.
 *
 * @author Sven Hertling
 * @author Jan Portisch
 */
public class ExecutionResult {


    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionResult.class);
    
    private TestCase testCase;
    private String matcherName;    
    private URL originalSystemAlignment;

    /**
     * The runtime in nanoseconds
     */
    private long runtime;
    private Alignment systemAlignment;
    private Alignment referenceAlignment;
    private IOntologyMatchingToolBridge matcher;
    private Set<Refiner> refinements;

    /**
     * Reference to the log message file of the matcher.
     */
    File matcherLog;

    /**
     * File reference to the error log of the matcher.
     */
    File matcherErrorLog;

    /**
     * Base constructor which needs all parameters
     * @param testCase Test case on which the matcher was run which produced this particular result.
     * @param matcherName Name of the matcher 
     * @param originalSystemAlignment original URL return by a matcher (represents a mapping file)
     * @param runtime Runtime used by the matcher
     * @param systemAlignment Alignment to the alignment output produced by the matcher
     * @param referenceAlignment Alignment to the reference alignment
     * @param matcher Reference to the mather which was used
     * @param refinements Refinements which were executed on this executionResult
     */
    public ExecutionResult(TestCase testCase, String matcherName, URL originalSystemAlignment, long runtime, Alignment systemAlignment, Alignment referenceAlignment, IOntologyMatchingToolBridge matcher, Set<Refiner> refinements) {
        this.testCase = testCase;
        this.matcherName = matcherName;
        this.runtime = runtime;
        this.originalSystemAlignment = originalSystemAlignment;
        this.systemAlignment = systemAlignment;
        this.referenceAlignment = referenceAlignment;
        this.matcher = matcher;
        if(refinements != null) this.refinements = refinements;
        else this.refinements = new HashSet<>();
    }
    
    /**
     * Constructor used by tests to check if metrics compute correctly.
     * @param testCase Test case on which the matcher was run which produced this particular result.
     * @param matcherName Name of the matcher 
     * @param systemAlignment Alignment to the alignment output produced by the matcher
     * @param referenceAlignment Alignment to the reference alignment
     */
    public ExecutionResult(TestCase testCase, String matcherName, Alignment systemAlignment, Alignment referenceAlignment) {
        this(testCase, matcherName, null, 0, systemAlignment, referenceAlignment, null, new HashSet<>());
    }

    /**
     * Constructor used by ExecutionRunner for initializing a execution result from a matcher run
     * @param testCase Test case on which the matcher was run which produced this particular result.
     * @param matcherName Name of the matcher.
     * @param originalSystemAlignment URL where the system alignment is persisted in. Note that this URL is parsed immediately into a mapping.
     * @param runtime Runtime by the matcher.
     * @param matcher Matcher that was used for the testCase.
     */
    public ExecutionResult(TestCase testCase, String matcherName, URL originalSystemAlignment, long runtime, IOntologyMatchingToolBridge matcher) {
        this(testCase, matcherName, originalSystemAlignment, runtime, null, testCase.getParsedReferenceAlignment(), matcher, new HashSet<>());
    }

    /**
     * Copies all members except the mappings from the given execution result (like a copy constructor).
     * It should be used by all refinement operations to create a new executionResult with modified system and referenceAlignments.
     * The refinement is not executed.
     * @param base The base execution result from which all members except the mappings are copied.
     * @param systemAlignment The new system alignment which should be used.
     * @param referenceAlignment The reference alignment.
     * @param refinement The refinement that was used.
     */
    public ExecutionResult(ExecutionResult base, Alignment systemAlignment, Alignment referenceAlignment, Refiner refinement) {
        this(base.testCase, base.matcherName, base.originalSystemAlignment, base.runtime, systemAlignment, referenceAlignment, base.matcher, addRefinementToNewSet(base.refinements, refinement));
    }
    
    /**
     * Helper method to create a new refinement set and add a new refinement to it.
     * Used by one constructor of this class. This method returns a NEW refinement set.
     * @param initialRefinement The original refinement, all data will also be found in the newly created refinement set.
     * @param newRefinement Data that will be added.
     * @return New Refinement Set.
     */
    private static Set<Refiner> addRefinementToNewSet(Set<Refiner> initialRefinement, Refiner newRefinement){
        Set<Refiner> s = new HashSet<>(initialRefinement);
        if(newRefinement != null)
            s.add(newRefinement);
        return s;
    }
    
    /**
     * Helper method to parse an alignment from an URL and log a possible error.
     * This method will not throw any exceptions.
     * Used by one constructor of this class.
     * @param url url which represents the alignment
     * @return Parsed alignment.
     */
    private static Alignment silentlyParseAlignment(URL url){
        try {
            return AlignmentParser.parse(url);
        }catch(FileNotFoundException ex){
            LOGGER.error("The system alignment file with URL {} does not exist. Returning empty system alignment.", url);
        }
        catch (SAXException | IOException | NullPointerException ex) {
            LOGGER.error("The system alignment given by following URL could not be parsed: " + url.toString(), ex);
        }
        return new Alignment();
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public String getMatcherName() {
        return matcherName;
    }

    /**
     * Returns the runtime in nanoseconds
     * @return the runtime in nanoseconds
     */
    public long getRuntime() {
        return runtime;
    }

    public IOntologyMatchingToolBridge getMatcher() {
        return matcher;
    }    

    public Alignment getSystemAlignment() {
        if(this.systemAlignment == null){
            if(this.originalSystemAlignment == null){
                LOGGER.warn("originalSystemAlignment and systemAlignment is null - returned an empty alignment.");
                return new Alignment();                
            }
            this.systemAlignment = silentlyParseAlignment(this.originalSystemAlignment);            
        }
        return this.systemAlignment;
    }

    public Alignment getReferenceAlignment() {
        return this.referenceAlignment;
    }

    public URL getOriginalSystemAlignment() {
        return originalSystemAlignment;
    }

    public File getMatcherLog() {
        return matcherLog;
    }

    public void setMatcherLog(File matcherLog) {
        this.matcherLog = matcherLog;
    }

    public File getMatcherErrorLog() {
        return matcherErrorLog;
    }

    public void setMatcherErrorLog(File matcherErrorLog) {
        this.matcherErrorLog = matcherErrorLog;
    }

    /**
     * Sets a new system alignment. Be aware of what you are doing.
     * This shall only be used in very rare cases such as residual recall calculations.
     * @param systemAlignment System alignment to be set.
     * @deprecated better create a new execution result. This method will be removed in future releases.
     */
    public void setSystemAlignment(Alignment systemAlignment){
        this.systemAlignment = systemAlignment;
    }

    /**
     * Sets a new reference alignment. Be aware of what you are doing.
     * This shall only be used in very rare cases such as residual recall calculations.
     * @param referenceAlignment Reference alignment to be set.
     * @deprecated better create a new execution result. This method will be removed in future releases.
     */
    public void setReferenceAlignment(Alignment referenceAlignment) {
        this.referenceAlignment = referenceAlignment;
    }

    /**
     * Get the source ontology using a buffer and the default OntModelSpec.
     * @param clazz The result type that is expected.
     * @param <T> Type of the ontology class e.g. OntModel
     * @return Source ontology in the specified format.
     */
    public <T> T getSourceOntology(Class<T> clazz){
        return testCase.getSourceOntology(clazz);
    }

    /**
     * Get the target ontology using a buffer and the default OntModelSpec.
     * @param clazz The result type that is expected.
     * @param <T> Type of the ontology class e.g. OntModel
     * @return Target ontology in the specified format.
     */
    public <T> T getTargetOntology(Class<T> clazz){
        return testCase.getTargetOntology(clazz);
    }

    /**
     * Convenience getter which returns the track to which the test case of the ExecutionResult belongs to.
     * @return Instance of the track.
     */
    public Track getTrack(){
        return this.testCase.getTrack();
    }

    public Set<Refiner> getRefinements() {
        return refinements;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.testCase);
        hash = 79 * hash + Objects.hashCode(this.matcherName);
        hash = 79 * hash + Objects.hashCode(this.refinements);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExecutionResult other = (ExecutionResult) obj;
        if (!Objects.equals(this.matcherName, other.matcherName)) {
            return false;
        }
        if (!Objects.equals(this.testCase, other.testCase)) {
            return false;
        }
        return Objects.equals(this.refinements, other.refinements);
    }

    @Override
    public String toString() {
        return "ExecutionResult{testCase=" + testCase.getName() + ", matcherName=" + matcherName + ", refinements=" + refinements + '}';
    }

    /**
     * Returns a comparator that can be used to sort multiple ExecutionResults by matcher name.
     * @return Comparator that compares ExecutionResults by matcher name.
     */
    public static Comparator<ExecutionResult> getMatcherNameComparator(){
        return new Comparator<ExecutionResult>() {
            @Override
            public int compare(ExecutionResult o1, ExecutionResult o2) {
                return (int) (o1.getMatcherName().compareTo(o2.getMatcherName()));
            }
        };
    }

    /**
     * This method allows the MATCHER to be an index within a collection.
     */
    public static final Attribute<ExecutionResult, String> MATCHER = new SimpleAttribute<ExecutionResult, String>("matcher") {
        @Override
        public String getValue(ExecutionResult c, QueryOptions queryOptions) { return c.matcherName; }
    };

    /**
     * This method allows the TRACK to be an index within a collection.
     */
    public static final Attribute<ExecutionResult, Track> TRACK = new SimpleAttribute<ExecutionResult, Track>("track") {
        @Override
        public Track getValue(ExecutionResult c, QueryOptions queryOptions) { return c.testCase.getTrack(); }
    };

    /**
     * This method allows the TEST_CASE to be an index within a collection.
     */
    public static final Attribute<ExecutionResult, TestCase> TEST_CASE = new SimpleAttribute<ExecutionResult, TestCase>("testcase") {
        @Override
        public TestCase getValue(ExecutionResult c, QueryOptions queryOptions) { return c.testCase; }
    };

    /**
     * This method allows REFINEMENT_SET to be an index within a collection.
     */
    public static final Attribute<ExecutionResult, Set<Refiner>> REFINEMENT_SET = new SimpleAttribute<ExecutionResult, Set<Refiner>>("refinementset") {
       @Override
        public Set<Refiner> getValue(ExecutionResult c, QueryOptions queryOptions) { return c.refinements; }        
    };
}
