package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MergeOrder;

/**
 * An interface to choose between different implementations of clusterers like SMILE library or ELKI.
 */
public interface Clusterer {
    
    public MergeOrder run(double[][] features, ClusterLinkage linkage, ClusterDistance distance);
    
}
