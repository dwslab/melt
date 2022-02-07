package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge;

/**
 * A result of the distance matrix computation. It contains the partial distance matrix and the correspodning offsets.
 */
public class DistanceMatrixComputationResult {
    private final int i;
    private final int j;
    private final double[][] result;

    public DistanceMatrixComputationResult(int i, int j, double[][] result) {
        this.i = i;
        this.j = j;
        this.result = result;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public double[][] getResult() {
        return result;
    }
}
