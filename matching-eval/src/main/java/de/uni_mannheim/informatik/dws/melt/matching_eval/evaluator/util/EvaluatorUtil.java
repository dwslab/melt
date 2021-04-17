package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.util;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCopyResults;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class offering multiple services to evaluators (building blocks for quick evaluator development).
 * @author Sven Hertling, Jan Portisch
 */
public class EvaluatorUtil {


    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorCopyResults.class);

    /**
     * Writes the system alignment to the specified file.
     * @param executionResult The execution result whose system alignment shall be written to the specified alignmentFile.
     * @param alignmentFileToBeWritten File that shall be written.
     */
    public static void copySystemAlignment(ExecutionResult executionResult, File alignmentFileToBeWritten){
        try {
            if(executionResult.getOriginalSystemAlignment() != null){
                FileUtils.copyURLToFile(executionResult.getOriginalSystemAlignment(), alignmentFileToBeWritten);
            }else{
                executionResult.getSystemAlignment().serialize(alignmentFileToBeWritten);
            }
        } catch (IOException ex) {
            LOGGER.error("Couldn't copy the results from the matcher to the results directory.", ex);
        }
    }
}
