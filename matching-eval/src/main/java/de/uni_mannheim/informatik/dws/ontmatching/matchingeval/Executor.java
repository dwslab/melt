package de.uni_mannheim.informatik.dws.ontmatching.matchingeval;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Executor runs a matcher or a list of matchers on a single test case or a list of test cases.
 * Also have a look at other executors like ExecutorParallel or ExecutorSeals.
 *
 * @author Sven Hertling
 * @author Jan Portisch
 */
public class Executor {

    /**
     * Default logger.
      */
    private static final Logger LOGGER = LoggerFactory.getLogger(Executor.class);

    private static final String FALLBACK_MATCHER_NAME = "default_matcher";

    private static Pattern timeRunning = Pattern.compile("MELT: Matcher finished within\\s*(\\d+)\\s*seconds");


    /**
     * This method runs the specified matcher on the specified track.
     * @param track Track on which the matcher shall be run.
     * @param matcher The matcher to be run.
     * @return An {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(Track track, IOntologyMatchingToolBridge matcher) {
        return run(track, matcher, getMatcherName(matcher));
    }

    /**
     * This method runs the specified matcher on the specified track.
     * @param track Track on which the matcher shall be run.
     * @param matcher The matcher to be run.
     * @param matcherName The name of the matcher that will be associated with an individual execution result.
     * @return An {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(Track track, IOntologyMatchingToolBridge matcher, String matcherName) {
        if(track == null){
            LOGGER.error("The track specified is null. Cannot execute the given matcher. " +
                    "Resolution: Will return null.");
            return null;
        }
        return run(track.getTestCases(), matcher, matcherName);
    }

    /**
     * Evaluate multiple matchers on the given track with this static method.
     * @param track The track to be evaluated.
     * @param matchers The matchers to be run on the given track.
     * @return ExecutionResultSet instance.
     */
    public static ExecutionResultSet run(Track track, Map<String, IOntologyMatchingToolBridge> matchers) {
        if(track == null){
            LOGGER.error("The track specified is null. Cannot execute the given matchers. " +
                    "Resolution: Will return null.");
            return null;
        }
        if(matchers == null){
            LOGGER.error("The matchers are not specified (map is null). Cannot execute the given matchers. " +
                    "Resolution: Will return null.");
            return null;
        }
        return run(track.getTestCases(), matchers);
    }

    /**
     * Run a matchers on multiple tracks.
     *
     * @param tracks The tracks on which the matchers shall be run.
     * @param matcher The matcher that shall be run.
     * @return The matching result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet runTracks(List<Track> tracks, IOntologyMatchingToolBridge matcher, String matcherName) {
        if(tracks == null){
            LOGGER.error("The tracks list is null. Resolution: Returning null.");
            return null;
        }
        if(matcher == null){
            LOGGER.error("The specified matcher is null. Resolution: Returning null.");
            return null;
        }
        ExecutionResultSet r = new ExecutionResultSet();
        for(Track track : tracks){
            for (TestCase tc : track.getTestCases()) {
                r.add(ExecutionRunner.runMatcher(tc, matcher, matcherName));
            }
        }
        return r;
    }

    /**
     * Run multiple matchers on multiple tracks.
     *
     * @param tracks The tracks on which the matchers shall be run.
     * @param matchers The matchers that shall be run.
     * @return The matching result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet runTracks(List<Track> tracks, Map<String, IOntologyMatchingToolBridge> matchers) {
        if(tracks == null){
            LOGGER.error("The tracks list is null. Resolution: Returning null.");
            return null;
        }
        if(matchers == null){
            LOGGER.error("The matchers are null. Resolution: Returning null.");
            return null;
        }
        ExecutionResultSet r = new ExecutionResultSet();
        for(Entry<String, IOntologyMatchingToolBridge> matcher : matchers.entrySet()){
            for(Track track : tracks){
                for (TestCase tc : track.getTestCases()) {
                    r.add(ExecutionRunner.runMatcher(tc, matcher.getValue(), matcher.getKey()));
                }
            }
        }        
        return r;
    }

    /**
     * Run a set of matchers on a set of test cases.
     *
     * @param testCases The test cases on which all the specified matchers shall be run.
     * @param matchers  A map of matchers from unique_name to matcher instance.
     * @return The matching result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(List<TestCase> testCases, Map<String, IOntologyMatchingToolBridge> matchers) {
        if(testCases == null){
            LOGGER.error("The testCases list is null. Resolution: Returning null.");
            return null;
        }
        if(matchers == null){
            LOGGER.error("The matchers are null. Resolution: Returning null.");
            return null;
        }
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
        if(matchers == null){
            LOGGER.error("The matchers are null. Resolution: Returning null.");
            return null;
        }
        if(testCase == null){
            LOGGER.error("The testCase is null. Resolution: Returning null.");
            return null;
        }
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
        if(testCases == null){
            LOGGER.error("The testCase list is null. Resolution: Returning null.");
            return null;
        }
        if(matcher == null){
            LOGGER.error("The matcher is null. Resolution: Returning null.");
            return null;
        }
        return run(testCases, matcher, getMatcherName(matcher));
    }

    /**
     * Run a matcher on a set of test cases.
     *
     * @param testCases The set of test cases on which the specified matcher shall be run.
     * @param matcher The matcher to be run.
     * @param matcherName The name of the matcher that will be associated with an individual execution result of the specified matcher.
     * @return An ExecutionResultSet instance.
     */
    public static ExecutionResultSet run(List<TestCase> testCases, IOntologyMatchingToolBridge matcher, String matcherName) {
        if(testCases == null){
            LOGGER.error("testCases list is null. Cannot execute matcher " + matcherName + " on the specified list of test cases." +
                    "Resolution: Return null.");
            return null;
        }
        ExecutionResultSet r = new ExecutionResultSet();
        for (TestCase tc : testCases) { // no parallelism possible because each matcher possibly writes to the same temp file.
            if(tc != null) {
                r.add(ExecutionRunner.runMatcher(tc, matcher, matcherName));
            } else {
                LOGGER.error("The testCases list contains a null object. Resolution: skipping...");
            }
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

    /**
     * Run a matcher on a test case.
     *
     * @param testCase The test case on which the matcher shall be executed.
     * @param matcher The matcher to be executed.
     * @param matcherName The name of the matcher that will be associated with an individual execution result of the specified matcher.
     * @return Result in the form of an {@link ExecutionResultSet}.
     */
    public static ExecutionResultSet run(TestCase testCase, IOntologyMatchingToolBridge matcher, String matcherName) {
        if(testCase == null){
            LOGGER.error("The testCase is null. Resolution: Returning null.");
            return null;
        }
        if(matcher == null){
            LOGGER.error("The matcher is null. Resolution: Returning null.");
            return null;
        }
        ExecutionResultSet r = new ExecutionResultSet();
        r.add(ExecutionRunner.runMatcher(testCase, matcher, matcherName));
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
                long runtime = tryToGetRuntime(new File(f.getParentFile(), f.getName().substring(0, f.getName().length() - 4) + "_log.txt"));
                try {
                    results.add(new ExecutionResult(testCase, FilenameUtils.removeExtension(f.getName()), f.toURI().toURL(), runtime, null));
                } catch (MalformedURLException ex) {
                    LOGGER.error("Cannot convert file URI to URL.", ex);
                }
            }
        }
        return results;
    }
    
