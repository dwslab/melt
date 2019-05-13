package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Paths;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for all default evaluators.
 * @author Sven Hertling
 * @author Jan Portisch
 */
public abstract class Evaluator {

    /**
     * Default Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);

    /**
     *  The directory where the results shall be written to.
     */
    private static File resultsDirectory;
    static {
        setDefaultResultsDirectory(new File("results"));
    }

    /**
     * Set the defaults results directory.
     * @param directory The directory where the results shall be written to.
     */
    public static void setDefaultResultsDirectory(File directory){
        if (directory == null) {
            throw new IllegalArgumentException("DefaultResultsFolder in class Evaluator should not be null");
        }
        if(!directory.exists()) {
            directory.mkdirs();
        }
        if(directory.isDirectory() == false){
            throw new IllegalArgumentException("DefaultResultsFolder should be a directory.");
        }
        resultsDirectory = directory;
    }

    /**
     * Set of the individual instances of {@link ExecutionResult} for which an evaluation will be performed.
     */
    protected ExecutionResultSet results;

    /**
     * Constructor.
     * @param results The results of the matching process.
     */
    public Evaluator(ExecutionResultSet results){
        this.results = results;
    }

    /**
     * Persist the results of the evaluator in the default directory.
     */
    public void write(){
        this.write(resultsDirectory);
    }

    /**
     * Persist the results of the evaluator in the base directory.
     * @param baseDirectory The directory into which the evaluation results shall be written to.
     */
    public abstract void write(File baseDirectory);

    /**
     * Given a base directory and a Execution result, the target directory will be returned to which results can be
     * persisted.
     * @param baseDirectory Base directory for evaluation results.
     * @param executionResult Execution result whose evaluation shall be persisted.
     * @return Directory where evaluation result can be persisted.
     */
    protected File getResultsFolderTrackTestcaseMatcher(File baseDirectory, ExecutionResult executionResult){
        try {
            return Paths.get(baseDirectory.getAbsolutePath(),
                            URLEncoder.encode(executionResult.getTestCase().getTrack().getName(), "UTF-8") + "_" + URLEncoder.encode(executionResult.getTestCase().getTrack().getVersion(), "UTF-8"),
                            URLEncoder.encode(executionResult.getTestCase().getName(), "UTF-8"),
                            URLEncoder.encode(executionResult.getMatcherName(), "UTF-8")).toFile();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Could not crreate results folder", ex);
            return Paths.get(baseDirectory.getAbsolutePath()).toFile();
        }
    }

    /**
     * Given a base directory and a test case, the target directory will be returned for aggregated results.
     * @param baseDirectory Base directory for evaluation results.
     * @param track Track
     * @return Directory where aggregated evaluation results can be persisted.
     */
    protected File getResultsDirectoryTrackMatcher(File baseDirectory, Track track){
        try {
            return Paths.get(baseDirectory.getAbsolutePath(),
                    URLEncoder.encode(track.getName(), "UTF-8") + "_" + URLEncoder.encode(track.getVersion(), "UTF-8") + "/aggregated")
                    .toFile();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Could not crreate results folder", ex);
            return Paths.get(baseDirectory.getAbsolutePath()).toFile();
        }
    }
}
