package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.instancelevel;

/**
 * Similarity Metric which can be used for MatchClassBasedOnInstances
 */
public enum SimInstanceMetric {
    /**
     * matches two classes c1 and c2 if they share at least one already matches instance.
     * Metric value is set to 1.
     */
    BASE,
    /**
     * Value is computed by 2 x overlapping instances / ( instances of class 1 + instances of class 2).
     * It determines the instance overlap w.r.t. to both concepts.
     */
    DICE,
    /**
     * Value is computed by overlapping instances / min(instances of class 1,  instances of class 2).
     * It determines the relative instance overlap with respect to the smaller-sized concept.
     */
    MIN, 
    /**
     * Value is computed by overlapping instances / number of matched instances.
     * This value also takes into account if the matcher does not match many instances.
     */
    MATCH_BASED
}
