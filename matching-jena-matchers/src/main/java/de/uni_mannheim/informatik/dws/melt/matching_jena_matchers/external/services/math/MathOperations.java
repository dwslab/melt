package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * A helper class for mathematical and statistical operations.
 */
public class MathOperations {

    private static Logger LOG = LoggerFactory.getLogger(MathOperations.class);

    /**
     * Calculates the euclidean distance between two vectors.
     * @param v1 Vector 1.
     * @param v2 Vector 2.
     * @return Euclidean distance.
     */
    public static double calculateEuclideanDistance(double[] v1, double[] v2){
        if(v1.length != v2.length){
            // calculation not possible
            LOG.error("Calculation not possible due to differently sized vectors. Aborting operation.");
            return -1.0;
        }
        double sum = 0.0;
        // sum squares of differences
        for(int i = 0; i < v1.length; i++){
            sum = sum + Math.pow((v1[i] - v2[i]),2);
        }
        return Math.sqrt(sum);
    }


    /**
     * This method will set values below the threshold to 0.0 and those equal or above to 1.0.
     * @param input double array that shall be cut
     * @param threshold cutting threshold
     * @return new double array
     */
    public static double[] cutAtThreshold(double[] input, double threshold){
        return Arrays.stream(input).map(x -> {
            if(x >= threshold){
                return  1.0;
            } else {
                return 0.0;
            }
        }).toArray();
    }

 
    /**
     * Calculate The cosine similarity between two vectors.
     * @param vector1 First vector.
     * @param vector2 Second vector.
     * @return Cosine similarity as double.
     */
    public static double cosineSimilarity(Double[] vector1, Double[] vector2){
        if(vector1 == null || vector2 == null){
            return 0.0;
        }
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 = norm1 +  Math.pow(vector1[i], 2);
            norm2 = norm2 + Math.pow(vector2[i], 2);
        }
        return dotProduct / ( Math.sqrt(norm1) * Math.sqrt(norm2) );
    }
    
    
    /**
     * Calculate The cosine similarity between two vectors.
     * @param vector1 First vector.
     * @param vector2 Second vector.
     * @return Cosine similarity as double.
     */
    public static double cosineSimilarity(double[] vector1, double[] vector2) {
    	 if(vector1 == null || vector2 == null){
             return 0.0;
         }
         double dotProduct = 0.0;
         double norm1 = 0.0;
         double norm2 = 0.0;
         for (int i = 0; i < vector1.length; i++) {
             dotProduct += vector1[i] * vector2[i];
             norm1 = norm1 +  Math.pow(vector1[i], 2);
             norm2 = norm2 + Math.pow(vector2[i], 2);
         }
         return dotProduct / ( Math.sqrt(norm1) * Math.sqrt(norm2) );
    }

}
