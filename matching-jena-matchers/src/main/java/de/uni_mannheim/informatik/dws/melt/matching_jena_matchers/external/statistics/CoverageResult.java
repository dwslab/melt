package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.statistics;


import java.util.Map;
import java.util.Set;

/**
 * A CoverageResult is immutable.
 */
public class CoverageResult {


    /**
     * Constructor
     * @param coverageScore The coverage score.
     * @param linkedConcepts The linked concepts.
     * @param nonLinkedConcepts The concepts not linked.
     */
    public CoverageResult(float coverageScore, Map<String, Set<String>>  linkedConcepts, Set<String> nonLinkedConcepts){
        this.coverageScore = coverageScore;
        this.linkedConcepts = linkedConcepts;
        this.nonLinkedConcepts = nonLinkedConcepts;
    }

    /**
     * The share of linked strings.
     */
    private final float coverageScore;

    /**
     * The concepts that were linked
     */
    private final Map<String, Set<String>> linkedConcepts;

    /**
     * The concepts that were not linked.
     */
    private final Set<String> nonLinkedConcepts;

    public float getCoverageScore() {
        return coverageScore;
    }

    public Map<String, Set<String>>  getLinkedConcepts() {
        return linkedConcepts;
    }

    public Set<String> getNonLinkedConcepts() {
        return nonLinkedConcepts;
    }
}
