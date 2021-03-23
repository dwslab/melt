package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external;

/**
 * Synonymy can be determined on a continuous scale.
 */
public interface SynonymConfidenceCapability {


    /**
     * Given two links, determine the degree of synonymy.
     *
     * @param linkedConcept1 Linked concept 1.
     * @param linkedConcept2 Linked concept 2.
     * @return True if synonymous, else false.
     */
    double getSynonymyConfidence(String linkedConcept1, String linkedConcept2);

    /**
     * Given two links, determine the degree of synonymy.
     *
     * @param linkedConcept1 Linked concept 1.
     * @param linkedConcept2 Linked concept 2.
     * @return True if synonymous, else false.
     */
    double getStrongFormSynonymyConfidence(String linkedConcept1, String linkedConcept2);
}