    private static long tryToGetRuntime(File logFile){
        if(logFile.exists() == false)
            return 0;
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8))){
            String line;
            while ((line=reader.readLine()) != null) {
                Matcher m = timeRunning.matcher(line);
                if(m.find())
                    return Long.parseLong(m.group(1));
            }
        } catch (IOException ex) {
            LOGGER.error("Could not retive runtime. Return 0 as runtime.", ex);
            return 0;
        }
        return 0;
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
     * Load raw results from folder structure like:
     * folder
     * - testcase name
     *      - ALIGN.rdf
     *      - LogMap.rdf
     * File names are treated as matcher names and they are associated with the given testcase.
     *
     * @param folder   The folder where the system results can be found.
     * @param track    Track with which the individual system results shall be associated.
     * @return {@link ExecutionResultSet} instance with the loaded results.
     */
    public static ExecutionResultSet loadFromFolder(File folder, Track track) {
        if (!folder.isDirectory()) {
            LOGGER.error("The specified folder is not a directory. Returning null.");
            return null;
        }
        ExecutionResultSet results = new ExecutionResultSet();
        for (File f : folder.listFiles()) {
            if(f.isDirectory()){
                TestCase testcase = track.getTestCase(f.getName());
                if(testcase == null){
                    LOGGER.error("cannot read from folder {} because testcase doesn't exist in track {} .", f.getName(), track.getName());
                    continue;
                }
                results.addAll(loadFromFolder(f, testcase));                
            }
        }
        return results;
    }
    
    /**
     * Load raw results from folder structure like:
     * folder
     * - testcase name
     *      - ALIGN.rdf
     *      - LogMap.rdf
     * File names are treated as matcher names and they are associated with the given testcase.
     *
     * @param folder   The folder where the system results can be found.
     * @param track    Track with which the individual system results shall be associated.
     * @return {@link ExecutionResultSet} instance with the loaded results.
     */
    public static ExecutionResultSet loadFromFolder(String folder, Track track) {
        return loadFromFolder(new File(folder), track);
    }

    /**
     * Load raw results from a folder with the structure used by the <a href="http://oaei.ontologymatching.org/2018/results/anatomy/index.html">Anatomy Track</a>
     * of the OAEI.
     *
     * @return {@link ExecutionResultSet} instance with the loaded results.
     */
    public static ExecutionResultSet loadFromAnatomyResultsFolder(String pathToFolder) {
        return loadFromFolder(pathToFolder, TrackRepository.Anatomy.Default.getTestCases().get(0));
    }

    /**
     * Load raw results from a folder with the structure used by the <a href="http://oaei.ontologymatching.org/2018/results/conference/">Conference Track</a>
     * of the OAEI.
     *
     * @param pathToFolder The path to the folder where the alignments of the conference folder reside.
     * @return {@link ExecutionResultSet} instance with the loaded results.
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

    /**
     * Load raw results from a folder with the structure used by the
     * <a href="http://oaei.ontologymatching.org/2019/results/knowledgegraph/">Knowledge Graph Track</a>
     * of the OAEI.
     * @param pathToFolder The path to the unzipped folder where the folders of the knowledge graph track reside.
     * @return {@link ExecutionResultSet} instance with the loaded results.
     */
    public static ExecutionResultSet loadFromKnowledgeGraphResultsFolder(String pathToFolder){
        return loadFromFolder(pathToFolder, TrackRepository.Knowledgegraph.V3);
    }

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
    
    /**
     * Deletes all matcher result files which starts with "alignment" and are stored in the tmp folder.
     * Does not delete the whole tmp folder but onl files which are produced by oaei matchers.
     */
    public static void deleteTempFiles() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File[] toBeDeleted = tmpDir.listFiles((dir, name) -> {
            return name.startsWith("alignment");
        });        
        for(File f : toBeDeleted){
            f.delete();
        }
    }
}
