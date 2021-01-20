package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.setSimilarityMeasures;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class JaccardSimilarityTest {
    private static final double DELTA = 0.00001;

    @Test
    void calculateSimilarity() {
        String[] arraySet1 = {"hello", "world"};
        String[] arraySet2 = {"hello", "world", "peace"};
        HashSet<String> set1 = new HashSet<>(Arrays.asList(arraySet1));
        HashSet<String> set2 = new HashSet<>(Arrays.asList(arraySet2));

        JaccardSimilarity sim = new JaccardSimilarity();

        // test for arrayset
        assertEquals(sim.calculateSimilarity(arraySet1, arraySet2), (2.0/3.0), DELTA);
        assertEquals(sim.calculateSimilarity(arraySet1, arraySet1), 1.0, DELTA);

        // test for set
        assertEquals(sim.calculateSimilarity(set1, set2), (2.0/3.0), DELTA);
        assertEquals(sim.calculateSimilarity(set1, set1), (1.0), DELTA);

    }

    @Test
    void calculateSimilarityWithNumbers(){
        JaccardSimilarity sim = new JaccardSimilarity();
        assertEquals(sim.calculateSimilarityWithNumbers(2,2,3), (2.0/3.0), DELTA);
    }
}