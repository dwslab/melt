package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

/**
 * An enum which describes entities on how to optimize the batch size.
 */
public enum BatchSizeOptimization {
    
    USE_LONGEST_TEXTS,
    
    USE_MAX_WORDS,
    
    /**
     * Use leroem ipsum text to find the highest batch size.
     */
    USE_THEORETICAL_MAX,
    
    /**
     * No batch size optimization.
     */
    NONE
}
