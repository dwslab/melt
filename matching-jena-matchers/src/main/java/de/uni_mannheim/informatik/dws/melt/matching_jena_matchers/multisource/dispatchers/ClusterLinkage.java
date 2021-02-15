package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

/**
 * The clustering likage.
 * For documentation of each value, see subclasses of {@link smile.clustering.linkage.Linkage}.
 */
public enum ClusterLinkage {    
    COMPLETE,
    SINGLE,
    AVERAGE,
    WARD,
    CENTROID,
    MEDIAN,
    WPGMA;
}
