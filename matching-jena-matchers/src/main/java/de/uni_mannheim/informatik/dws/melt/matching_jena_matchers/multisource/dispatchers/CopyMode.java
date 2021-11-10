package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

/**
 * Defines different modes how a model should be copied during incremental merge.
 */
public enum CopyMode {
    /**
     * Do not make any copy but use the models which are provided.
     * Do not use it if the initial models are stired in TDB, because those would be modifed.
     */
    NONE,
    
    /**
     * Creates a new TDB storage for the merged KGs.
     */
    CREATE_TDB,
    
    /**
     * Copy the model in memory. This will not modify the provided models, 
     * but will require a lot of memory.
     */
    COPY_IN_MEMORY
}
