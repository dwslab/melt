package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher;

/**
 * Strategies that are supported by {@link BackgroundMatcher}.
 */
public enum ImplementedStrategies {


    /**
     * Enforce that concepts have to be explicitly mentioned as synonymous in the background knowledge source.
     */
    SYNONYMY,

    /**
     * Enforce that concepts have to be mentioned as synonyms or have a level-1 hypernymy relation.
     */
    SYNONYMY_OR_HYPERNYMY,

    /**
     * Specify that the overlap of homonyms determines whether two concepts can be regarded as equivalent or not.
     */
    OVERLAP;

}
