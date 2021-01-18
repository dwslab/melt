package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.setSimilarityMeasures;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class CombinedJaccardAndOverlapCoefficientTest {

    private static final double DELTA = 0.00001;


    @Test
    void calculateSimilarity() {

        String[] arraySet1 = {"hello", "world"};
        String[] arraySet2 = {"hello", "world", "peace"};

        CombinedJaccardAndOverlapCoefficient sim = new CombinedJaccardAndOverlapCoefficient();

        // test for arrayset
        assertEquals(sim.calculateSimilarity(arraySet1, arraySet2), 0.5 * (2.0/3.0) + 0.5, DELTA);
        assertEquals(sim.calculateSimilarity(arraySet1, arraySet1), 1.0, DELTA);
    }

    @Test
    void calculateSimilarity1() {

        CombinedJaccardAndOverlapCoefficient sim = new CombinedJaccardAndOverlapCoefficient();
        String[] arraySet1 = {"hello", "world"};
        String[] arraySet2 = {"hello", "world", "peace"};
        HashSet<String> set1 = new HashSet<>(Arrays.asList(arraySet1));
        HashSet<String> set2 = new HashSet<>(Arrays.asList(arraySet2));

        // test for set
        assertEquals(sim.calculateSimilarity(set1, set2), 0.5 * (2.0/3.0) + 0.5, DELTA);
        assertEquals(sim.calculateSimilarity(set1, set1), (1.0), DELTA);
    }

    @Test
    void calculateSimilarityWithNumbers() {
        CombinedJaccardAndOverlapCoefficient sim = new CombinedJaccardAndOverlapCoefficient();
        assertEquals(sim.calculateSimilarityWithNumbers(2,2,3), 0.5 * (2.0/3.0) + 0.5, DELTA);
    }
}