package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

/**
 * Due to the fact that a matcher can only return one value, but alignment and parameters can be changed,
 * an extra object is necessary.
 * If you don't care about the updated parameters you can just return the alignment.
 */
public class AlignmentAndParameters {
    private Object alignment;
    private Object parameters;

    public AlignmentAndParameters(Object alignment, Object parameters) {
        this.alignment = alignment;
        this.parameters = parameters;
    }

    public Object getAlignment() {
        return alignment;
    }

    public Object getParameters() {
        return parameters;
    }
}
