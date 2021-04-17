package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.util.EvaluatorUtil;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This evaluator simply writes the system alignments of individual {@link ExecutionResult} instances to a file in the
 * results folder.
 *
 * @author Sven Hertling
 */
public class EvaluatorCopyResults extends Evaluator {


    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorCopyResults.class);

    public EvaluatorCopyResults(ExecutionResultSet results) {
        super(results);
    }

    @Override
    public void writeResultsToDirectory(File baseDirectory) {
        for (ExecutionResult r : this.results) {
            EvaluatorUtil.copySystemAlignment(r, new File(getResultsFolderTrackTestcaseMatcher(baseDirectory, r), "systemAlignment.rdf"));
        }
    }
}
