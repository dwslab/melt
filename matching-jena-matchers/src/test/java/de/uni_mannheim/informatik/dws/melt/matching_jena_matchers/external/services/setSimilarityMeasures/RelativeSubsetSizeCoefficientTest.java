package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.setSimilarityMeasures;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class RelativeSubsetSizeCoefficientTest {

    @Test
    void calculateSimilarity() {
        String[] arraySet1 = {"hello", "world"};
        String[] arraySet2 = {"hello", "world", "peace"};
        HashSet<String> set1 = new HashSet<>(Arrays.asList(arraySet1));
        HashSet<String> set2 = new HashSet<>(Arrays.asList(arraySet2));

        RelativeSubsetSizeCoefficient c = new RelativeSubsetSizeCoefficient();

        // test arrayset operation
        assertEquals(2.0/2.0, c.calculateSimilarity(arraySet1, arraySet2));

        // test set operation
        assertEquals(2.0/2.0, c.calculateSimilarity(set1, set2));

    }

    @Test
    void calculateSimilarityWithNumbers() {
        RelativeSubsetSizeCoefficient c = new RelativeSubsetSizeCoefficient();
        assertEquals(2.0/3.0, c.calculateSimilarityWithNumbers(2, 3,6));
    }
}