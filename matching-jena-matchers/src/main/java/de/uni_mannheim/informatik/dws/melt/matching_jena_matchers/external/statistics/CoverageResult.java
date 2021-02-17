package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.statistics;


import java.util.Map;
import java.util.Set;

/**
 * The result object of {@link Coverage}.
 * A CoverageResult is immutable.
 */
public class CoverageResult {


    /**
     * Constructor
     * @param coverageScore The coverage score.
     * @param linkedConcepts The linked concepts.
     * @param nonLinkedConcepts The concepts not linked.
     */
    public CoverageResult(double coverageScore, Map<String, Set<String>>  linkedConcepts, Set<String> nonLinkedConcepts){
        this((float) coverageScore, linkedConcepts, nonLinkedConcepts);
    }

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

    /**
     * Helper method.
     * @param isLongString True if an extensive string shall be generated.
     * @return String representation.
     */
    private String getString(boolean isLongString){
        StringBuffer result = new StringBuffer()
                .append("Coverage Score: " + coverageScore + "\n")
                .append("Concepts Found: " + linkedConcepts.size() + "\n");
        if(isLongString) {
            for (Map.Entry<String, Set<String>> conceptLinks : linkedConcepts.entrySet()) {
                result.append("\t" + conceptLinks.getKey() + "\n");
                for (String s : conceptLinks.getValue()) {
                    result.append("\t\t" + s + "\n");
                }
            }
        }
        result.append("Concepts Not Found: " + nonLinkedConcepts.size() + "\n");
        if(isLongString) {
            for (String concept : nonLinkedConcepts) {
                result.append("\t" + concept + "\n");
            }
        }
        return result.toString();
    }

    /**
     * A more concise String representation of the instance than {@link CoverageResult#toString()}.
     * @return String representation of the object.
     */
    public String toShortString(){
        return getString(false);
    }

    @Override
    public String toString(){
        return getString(true);
    }
}
