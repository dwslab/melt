package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Set;

/**
 * Result of the MergeTask
 */
public class MergeResult {
    
    private int newPos;
    private Set<Object> result;
    private Alignment alignment;

    public MergeResult(int newPos, Set<Object> result, Alignment alignment) {
        this.newPos = newPos;
        this.result = result;
        this.alignment = alignment;
    }

    public int getNewPos() {
        return newPos;
    }

    public Set<Object> getResult() {
        return result;
    }

    public Alignment getAlignment() {
        return alignment;
    }
}
