package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.setSimilarityMeasures;

import java.util.HashSet;

/**
 * Interface for similarity measures of sets.
 */
public interface SetSimilarityMeasure {

    /**
     * Instance-Specific similarity calculation.
     * Returns a similarity value in the range of [0.0, 1.0].
     * @param set1 Set 1 as String Array.
     * @param set2 Set 2 as String Array.
     * @return A similarity value in the range of [0.0, 1.0].
     */
    double calculateSimilarity(String[] set1, String[] set2);


    /**
     * Instance-Specific similarity calculation.
     * Returns a similarity value in the range of [0.0, 1.0].
     * @param set1 Set 1.
     * @param set2 Set 2.
     * @return A similarity value in the range of [0.0, 1.0].
     */
    double calculateSimilarity(HashSet<String> set1, HashSet<String> set2);


    /**
     * A quick way to calcualte set similarity.
     * @param common The number of common elements.
     * @param sizeSet1 The size of set 1.
     * @param sizeSet2 The size of set 2.
     * @return The similarity result.
     */
    double calculateSimilarityWithNumbers(int common, int sizeSet1, int sizeSet2);

    /**
     * Get the name representation of the SetSimilarity Measure.
     * @return Name respresentation as String.
     */
    String getName();

}
