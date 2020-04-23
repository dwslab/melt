package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction;

/**
 * Initialization heuristic for {@link MaxWeightBipartiteExtractor}.
 */
public enum MwbInitHeuristic {
    /**
     *Native heuristic: set potential of all nodes in A to the maximum confidence.
     */
    NAIVE,
    /**
     * Simple heuristic: set potential of a node a in the source to the maximum confidence of the adjacent edges of a.
     */
    SIMPLE,
    /**
     * Refined heuristic - not implemented yet.
     */
    REFINED
}
