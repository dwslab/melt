package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge;

/**
 * The clustering likage.
 * For documentation of each value, see subclasses of {@link smile.clustering.linkage.Linkage}.
 */
public enum ClusterLinkage {    
    /**
     * Complete linkage. Distance between clusters is defined as the distance between the most distant pair of examples.
     */
    COMPLETE,
    /**
     * Single linkage. The distance between clusters is defined as the distance between the closest pair of examples.
     */
    SINGLE,
    /**
     * Average linkage (also called  {@link smile.clustering.linkage.UPGMALinkage}).
     * The distance between two clusters is the mean distance of all possible pairs of examples. 
     */
    AVERAGE,
    /**
     * Ward's linkage. {@link smile.clustering.linkage.WardLinkage}
     */
    WARD,
    /**
     * Centroid linkage (also called  {@link smile.clustering.linkage.UPGMCLinkage}).
     * The distance between two clusters is the Euclidean distance between their centroids.
     */
    CENTROID,
    /**
     * Median linkage (also called  {@link smile.clustering.linkage.WPGMCLinkage} - Weighted Pair Group Method using Centroids).
     * The distance between two clusters is the Euclidean distance between their weighted centroids.
     */
    MEDIAN,
    /**
     * WPGMA linkage (Weighted Pair Group Method with Arithmetic mean).
     * {@link smile.clustering.linkage.WPGMALinkage}).
     */
    WPGMA;
}
