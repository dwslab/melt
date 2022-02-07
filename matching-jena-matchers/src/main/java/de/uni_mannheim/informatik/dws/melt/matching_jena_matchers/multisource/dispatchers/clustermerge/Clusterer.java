package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge;

/**
 * An interface to choose between different implementations of clusterers like SMILE library or ELKI.
 */
public interface Clusterer {
    
    public ClusterResult run(double[][] features, ClusterLinkage linkage, ClusterDistance distance);
    
}
