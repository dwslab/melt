package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge;

import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClustererSmileTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClustererSmileTest.class);
    
    @Test
    void testSameResult() {
        double[][] features = getRandomDoubleMatrix(201, 200, 123);
        
        long start = System.currentTimeMillis();
        float[] prox = ClustererSmile.proximity(features, ClusterDistance.EUCLIDEAN);
        long diff = System.currentTimeMillis() - start;
        LOGGER.info("proximity takes {} ms", diff);
        
        start = System.currentTimeMillis();
        float[] proxTwo = ClustererSmile.proximityParallel(features, ClusterDistance.EUCLIDEAN);
        diff = System.currentTimeMillis() - start;
        LOGGER.info("proximityEuclidean takes {} ms", diff);
        assertTrue(Arrays.equals(prox, proxTwo));
        
        start = System.currentTimeMillis();
        float[] proxThree = ClustererSmile.proximityEuclideanParallel(features, 2, 200, false);
        diff = System.currentTimeMillis() - start;
        LOGGER.info("proximityEuclideanParallel takes {} ms", diff);
        
        assertTrue(equalsFloatArray(prox, proxThree, 0.00001f)); // due to numerical instability we need to use epsilon = 0.00001
    }
    
    
    @Test
    void comparisonWithElki() {
        double[][] features = getRandomDoubleMatrix(100, 20, 123);
        
        ClustererSmile smile = new ClustererSmile();
        ClusterResult smileResult = smile.run(features, ClusterLinkage.SINGLE, ClusterDistance.EUCLIDEAN);
        
        ClustererELKI elki = new ClustererELKI(true);
        ClusterResult elkiResult = elki.run(features, ClusterLinkage.SINGLE, ClusterDistance.EUCLIDEAN);
        
        assertEquals(smileResult.hashCode(), elkiResult.hashCode());
        assertTrue(smileResult.equals(elkiResult));
        
        elki = new ClustererELKI(false); // will use SLINK instead of anderberg
        elkiResult = elki.run(features, ClusterLinkage.SINGLE, ClusterDistance.EUCLIDEAN);
        assertEquals(smileResult.hashCode(), elkiResult.hashCode());
        assertEquals(smileResult, elkiResult);
    }
    
    
    private static double[][] getRandomDoubleMatrix(int rows, int columns, long seed){
        double[][] array = new double[rows][columns];
        Random rnd = new Random(seed);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                // multiple the random by 10 and then cast to in
                array[i][j] = rnd.nextDouble();
            }
        }
        return array;
    }
    
    private static boolean equalsFloatArray(float[] one, float[] two, float eps){
        if(one.length != two.length){
            return false;
        }
        for(int i=0; i < one.length; i++){
            if(Math.abs(one[i]-two[i]) > eps){
                //LOGGER.info("Pos {} (left: {} right: {})", i, one[i], two[i]);
                return false;
            }
        }
        return true;
    }
}
