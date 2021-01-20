package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.math;

/**
 * Interface for classes that are able to calculate vector distances.
 */
public interface VectorDistance {

    /**
     * Calculation of the distance.
     * @param v1 Vector 1.
     * @param v2 Vector 2.
     * @return The distance.
     */
    double calculateDistance(double[] v1, double[] v2);

}
