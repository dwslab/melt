package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge;

import java.util.concurrent.Callable;
import smile.math.blas.Transpose;
import smile.math.matrix.Matrix;

/**
 * The job/callable to compute the distance matrix in parallel.
 * This is only for euclidean distance.
 */
public class DistanceMatrixComputationJob implements Callable<DistanceMatrixComputationResult> {
    private final double[][] fullArray;
    private final int i;
    private final int j;
    private final int step;
    /**
     * if true, the euclidean distance is squared which means the square root is not computed.
     */
    private final boolean squared;

    public DistanceMatrixComputationJob(double[][] fullArray, int i, int j, int step, boolean squared){
        this.fullArray = fullArray;
        this.i = i;
        this.j = j;
        this.step = step;
        this.squared = squared;
    }

    @Override
    public DistanceMatrixComputationResult call() throws Exception {
        double[][] A = extractSubArray(fullArray, i, i + step);
        double[][] B = extractSubArray(fullArray, j, j + step);
        double[][] result = computeDistanceMatrix(A, B, this.squared);
        return new DistanceMatrixComputationResult(i, j, result);
    }
    
    private static double[][] extractSubArray(double[][] from, int fromIndex, int toIndex){
        int counter = 0;
        
        int to = Math.min(from.length, toIndex);
        double[][] ret = new double[to-fromIndex][];       
        
        for(int i = fromIndex; i < to; i++){
            ret[counter] = from[i];
            counter++;
        }
        return ret;        
    }

    public static double[][] computeDistanceMatrix(double[][] A, double[][] B, boolean squared){
        //https://www.dabblingbadger.com/blog/2020/2/27/implementing-euclidean-distance-matrix-calculations-from-scratch-in-python

        Matrix a = new Matrix(A);
        Matrix b = new Matrix(B);
        
        Matrix result = new Matrix(a.nrows(), b.nrows());  
        
        //A_dots to resuts matrix
        Matrix adots = a.clone();
        double[] sums = adots.mul(adots).rowSums();
        for (int r = 0; r < result.nrows(); r++) {
            for (int c = 0; c < result.ncols(); c++) {
                result.set(r, c, sums[r]);
            }
        }
        
        //add B_dots to result matrix
        Matrix bdots = b.clone();
        sums = bdots.mul(bdots).rowSums();
        for (int r = 0; r < result.nrows(); r++) {
            for (int c = 0; c < result.ncols(); c++) {
                result.add(r, c, sums[c]);
            }
        }
        
        //-2*A.dot(B.T) via BLAS level 3 gemm function
        a.mm(Transpose.NO_TRANSPOSE, Transpose.TRANSPOSE, -2.0, b, 1.0, result);

        //convert to double[] and check negative values
        double[][] resultArray = new double[result.nrows()][result.ncols()];
        for (int r = 0; r < result.nrows(); r++) {
            for (int c = 0; c < result.ncols(); c++) {
                double v = result.get(r, c);
                if(v <= 0){
                    resultArray[r][c] = 0;
                }else if(squared){
                    resultArray[r][c] = v;
                }else{
                    resultArray[r][c] = Math.sqrt(v);
                }
            }
        }
        return resultArray;
    }
    
    /*
    private static double[][] computeDistanceMatrix(double[][] A, double[][] B, boolean squared){
        //using Nd4j        
        INDArray a = Nd4j.create(A);
        INDArray b = Nd4j.create(B);        
        long M = a.shape()[0];
        long N = b.shape()[0];
        
        INDArray aDots = a.mul(a).sum(1).reshape(M,1).mul(Nd4j.ones(1, N));
        //INDArray bDots = b.mul(b).sum(1).mul(Nd4j.ones(M, 1));
        INDArray bDots;
        if(M==1){
            INDArray otwo = b.mul(b).sum(1);
            INDArray oone = Nd4j.ones(M, otwo.length());
            bDots = otwo.mul(oone);
        }else{
            bDots = b.mul(b).sum(1).mul(Nd4j.ones(M, 1));
        }
        
        INDArray intermediate = a.mmul(b.transpose()).mul(2);
        INDArray finall = aDots.add(bDots).sub(intermediate);
        
        BooleanIndexing.replaceWhere(finall, 0, Conditions.lessThan(0));
        if(squared == false)
            finall = Transforms.sqrt(finall);
        return finall.toDoubleMatrix();
    }
    */
}
