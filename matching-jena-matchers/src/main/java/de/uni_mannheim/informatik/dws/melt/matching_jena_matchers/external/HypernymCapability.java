package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external;

public interface HypernymCapability {

    /**
     * Checks for a one sided hypernymy relation (as opposed to {@link HypernymCapability#isHypernymous(String, String)}.
     * @param superConcept The linked super concept.
     * @param subConcept The linked sub concept.
     * @return True if {@code superConcept} is a hypernym of {@code subConcept}.
     */
    boolean isHypernym(String superConcept, String subConcept);

    /**
     * Checks for hypernymous words in a loose-form fashion: One concept needs to be a hypernym of the other concept
     * where the order of concepts is irrelevant, i.e., the method returns (hypernymous(w1, w2) || hypernymous(w2, w1).
     *
     * The assumed language is English.
     * CHECKS ONLY FOR LEVEL 1 HYPERNYMY - NO REASONING IS PERFORMED.
     *
     * CHECKS ONLY FOR LEVEL 1 HYPERNYMY - NO REASONING IS PERFORMED.
     * @param linkedConcept_1 Link to the first concept.
     * @param linkedConcept_2 Link to the second concept.
     * @return True if the given words are hypernymous, else false.
     */
    boolean isHypernymous(String linkedConcept_1, String linkedConcept_2);

}
