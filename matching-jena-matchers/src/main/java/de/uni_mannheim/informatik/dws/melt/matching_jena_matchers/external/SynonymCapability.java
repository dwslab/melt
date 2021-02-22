package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external;

import java.util.Set;

/**
 * Interface for external resources that are capable to determine whether two
 * concepts are synonymous given that the concepts are already linked.
 */
public interface SynonymCapability {

    /**
     * Get the synonyms.
     * @param linkedConcept Linked concept.
     * @return A set of synonyms. // TODO: be more specific links or words
     */
    Set<String> getSynonymsLexical(String linkedConcept);

    /**
     * Given two links, determine whether those are synonymous.
     * Some sources have multiple notions of synonymy, if this is the case,
     * this method refers to the looser interpretation.
     *
     * @param linkedConcept1 Linked concept 1.
     * @param linkedConcept2 Linked concept 2.
     * @return True if synonymous, else false.
     */
    boolean isSynonymous(String linkedConcept1, String linkedConcept2);

    /**
     * Given two links, determine whether those are synonymous.
     * Some sources have multiple notions of synonymy, if this is the case,
     * this method refers to the stricter interpretation.
     *
     * @param linkedConcept1 Linked concept 1.
     * @param linkedConcept2 Linked concept 2.
     * @return True if synonymous, else false.
     */
    boolean isStrongFormSynonymous(String linkedConcept1, String linkedConcept2);
}
