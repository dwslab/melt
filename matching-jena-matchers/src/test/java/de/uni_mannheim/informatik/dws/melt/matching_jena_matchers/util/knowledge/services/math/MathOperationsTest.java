package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.math;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;


import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.testTools.TestOperations.isSameDoubleArray;
import static org.junit.jupiter.api.Assertions.*;

class MathOperationsTest {

    @Test
    void calculateEuclideanDistance() {
        double[] v1 = {1.0, 2.0, 3.0};
        double[] v2 = {1.0, 2.0, 3.0};
        assertEquals(MathOperations.calculateEuclideanDistance(v1, v2), 0.0);

        double[] v3 = {1.0, 2.0};
        double[] v4 = {2.0, 1.0};
        assertEquals(MathOperations.calculateEuclideanDistance(v3,v4), Math.sqrt(2.0));
    }

    @Test
    void cutAtThreshold(){
        double[] input1 = {0.1, 0.2, 0.3};
        double[] solution1 = {0.0, 1.0, 1.0};
        assertTrue(isSameDoubleArray(MathOperations.cutAtThreshold(input1, 0.15), solution1));

        double[] input2 = {0.2, 0.2, 0.3};
        double[] solution2 = {1.0, 1.0, 1.0};
        assertTrue(isSameDoubleArray(MathOperations.cutAtThreshold(input2, 0.15), solution2));
    }


    @Test
    void calculateCosineSimilarity(){
        double[] v1 ={2, 0, 1, 1, 0, 2, 1, 1};
        double[] v2 = {2, 1, 1, 0, 1, 1, 1, 1};
        assertEquals(MathOperations.cosineSimilarity(ArrayUtils.toObject(v1), ArrayUtils.toObject(v2)), 0.8215838362577491);
    }

}