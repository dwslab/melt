package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.Evaluator;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for all multisource evaluators.
 * The default results folder can be set via {@link Evaluator}.
 */
public abstract class EvaluatorMultiSource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorMultiSource.class);
    
    protected ExecutionResultSetMultiSource results;
    
    /**
     * Constructor.
     * @param results The results of the matching process.
     */
    public EvaluatorMultiSource(ExecutionResultSetMultiSource results){
        this.results = results;
    }

    /**
     * Perform an evaluation and persist the results of the evaluator in the default directory.
     */
    public void writeToDirectory(){
        this.writeToDirectory(Evaluator.getDirectoryWithCurrentTime());
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
        if(directory.isDirectory() == false){
            throw new IllegalArgumentException("Could not write evaluator results to baseDirectory because it is not a directory.");
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
