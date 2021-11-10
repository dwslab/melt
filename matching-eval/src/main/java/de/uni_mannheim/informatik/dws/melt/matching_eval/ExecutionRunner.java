package de.uni_mannheim.informatik.dws.melt.matching_eval;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherCaller;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Individual execution object for parallel execution.
 * Development Remark: Not a public class because this should not be called by user of framework.
 * @author Sven Hertling
 * @author Jan Portisch
 */
class ExecutionRunner implements Callable<ExecutionResult> {


    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionRunner.class);
    
    private TestCase testCase;
    private Object matcher;
    private String matcherName;
    private Object inputAlignment;
    private Object parameters;

    public ExecutionRunner(TestCase testCase, Object matcher, String matcherName, Object inputAlignment, Object parameters) {
        this.testCase = testCase;
        this.matcher = matcher;
        this.matcherName = matcherName;
        this.inputAlignment = inputAlignment;
        this.parameters = parameters;
    }
    
    public ExecutionRunner(TestCase testCase, Object matcher, String matcherName) {
        this.testCase = testCase;
        this.matcher = matcher;
        this.matcherName = matcherName;
        try {
            this.inputAlignment = getUrlOrNull(testCase.getInputAlignment());
        } catch (MalformedURLException ex) {
            LOGGER.warn("URL of input alignment cannot be converted to URL. Set to null. Be careful.", ex);
            this.inputAlignment = null;
        }
        try {
            this.parameters = getUrlOrNull(testCase.getParameters());
        } catch (MalformedURLException ex) {
            LOGGER.warn("URL of parameters cannot be converted to URL. Set to null. Be careful.", ex);
            this.parameters = null;
        }
    }
    

    @Override
    public ExecutionResult call() {
        Thread.currentThread().setName(matcherName + "-" + testCase.getName());
        return runMatcher(testCase, matcher, matcherName, inputAlignment, parameters);
    }

    /**
     * Run an individual matcher on an individual test case.
     * @param testCase Test case to be used for run.
     * @param matcher The matcher to be run. This class should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @param matcherName Name of the matcher.
     * @return ExecutionResult Object
     */
    public static ExecutionResult runMatcher(TestCase testCase, Object matcher, String matcherName){
        try {
            return runMatcher(testCase, matcher, matcherName, getUrlOrNull(testCase.getInputAlignment()), getUrlOrNull(testCase.getParameters()));
        } catch (MalformedURLException ex) {
            LOGGER.error("Cannot call mactehr because the file URI cannot be transformed into a URL", ex);
            return null;
        }
    }
    
    /**
     * Run an individual matcher on an individual test case.
     * @param testCase Test case to be used for run.
     * @param matcher The matcher to be run. This class should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @param matcherName Name of the matcher.
     * @return ExecutionResult Object
     */
    public static ExecutionResult runMatcher(TestCase testCase, Object matcher, String matcherName, Object inputAlignment, Object parameters){
        String trackName = "<null>";
        if(testCase.getTrack() != null){
            trackName = testCase.getTrack().getName();
        }
        LOGGER.info("Running matcher {} on testcase {} (track {}).",matcherName, testCase.getName(),trackName);
        long runTime;
        URL resultingAlignment = null;
        long startTime = System.nanoTime();
        
        
        try {
            AlignmentAndParameters alignmentAndParameters = GenericMatcherCaller.runMatcher(matcher, 
                    getUrlOrNull(testCase.getSource()),
                    getUrlOrNull(testCase.getTarget()),
                    inputAlignment, 
                    parameters);
            resultingAlignment = alignmentAndParameters.getAlignment(URL.class);
        } catch (Exception ex) {
            LOGGER.error("Exception during matching (matcher " + matcherName + " on testcase " +  testCase.getName() + ").", ex);
        }
        finally
        {
            runTime = System.nanoTime() - startTime;  
            LOGGER.info("Running matcher {} on testcase {} (track {}) completed in {}.", matcherName,
                    testCase.getName(), trackName, DurationFormatUtils.formatDurationWords(runTime/1_000_000, true,
                            true));
        }
        if(resultingAlignment == null) {
            LOGGER.error("Matching task unsuccessful: output alignment equals null. (matcher: {} testcase: {} track: " +
                    "{})", matcherName, testCase.getName(), trackName);
        } else {
            try {
                new File(resultingAlignment.toURI()).deleteOnExit();
            } catch (URISyntaxException | IllegalArgumentException ex) {
                LOGGER.error("Original system alignment does not point to a file and thus cannot be deleted on evaluation exit. " +
                        "Use Executor.deleteOriginalSystemResults", ex);
            }
            return new ExecutionResult(testCase, matcherName, resultingAlignment, runTime, matcher);
        }
        return null;
        
    }
    
    private static URL getUrlOrNull(URI uri) throws MalformedURLException{
        if(uri == null)
            return null;
        return uri.toURL();
    }
}
