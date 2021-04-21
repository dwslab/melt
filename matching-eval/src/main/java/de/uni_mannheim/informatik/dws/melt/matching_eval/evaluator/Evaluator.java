package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Paths;

import java.text.SimpleDateFormat;
import java.util.Date;

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
        resultsDirectory = directory;
    }
    
    /**
     * Returns the default results directory.
     * @return the default results directory
     */
    public static File getDefaultResultsDirectory(){
        return resultsDirectory;
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
     * Perform an evaluation and persist the results of the evaluator in the default directory.
     */
    public void writeToDirectory(){
        this.writeToDirectory(getDirectoryWithCurrentTime());
    }
    
    public static File getDirectoryWithCurrentTime(){
        String dateAndTime = "results_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        return new File(resultsDirectory, dateAndTime);
    }

    /**
     * Perform an evaluation and persist the results of the evaluator in the given directory.
     * @param baseDirectory The directory which shall be used to place the evaluation files. 
     *                      The directory will be created if it does not exist.
     */
    public void writeToDirectory(File baseDirectory){
        checkAndCreateDirectory(baseDirectory);
        writeResultsToDirectory(baseDirectory);
    }
    
    protected abstract void writeResultsToDirectory(File baseDirectory);

    /**
     * Perform an evaluation and persist the results of the evaluator in the default directory.
     * @param baseDirectoryPath The directory path which shall be used to place the evaluation files. 
     *                          The directory will be created if it does not exist.
     */
    public void writeToDirectory(String baseDirectoryPath){
        writeToDirectory(new File(baseDirectoryPath));
    }
    
    /**
     * Checks if the directory is not null, creates it and check if it is a directory.
     * Throws an exception if something is not correct.
     * @param directory the directory to inspect
     */
    protected void checkAndCreateDirectory(File directory){
        if (directory == null) {
            throw new IllegalArgumentException("Could not write evaluator results to baseDirectory because it is null.");
        }
        if(!directory.exists()) {
            directory.mkdirs();
        }
        if(!directory.isDirectory()){
            throw new IllegalArgumentException("Could not write evaluator results to baseDirectory because it is not a directory.");
        }
    }

    /**
     * Given a base directory and an {@link ExecutionResult}, the target directory will be returned to which results can
     * be persisted.
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
            LOGGER.error("Could not create results folder", ex);
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

    public ExecutionResultSet getResults() {
        return results;
    }
}
