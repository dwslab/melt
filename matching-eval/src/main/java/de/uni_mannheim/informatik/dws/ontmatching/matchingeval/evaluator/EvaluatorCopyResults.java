package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;

import java.io.File;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util.EvaluatorUtil;
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
    public void write(File baseDirectory) {
        for (ExecutionResult r : this.results) {
            EvaluatorUtil.copySystemAlignment(r, new File(getResultsFolderTrackTestcaseMatcher(baseDirectory, r), "alignment.rdf"));
        }
    }

}
