package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

/**
 * The enum which represents the possible measure for evaluating a model during hyperparameter search.
 */
public enum TransformersOptimizingMetric {
    
    /**
     * The loss of the model is optimized.
     */
    LOSS,
    
    /**
     * The accuracy is optimized.
     * This is not a differentiable measure. Be careful - the hp search will not run optimally.
     */
    ACCURACY,
    
    /**
     * The f-measure is optimized.
     * This is not a differentiable measure. Be careful - the hp search will not run optimally.
     */
    F1,
    
    /**
     * The f-measure is optimized.
     * This is not a differentiable measure. Be careful - the hp search will not run optimally.
     */
    RECALL,
    
    /**
     * The precision is optimized.
     * This is not a differentiable measure. Be careful - the hp search will not run optimally.
     */
    PRECISION,
    
    /**
     * The area under curve (AUC)will be optimized.
     * This means the "confindence" value will be optimized.
     */
    AUC,
    
    /**
     * The sum of AUC and F1 will be optimized.
     */
    AUCF1,
    
    /**
     * Log loss.
     */
    LOGLOSS;
    
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
