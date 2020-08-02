package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel.hierarchical.agony.Agony;

/**
 * Enum for choosing the approach in {@link SimilarHierarchyFilter}.
 */
public enum SimilarHierarchyFilterApproach {
    
    /**
     * Absolute matches in the hierarchy independent where it appears in the hierarchy.
     */
    ABSOLUTE_MATCHES,
    /**
     * Average of the lowest match in both hierarchies (depth of match in the hierarchy / maximum depth of the hierarchy).
     * The lower position in the hierarchy, the higher the confidence.
     * Computed by simple breath first search. Fast, but may not yield good results.
     */
    DEPTH_DEPENDEND_MATCHES ,
    /**
     * Average of the lowest match in both hierarchies:
     * The lower position in the hierarchy, the higher the confidence.
     * Computed by the {@link Agony} class. May yield better results than DEPTH_DEPENDEND_MATCHES but is slower.
     */
    HIERARCHY_LEVEL_DEPENDED_MATCHES;
    
}
