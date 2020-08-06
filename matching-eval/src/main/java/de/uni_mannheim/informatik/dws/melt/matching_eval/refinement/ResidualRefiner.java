package de.uni_mannheim.informatik.dws.melt.matching_eval.refinement;

import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * This refiner is capable of refining an {@link ExecutionResult} according to trivial and nontrivial matches.
 * Trivial matches are removed from the system and reference alignment.
 *
 * @author Jan Portisch
 */
public class ResidualRefiner implements Refiner {

    /**
     * The baseline execution results.
     */
    private ExecutionResult baseline;

    /**
     * Baseline Matcher that is used to calculate the {@link ResidualRefiner#baseline}.
     */
    private IOntologyMatchingToolBridge baselineMatcher;

    /**
     * Default Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(ResidualRefiner.class);

    /**
     * Constructor.
     * @param baselineExecutionResult The baseline that shall be used to determine the residuals.
     */
    public ResidualRefiner(ExecutionResult baselineExecutionResult){
        this.baseline = baselineExecutionResult;
        this.baselineMatcher = baselineExecutionResult.getMatcher();
    }

    /**
     * Constructor
     * @param baselineMatcher The baseline matcher that shall be used to determine the residuals.
     */
    public ResidualRefiner(IOntologyMatchingToolBridge baselineMatcher){
        this.baseline = null;
        this.baselineMatcher = baselineMatcher;
    }


    @Override
    public ExecutionResult refine(ExecutionResult toBeRefined) {
        ExecutionResult usedBaseline;
        if(this.baseline != null){
            usedBaseline = this.baseline;
        } else {
            usedBaseline = Executor.runSingle(toBeRefined.getTestCase(), baselineMatcher, "baseLineMatcher");
        }

        // new reference alignment: old - trivial matches
        Alignment nonTrivialReferenceAlignment = new Alignment(toBeRefined.getReferenceAlignment());

        // note: it does not matter whether the baseline is correct in this case b/c the reference alignment contains only correct mappings
        nonTrivialReferenceAlignment.removeAll(usedBaseline.getSystemAlignment());

        // to be consistent: new system alignment: old - trivial matches
        Alignment nonTrivialSystemAlignment = new Alignment(toBeRefined.getSystemAlignment());
        Alignment correctTrivialAlignments = new Alignment(usedBaseline.getSystemAlignment());

        // retain all: intersection between system and reference
        correctTrivialAlignments.retainAll(toBeRefined.getReferenceAlignment());
        nonTrivialSystemAlignment.removeAll(correctTrivialAlignments);

        // change original system execution result
        return new ExecutionResult(toBeRefined, nonTrivialSystemAlignment, nonTrivialReferenceAlignment, this);
    }


    @Override
    public int hashCode() {
        int hash = 8;
        hash = 71 * hash + Objects.hashCode(this.baselineMatcher);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResidualRefiner other = (ResidualRefiner) obj;
        if(other.baselineMatcher != this.baselineMatcher){
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ResidualRefiner()";
    }

    
    
}
