package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.setSimilarityMeasures;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class DiceCoefficientTest {

    private static final double DELTA = 0.00001;

    @Test
    void calculateSimilarity() {
        String[] arraySet1 = {"hello", "world"};
        String[] arraySet2 = {"hello", "world", "peace"};
        HashSet<String> set1 = new HashSet<>(Arrays.asList(arraySet1));
        HashSet<String> set2 = new HashSet<>(Arrays.asList(arraySet2));

        DiceCoefficient sim = new DiceCoefficient();

        // test arraySet operation
        assertEquals((4.0/5.0),sim.calculateSimilarity(arraySet1, arraySet2), DELTA);
        assertEquals(1.0,sim.calculateSimilarity(arraySet1, arraySet1), DELTA);

        // test set operation
        assertEquals((4.0/5.0),sim.calculateSimilarity(set1, set2), DELTA);
        assertEquals(1.0,sim.calculateSimilarity(set1, set1), DELTA);
    }

    @Test
    void calculateSimilarityWithNumbers(){
        DiceCoefficient sim = new DiceCoefficient();
        assertEquals((4.0/5.0),sim.calculateSimilarityWithNumbers(2,2,3), DELTA);
    }

}