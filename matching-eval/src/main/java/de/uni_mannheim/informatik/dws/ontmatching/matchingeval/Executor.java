package de.uni_mannheim.informatik.dws.ontmatching.matchingeval;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Executor runs a matcher or a list of matchers on a single test case or a list of test cases.
 * Also have a look at other executors like ExecutorParallel or ExecutorSeals.
 * @author Sven Hertling
 * @author Jan Portisch
 */
public class Executor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Executor.class);

    /**
     * This method runs the specified matcher on the specified track.
     * @param track Track on which the matcher shall be run.
     * @param matcher The matcher to be run.
     * @return An {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(Track track, IOntologyMatchingToolBridge matcher) {
        return run(track, matcher, getMatcherName(matcher));
    }


    public static ExecutionResultSet run(Track track, IOntologyMatchingToolBridge matcher, String matcherName) {
        return run(track.getTestCases(), matcher, matcherName);
    }
    
    public static ExecutionResultSet run(Track track, Map<String, IOntologyMatchingToolBridge> matchers) {
        return run(track.getTestCases(), matchers);
    }
        
    public static ExecutionResultSet runTracks(List<Track> tracks, IOntologyMatchingToolBridge matcher, String matcherName) {
        ExecutionResultSet r = new ExecutionResultSet();
        for(Track track : tracks){
            for (TestCase tc : track.getTestCases()) {
                r.add(ExecutionRunner.runMatcher(tc, matcher, matcherName));
            }
        }
        return r;
    }

    /**
     * Run a set of matchers on a set of test cases.
     *
     * @param testCases The test cases on which all the specified matchers shall be run.
     * @param matchers  A map of matchers from unique_name to matcher instance.
     * @return The result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(List<TestCase> testCases, Map<String, IOntologyMatchingToolBridge> matchers) {
        ExecutionResultSet r = new ExecutionResultSet();
        for (Entry<String, IOntologyMatchingToolBridge> matcher : matchers.entrySet()) {
            for (TestCase tc : testCases) {
                r.add(ExecutionRunner.runMatcher(tc, matcher.getValue(), matcher.getKey()));
            }
        }
        return r;
    }
    
    /**
     * Run a set of matchers on a specific test cases.
     *
     * @param testCase One specific testcase on which all the specified matchers shall be run.
     * @param matchers  A map of matchers from unique_name to matcher instance.
     * @return The result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(TestCase testCase, Map<String, IOntologyMatchingToolBridge> matchers) {
        ExecutionResultSet r = new ExecutionResultSet();
        for (Entry<String, IOntologyMatchingToolBridge> matcher : matchers.entrySet()) {
            r.add(ExecutionRunner.runMatcher(testCase, matcher.getValue(), matcher.getKey()));
        }
        return r;
    }

    /**
     * Run a matcher on a set of test cases.
     *
     * @param testCases The test cases on which all the specified matcher shall be run.
     * @param matcher   The matcher to be run.
     * @return The result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(List<TestCase> testCases, IOntologyMatchingToolBridge matcher) {
        return run(testCases, matcher, getMatcherName(matcher));
    }

    public static ExecutionResultSet run(List<TestCase> testCases, IOntologyMatchingToolBridge matcher, String matcherName) {
        ExecutionResultSet r = new ExecutionResultSet();
        for (TestCase tc : testCases) { // no parallelism possible because each matcher possibly writes to the same temp file.
            r.add(ExecutionRunner.runMatcher(tc, matcher, matcherName));
        }
        return r;
    }

    /**
     * Run the specified matcher on the specified test case.
     *
     * @param testCase The test case on which the matcher shall be executed.
     * @param matcher  The matcher to be executed.
     * @return Result in the form of an {@link ExecutionResultSet}.
     */
    public static ExecutionResultSet run(TestCase testCase, IOntologyMatchingToolBridge matcher) {
        return run(testCase, matcher, getMatcherName(matcher));
    }

    public static ExecutionResultSet run(TestCase tc, IOntologyMatchingToolBridge matcher, String matcherName) {
        ExecutionResultSet r = new ExecutionResultSet();
        r.add(ExecutionRunner.runMatcher(tc, matcher, matcherName));
        return r;
    }

    /**
     * Run a single test case with one matcher.
     *
     * @param testCase    Test case to be evaluated.
     * @param matcher     Matcher to be evaluated.
     * @param matcherName The name of the matcher.
     * @return Single Execution Result
     */
    public static ExecutionResult runSingle(TestCase testCase, IOntologyMatchingToolBridge matcher, String matcherName) {
        return ExecutionRunner.runMatcher(testCase, matcher, matcherName);
    }


    /**
     * Run a single test case with one matcher.
     *
     * @param testCase Test case to be evaluated.
     * @param matcher  Matcher to be evaluated.
     * @return Single Execution Result
     */
    public static ExecutionResult runSingle(TestCase testCase, IOntologyMatchingToolBridge matcher) {
        return ExecutionRunner.runMatcher(testCase, matcher, getMatcherName(matcher));
    }


    /**
     * Load raw results from folder structure like:
     * folder
     * - ALIGN.rdf
     * - LogMap.rdf
     * File names are treated as matcher names and they are associated with the given testcase.
     *
     * @param folder   The folder where the system results can be found.
     * @param testCase Test case with which the individual system results shall be associated.
     * @return {@link ExecutionResultSet} instance with the loaded results.
     */
    public static ExecutionResultSet loadFromFolder(File folder, TestCase testCase) {
        if (!folder.isDirectory()) {
            LOGGER.error("The specified folder is not a directory. Returning null.");
            return null;
        }
        ExecutionResultSet results = new ExecutionResultSet();
        for (File f : folder.listFiles()) {
            if (f.isFile() && f.getName().endsWith(".rdf")) {
                try {
                    results.add(new ExecutionResult(testCase, FilenameUtils.removeExtension(f.getName()), f.toURI().toURL(), 0, null));
                } catch (MalformedURLException ex) {
                    LOGGER.error("Cannot convert file URI to URL.", ex);
                }
            }
        }
        return results;
    }


    /**
     * Load results directly from a folder. The results have to be run on the same testCase.
     * Folder structure like:
     * folder
     * - ALIGN.rdf
     * - LogMap.rdf
     * File names are treated as matcher names and they are associated with the given testcase.
     * This structure is equal to the structure used by the
     * <a href="http://oaei.ontologymatching.org/2018/results/anatomy/index.html">Anatomy Track</a> of the OAEI.
     *
     * @param pathToFolder The folder where the system results can be found.
     * @param testCase     Test case with which the individual system results shall be associated.
     * @return {@link ExecutionResultSet} instance with the loaded results.
     */
    public static ExecutionResultSet loadFromFolder(String pathToFolder, TestCase testCase) {
        File folder = new File(pathToFolder);
        return loadFromFolder(folder, testCase);
    }


    /**
     * Load raw results from a folder with the structure used by the <a href="http://oaei.ontologymatching.org/2018/results/anatomy/index.html">Anatomy Track</a>
     * of the OAEI.
     */
    public static ExecutionResultSet loadFromAnatomyResultsFolder(String pathToFolder) {
        return loadFromFolder(pathToFolder, TrackRepository.Anatomy.Default.getTestCases().get(0));
    }


    /**
     * Load raw results from a folder with the structure used by the <a href="http://oaei.ontologymatching.org/2018/results/conference/">Conference Track</a>
     * of the OAEI.
     *
     * @param pathToFolder The path to the folder where the alignments of the conference folder reside.
     */
    public static ExecutionResultSet loadFromConferenceResultsFolder(String pathToFolder){
        ExecutionResultSet results = new ExecutionResultSet();
        File folder = new File(pathToFolder);
        if (!folder.isDirectory()) {
            LOGGER.error("The specified folder is not a directory. Returning null.");
            return null;
        }
        HashMap<String, TestCase> name2tc = new HashMap<>();
        for(TestCase tc : TrackRepository.Conference.V1.getTestCases()){
            name2tc.put(tc.getName(), tc);
        }
        Pattern pattern = Pattern.compile("^[^-]*(?=-)"); // ^[^-]*(?=-)
        for(File f: folder.listFiles()) {
            if(!f.getName().endsWith(".rdf")){
                LOGGER.info("Skipping file " + f.getName() + " because it does not end with \".rdf\".");
                continue;
            }
            try {
                String fileName = f.getName();
                Matcher matcher = pattern.matcher(fileName);
                matcher.find();
                String matcherName = matcher.group();
                fileName = fileName.replace(matcherName + "-", "");
                fileName = fileName.replace(".rdf", "");

                TestCase testCase = name2tc.get(fileName);

                if(testCase != null) {
                    results.add(new ExecutionResult(testCase,matcherName, f.toURI().toURL(), 0, null));
                } else LOGGER.error("Could not find test case " + fileName + " of file " + f.getName());
            } catch (IllegalStateException ise){
                LOGGER.error("Could not parse file name: " + f.getName());
            } catch (MalformedURLException mfe){
                LOGGER.error("Could not build URL for file " + f.getName());
            }
        }
        LOGGER.info(results.size() + " results loaded.");
        return results;
    }


    private static final String FALLBACK_MATCHER_NAME = "default_matcher";

    /**
     * Given a matcher, this method returns its name.
     *
     * @param matcher Matcher whose name shall be retrieved.
     * @return The name as String.
     */
    public static String getMatcherName(IOntologyMatchingToolBridge matcher) {
        //https://stackoverflow.com/questions/22866925/detect-if-object-has-overriden-tostring
        try {
            if (matcher.getClass().getMethod("toString").getDeclaringClass() != Object.class) {
                return matcher.toString();
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            LOGGER.debug("No access to toString method of matcher.", ex);
        }
        return getMatcherName(matcher.getClass());
    }

    /**
     * Given a matcher, this method returns its name.
     *
     * @param matcher Matcher whose name shall be retrieved.
     * @return The name as String.
     */
    public static String getMatcherName(Class<? extends IOntologyMatchingToolBridge> matcher) {
        String name = matcher.getSimpleName();
        if (name == null)
            return FALLBACK_MATCHER_NAME;
        if (name.trim().isEmpty())
            return FALLBACK_MATCHER_NAME;
        return name;
    }

    /**
     * Deletes all system results which are stored usually in the tmp folder.
     * This will not delete anything in the results folder.
     *
     * @param er the execution results where all system alignments should be removed.
     * @return <code>true</code> if and only if all results are
     * successfully deleted; <code>false</code> otherwise
     */
    public static boolean deleteOriginalSystemResults(ExecutionResultSet er) {
        boolean allDeleted = true;
        for (ExecutionResult r : er) {
            try {
                if (r.getOriginalSystemAlignment() != null) {
                    if (!new File(r.getOriginalSystemAlignment().toURI()).delete()) {
                        allDeleted = false;
                    }
                }
            } catch (URISyntaxException ex) {
                LOGGER.warn("System alignment URL can not be converted to URI.", ex);
                allDeleted = false;
            }
        }
        return allDeleted;
    }

}
