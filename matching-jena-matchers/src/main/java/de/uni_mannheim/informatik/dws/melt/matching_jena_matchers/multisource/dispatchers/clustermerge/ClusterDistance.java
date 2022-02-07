package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge;

/**
 * The distance measure to use for clustering.
 * A good default is to use euclidean.
 */
public enum ClusterDistance {
    
    /**
     * Euclidean distance for double arrays.
     */
    EUCLIDEAN,
    
    /**
     * Squared Euclidean distance. This results in the same rankings as regular Euclidean distance, 
     * but does not compute the square root (thus thus is a bit faster).
     */
    SQUARED_EUCLIDEAN,
    
    /**
     * Manhattan distance for double arrays.
     */
    MANHATTAN,
}
