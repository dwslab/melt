package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import java.util.Properties;

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
    
    public <T> T getAlignment(Class<T> type) {
        return TypeTransformerRegistry.getTransformedObjectOrNewInstance(alignment, type);
    }
    
    public <T> T getAlignment(Class<T> type, Properties p) {
        return TypeTransformerRegistry.getTransformedObjectOrNewInstance(alignment, type, p);
    }

    public Object getParameters() {
        return parameters;
    }
    
    public <T> T getParameters(Class<T> type) {
        return TypeTransformerRegistry.getTransformedObjectOrNewInstance(parameters, type);
    }
    
    public <T> T getParameters(Class<T> type, Properties p) {
        return TypeTransformerRegistry.getTransformedObjectOrNewInstance(parameters, type, p);
    }
    
    public Properties getParametersAsProperties() {
        return TypeTransformerRegistry.getTransformedObjectOrNewInstance(parameters, Properties.class);
    }
}
