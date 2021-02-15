package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.clustering;

/**
 * Algorithm for modularity optimization used in {@link ComputeErrDegree}
 */
public enum ModularityAlgorithm {
    /**
     * original Louvain algorithm
     */
    LOUVRAIN,
    
    /**
     * Louvain algorithm with multilevel refinement
     */
    LOUVRAIN_MULTILEVEL,
    
    /**
     * SLM algorithm
     */
    SLM
    
}
