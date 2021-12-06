package de.uni_mannheim.informatik.dws.melt.matching_eval;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcherCaller;
import de.uni_mannheim.informatik.dws.melt.matching_data.LocalTrack;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;

import java.io.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

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

    private static final Pattern TIME_RUNNING = Pattern.compile("MELT: Matcher finished within\\s*(\\d+)\\s*seconds");
    
    /**
     * Run a single test case with one matcher.
     *
     * @param testCase    Test case to be evaluated.
     * @param matcher     Matcher to be evaluated.
     * @param matcherName The name of the matcher.
     * @return Single Execution Result
     */
    public static ExecutionResult runSingle(TestCase testCase, Object matcher, String matcherName) {
        if(!isMatcher(matcher)){
            LOGGER.warn("Class {} does not implement IMatcher or IOntologyMatchingToolBridge. Returning a null Execution result.", matcher.getClass().getSimpleName());
            return null;
        }
        return ExecutionRunner.runMatcher(testCase, matcher, matcherName);
    }

    /**
     * Run a single test case with one matcher.
     *
     * @param testCase Test case to be evaluated.
     * @param matcher  Matcher to be evaluated.
     * @return Single Execution Result
     */
    public static ExecutionResult runSingle(TestCase testCase, Object matcher) {
        return runSingle(testCase, matcher, getMatcherName(matcher));
    }
    
    /**
     * Run one matcher with a given name on multiple tracks.
     *
     * @param tracks The tracks on which the matchers shall be run.
     * @param matcher The matcher to be run. This class should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @param name the name of the matcher to use
     * @return The matching result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet runTracks(List<Track> tracks, Object matcher, String name) {
        return runTracks(tracks, getMatcherMapWithName(matcher, name));
    }
    
    /**
     * Run matchers on multiple tracks.
     *
     * @param tracks The tracks on which the matchers shall be run.
     * @param matchers The matcher to be run. This class should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @return The matching result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet runTracks(List<Track> tracks, Object... matchers) {
        return runTracks(tracks, getMatcherMap(matchers));
    }
    
    /**
     * Run multiple matchers on multiple tracks.
     *
     * @param tracks The tracks on which the matchers shall be run.
     * @param matchers The matchers that shall be run. The values in the map (matchers) should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @return The matching result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet runTracks(List<Track> tracks, Map<String, Object> matchers) {
        if(tracks == null){
            LOGGER.error("The tracks list is null. Resolution: Returning empty resultSet.");
            return new ExecutionResultSet();
        }
        List<TestCase> testCases = new ArrayList<>();
        for(Track track : tracks){
            if(track == null){
                LOGGER.warn("A track is null. It will be skipped.");
                continue;
            }
            testCases.addAll(track.getTestCases());
        }
        return run(testCases, matchers);
    }
    
    /**
     * This method runs the specified matcher on the specified track.
     * @param track Track on which the matcher shall be run.
     * @param matchers The matchers to be run. The classes should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @return An {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(Track track, Object... matchers) {
        return run(track, getMatcherMap(matchers));
    }
    
    /**
     * This method runs the specified matcher on the specified track.
     * @param track Track on which the matcher shall be run.
     * @param matcher The matcher to be run. The classes should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @param name the name of the matcher to use
     * @return An {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(Track track, Object matcher, String name) {
        return run(track, getMatcherMapWithName(matcher, name));
    }
    
    /**
     * Run matchers on one track.
     * @param track The track to be evaluated.
     * @param matchers The matchers to be run on the given track. The values of the map should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @return ExecutionResultSet instance.
     */
    public static ExecutionResultSet run(Track track, Map<String, Object> matchers) {
        if(track == null){
            LOGGER.error("The track specified is null. Cannot execute the given matchers. " +
                    "Resolution: Will return empty resultSet.");
            return new ExecutionResultSet();
        }
        return run(track.getTestCases(), matchers);
    }
    
    /**
     * Run matchers on one test cases.
     *
     * @param testCase One specific testcase on which all the specified matchers shall be run.
     * @param matchers  The matchers to be run. This class should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @return The result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(TestCase testCase, Object... matchers) {
        return run(Arrays.asList(testCase), getMatcherMap(matchers));
    }
    
    /**
     * Run matchers on one test cases.
     *
     * @param testCase One specific testcase on which all the specified matchers shall be run.
     * @param matcher  The matcher to be run. This class should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @param name the name of the matcher to use
     * @return The result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(TestCase testCase, Object matcher, String name) {
        return run(Arrays.asList(testCase), getMatcherMapWithName(matcher, name));
    }
    
    /**
     * Run matchers on one test cases.
     *
     * @param testCase One specific testcase on which all the specified matchers shall be run.
     * @param matchers  A map of matchers from unique_name to matcher instance.The value of the map should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @return The result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(TestCase testCase, Map<String, Object> matchers) {
        return run(Arrays.asList(testCase), matchers);
    }
    
    /**
     * Run matchers on a set of test cases.
     *
     * @param testCases The test cases on which all the specified matcher shall be run.
     * @param matchers   The matchers to be run. This class should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @return The result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(List<TestCase> testCases, Object... matchers) {
        return run(testCases, getMatcherMap(matchers));
    }
    
    /**
     * Run matchers on a set of test cases.
     *
     * @param testCases The test cases on which all the specified matcher shall be run.
     * @param matcher   The matcher to be run. This class should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @param name the name of the matcher to use
     * @return The result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(List<TestCase> testCases, Object matcher, String name) {
        return run(testCases, getMatcherMapWithName(matcher, name));
    }
    
    /**
     * Run matchers on a set of test cases.
     *
     * @param testCases The test cases on which all the specified matchers shall be run.
     * @param matchers  A map of matchers from unique_name to matcher instance. The matcher class should implement either IMatcher or IOntologyMatchingToolBridge is some way.
     * @return The matching result as {@link ExecutionResultSet} instance.
     */
    public static ExecutionResultSet run(List<TestCase> testCases, Map<String, Object> matchers) {
        if(testCases == null){
            LOGGER.error("The testCases list is null. Resolution: Returning empty resultSet.");
            return new ExecutionResultSet();
        }
        if(matchers == null){
            LOGGER.error("The matchers are null. Resolution: Returning empty resultSet.");
            return new ExecutionResultSet();
        }
        ExecutionResultSet r = new ExecutionResultSet();
        for (TestCase tc : testCases) {
            for (Entry<String, Object> matcher : matchers.entrySet()) {
                if(isMatcher(matcher.getValue()) == false){
                    LOGGER.warn("A matcher of class {} does not implement IMatcher or IOntologyMatchingToolBridge. "
                            + "This matcher will be skipped.");
                    continue;
                }
                ExecutionResult er = ExecutionRunner.runMatcher(tc, matcher.getValue(), matcher.getKey());
                if(er != null)
                    r.add(er);
            }
        }
        return r;
    }
    
    /*
    public static ExecutionResultSet runMatcherOnTop(ExecutionResultSet results, Map<String, Object> matchers) {
        
    }
    
    public static ExecutionResultSet runMatcherOnTop(ExecutionResult result, Object matcher, String name) {
        result
        ExecutionRunner.runMatcher(result.getTestCase(), matcher, name)
        
    }
    */

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
            LOGGER.error("The specified folder is not a directory. Returning empty resultSet.");
            return new ExecutionResultSet();
        }
        ExecutionResultSet results = new ExecutionResultSet();
        for (File f : folder.listFiles()) {
            if (f.isFile() && f.getName().endsWith(".rdf")) {
                long runtime = tryToGetRuntime(new File(f.getParentFile(), f.getName().substring(0, f.getName().length() - 4) + "_log.txt"));
                try {
                    results.add(new ExecutionResult(testCase, FilenameUtils.removeExtension(f.getName()), f.toURI().toURL(), runtime, null, null));
                } catch (MalformedURLException ex) {
                    LOGGER.error("Cannot convert file URI to URL.", ex);
                }
            }
        }
        return results;
    }
    
    private static long tryToGetRuntime(File logFile){
        if(!logFile.exists())
            return 0;
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8))){
            String line;
            while ((line=reader.readLine()) != null) {
                Matcher m = TIME_RUNNING.matcher(line);
                if(m.find())
                    return Long.parseLong(m.group(1));
            }
        } catch (IOException ex) {
            LOGGER.error("Could not retrieve runtime. Return 0 as runtime.", ex);
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
            LOGGER.error("The specified folder is not a directory. Returning empty resultSet.");
            return new ExecutionResultSet();
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
     * @param pathToFolder Path to the anatomy results folder (can be downloaded from OAEI Web site).
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
            LOGGER.error("The specified folder is not a directory. Returning empty resultSet.");
            return new ExecutionResultSet();
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
                    results.add(new ExecutionResult(testCase,matcherName, f.toURI().toURL(), 0, null, null));
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
     * Load results that are produced by the MELT evaluator {@link de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV}
     * in the results folder.
     *
     * @param folderPath Path to the results folder (the one with the time code e.g. results/results_2020-03-02_08-47-40)
     * @return {@link ExecutionResultSet} instance with the loaded results.
     */
    public static ExecutionResultSet loadFromEvaluatorCsvResultsFolder(String folderPath) {
        return loadFromEvaluatorCsvResultsFolder(new File(folderPath), new HashSet<>());
    }

    /**
     * Load results that are produced by the MELT evaluator {@link de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV}
     * in the results folder.
     *
     * @param folder Path to the results folder (the one with the time code e.g. results/results_2020-03-02_08-47-40)
     * @return {@link ExecutionResultSet} instance with the loaded results.
     */
    public static ExecutionResultSet loadFromEvaluatorCsvResultsFolder(File folder){
        return loadFromEvaluatorCsvResultsFolder(folder, new HashSet<>());
    }

    public static ExecutionResultSet loadFromEvaluatorCsvResultsFolder(String folderPath, LocalTrack localTrack){
        return loadFromEvaluatorCsvResultsFolder(new File(folderPath), localTrack);
    }

    public static ExecutionResultSet loadFromEvaluatorCsvResultsFolder(File folder, LocalTrack localTrack){
        HashSet<LocalTrack> localTracks = new HashSet<>();
        localTracks.add(localTrack);
        return loadFromEvaluatorCsvResultsFolder(folder, localTracks);
    }

    /**
     * Load results that are produced by the MELT evaluator {@link de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV}
     * in the results folder.
     *
     * @param folder Path to the results folder (the one with the time code e.g. results/results_2020-03-02_08-47-40)
     * @param localTracks A set of local tracks contained in the evaluation directory.
     * @return {@link ExecutionResultSet} instance with the loaded results.
     */
    public static ExecutionResultSet loadFromEvaluatorCsvResultsFolder(File folder, Set<LocalTrack> localTracks) {
        if(folder == null){
            LOGGER.error("The specified folder is null. Returning empty ResultSet.");
            return new ExecutionResultSet();
        }
        if (!folder.isDirectory()) {
            LOGGER.error("The specified folder is not a directory. Returning empty ResultSet.");
            return new ExecutionResultSet();
        }
        ExecutionResultSet results = new ExecutionResultSet();

        Map<String, Track> trackNameVersionToTrackMap = TrackRepository.getMapFromTrackNameAndVersionToTrack();
        if(localTracks != null){
            for(LocalTrack track : localTracks){
                try {
                    trackNameVersionToTrackMap.put(URLEncoder.encode(track.getName(), "UTF-8") + "_" + URLEncoder.encode(track.getVersion(), "UTF-8"), track);
                } catch (UnsupportedEncodingException e) {
                    LOGGER.error("Encoding problem with local track '" + track.getName() + "'", e);
                }
            }
        }

        for (File trackFolder : folder.listFiles()) {
            if(trackFolder.isDirectory()){
                Track track = trackNameVersionToTrackMap.get(trackFolder.getName());

                if(track == null){
                    LOGGER.error("cannot read from folder {} because track doesn't exist.", trackFolder.getName());
                    continue;
                }
                for (File testCaseFolder : trackFolder.listFiles()) {
                    if(testCaseFolder.getName().equals("aggregated"))
                        continue;
                    TestCase testcase = track.getTestCase(testCaseFolder.getName());
                    if(testcase == null){
                        LOGGER.error("cannot read from folder {} because testcase doesn't exist in track {} .", testCaseFolder.getName(), track.getName());
                        continue;
                    }
                    for (File matcherFolder : testCaseFolder.listFiles()) {
                        File alignmentFile = new File(matcherFolder, "systemAlignment.rdf");
                        if(alignmentFile.exists() == false){
                            LOGGER.error("alignment file (systemAlignment.rdf) is missing in folder {}", matcherFolder.getAbsolutePath());
                            continue;
                        }
                        File performanceFile = new File(matcherFolder, "performance.csv");
                        try {
                            results.add(new ExecutionResult(testcase, matcherFolder.getName(), alignmentFile.toURI().toURL(), getTimeFromPerformanceCSV(performanceFile), null, null));
                        } catch (MalformedURLException ex) {
                            LOGGER.error("Could not build URL for file " + alignmentFile.getName());
                        }
                    }
                }
            }
        }
        return results;
    }
    
    private static long getTimeFromPerformanceCSV(File performanceCSV){
        if(performanceCSV.exists() == false){
            LOGGER.warn("Could not extract runtime from performance.csv because it is not existent: ", performanceCSV.getPath());
            return 0;
        }
        try(Reader in = new InputStreamReader(new FileInputStream(performanceCSV), "UTF-8")){
            for (CSVRecord row : CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)) {
                try{
                    return Long.parseLong(row.get("Time"));
                }
                catch(NumberFormatException ex){} // do nothing -> continue
            }
        } catch (IOException ex) {
            LOGGER.warn("Could not extract runtime from file {}", performanceCSV.getPath(), ex);
        }
        return 0;
    }
    
    private static Map<String, Object> getMatcherMapWithName(Object matcher, String name){
        Map<String, Object> resultingMatchers = new HashMap<>();
        resultingMatchers.put(name, matcher);
        return resultingMatchers;
    }
    
    private static Map<String, Object> getMatcherMap(Object... matchers){
        Map<String, Object> resultingMatchers = new HashMap<>();
        for(Object matcher : matchers){
            if(isMatcher(matcher)){
                String matcherName = getNonExistentName(getMatcherName(matcher), resultingMatchers.keySet());
                resultingMatchers.put(matcherName, matcher);
            }else if(matcher instanceof Map){
                Map<?,?> matcherMap = (Map) matcher;
                //keys:
                boolean keyStringValueMatcherForAll = true;
                for(Entry<?,?> entry : matcherMap.entrySet()){
                    if(entry.getKey() instanceof String == false || isMatcher(entry.getValue()) == false){
                        keyStringValueMatcherForAll = false;
                        break;
                    }
                }
                if(keyStringValueMatcherForAll == false){
                    LOGGER.warn("MELT detected that a Map is provided as a matcher but not all keys are strings or not all values implements "
                            + "the interfaces IMatcher or IOntologyMatchingToolBridge. The whole Map is skipped.");
                    continue;
                }
                for(Entry<?,?> entry : matcherMap.entrySet()){
                    String matcherName = getNonExistentName((String)entry.getKey(), resultingMatchers.keySet());
                    resultingMatchers.put(matcherName, entry.getValue());
                }
            }else if(matcher instanceof Iterable){
                Iterable<?> matcherIterable = (Iterable) matcher;
                //check that all elements are matchers:
                boolean allMatchers = true;
                for(Object o : matcherIterable){
                    if(isMatcher(o) == false){
                        allMatchers = false;
                        break;
                    }
                }
                if(allMatchers == false){
                    LOGGER.warn("MELT detected that a list is provided as a matcher but not all elements implements "
                            + "the interfaces IMatcher or IOntologyMatchingToolBridge. The whole list is skipped.");
                    continue;
                }
                for(Object o : matcherIterable){
                    String matcherName = getNonExistentName(getMatcherName(o), resultingMatchers.keySet());
                    resultingMatchers.put(matcherName, matcher);
                }
            }else{
                LOGGER.warn("A matcher of class {} does not implement the interfaces IMatcher or IOntologyMatchingToolBridge. "
                        + "This matcher will be skipped.", matcher.getClass());
            }
        }
        return resultingMatchers;
    }
    
    private static boolean isMatcher(Object o){
        return o instanceof IMatcherCaller || 
                o instanceof IMatcher ||
                o instanceof IOntologyMatchingToolBridge;
    }
    
    private static String getNonExistentName(String matcherName, Set<String> alreadyUsedMatcherNames){
        if(alreadyUsedMatcherNames.contains(matcherName) == false){
            return matcherName;
        }
        for(int i = 1; i < 200; i++){
            String newMatcherName = matcherName + "_" + i;
            if(alreadyUsedMatcherNames.contains(newMatcherName) == false){
                return newMatcherName;
            }
        }
        LOGGER.error("Cannot create new matcher name because suffix number is greater than 200. This is not usual. "
                + "The initial name is returned. This means that not all matcher will be executed.");
        return matcherName;
    }
    
    /**
     * Given a matcher, this method returns its name.
     * It will first check if there is a specific toString method (e.g. not defined by Object class.
     * If this is not the case then, it will return the simple name of the class.
     * If this also does not exist(e.g. lamda definition), then it will return a fallback matcher name.
     * @param matcherInstance the matcher instance whose name shall be retrieved.
     * @return The name as String.
     */
    public static String getMatcherName(Object matcherInstance) {
        //https://stackoverflow.com/questions/22866925/detect-if-object-has-overriden-tostring
        Class<?> matcherClass = matcherInstance.getClass();
        try {
            if (matcherClass.getMethod("toString").getDeclaringClass() != Object.class) {
                return matcherInstance.toString();
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            LOGGER.debug("No access to toString method of matcher.", ex);
        }
        return getMatcherName(matcherClass);
    }
    
    /**
     * Given a matcher, this method returns its name.
     * It will first check if there is a specific toString method (e.g. not defined by Object class.
     * If this is not the case then, it will return the simple name of the class.
     * If this also does not exist(e.g. lamda definition), then it will return a fallback matcher name.
     * @param matcherClass the matcher class whose name shall be retrieved.
     * @return The name as String.
     */
    public static String getMatcherName(Class<?> matcherClass) {
        String name = matcherClass.getSimpleName();
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
    
    public static long getSummedRuntimeOfAllUnrefinedResults(ExecutionResultSet results){
        long summedRuntime = 0;
        for(ExecutionResult r : results.getUnrefinedResults()){
            summedRuntime += r.getRuntime();
        }
        return summedRuntime;
    }
    
    
    /*****************************************
     * Run On Top
     *****************************************/
    
    /**
     * Runs a matcher on top of another. This means that the previous matchings do not need to be recalculated.
     * @param oldResults the results from the previous runs already containing result from the oldMatcherName.
     * @param oldMatcherName the matcher name which should exist in previous results
     * @param newMatcherName the new matcher name
     * @param matcher the actual matcher
     * @return the execution results together with the new matcher
     */
    public static ExecutionResultSet runMatcherOnTop(ExecutionResultSet oldResults, String oldMatcherName, Object matcher, String newMatcherName){
        return runMatcherOnTop(oldResults, oldMatcherName, getMatcherMapWithName(matcher, newMatcherName));
    }
    
    public static ExecutionResultSet runMatcherOnTop(ExecutionResultSet oldResults, String oldMatcherName, Map<String, Object> newMatchers){
        ExecutionResultSet newResultSet = new ExecutionResultSet();
        for(ExecutionResult oldResult : oldResults.getGroup(oldMatcherName)){
            for(Entry<String, Object> newMatcher : newMatchers.entrySet()){
                ExecutionResult newResult = ExecutionRunner.runMatcher(
                    oldResult.getTestCase(),
                    newMatcher.getValue(),
                    newMatcher.getKey(),
                    new Alignment(oldResult.getSystemAlignment()),
                    oldResult.getParameters()
                );
                newResult.addRuntime(oldResult.getRuntime());
                newResultSet.add(newResult);
            }
        }
        oldResults.addAll(newResultSet);
        return oldResults;
    }
}
