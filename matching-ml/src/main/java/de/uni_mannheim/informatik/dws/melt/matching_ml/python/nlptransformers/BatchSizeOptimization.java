package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

/**
 * An enum which describes entities on how to optimize the batch size.
 */
public enum BatchSizeOptimization {
    
    /**
     * Use the longest texts in the dataset to determine the highest batch size.
     */
    USE_LONGEST_TEXTS,
    
    /**
     * Use the longest texts in the dataset to determine the highest batch size but reduce the may batch size two times.
     */
    USE_LONGEST_TEXTS_PESSIMISTIC,
    
    /**
     * Use the texts with the highest number of words to determine the highest batch size.
     */
    USE_MAX_WORDS,
    
    /**
     * Use a long lorem ipsum text to determine the highest batch size.
     */
    USE_THEORETICAL_MAX,
    
    /**
     * No batch size optimization.
     */
    NONE
}
