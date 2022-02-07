package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.HumanReadbleByteCount;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smile.clustering.HierarchicalClustering;
import smile.clustering.linkage.CompleteLinkage;
import smile.clustering.linkage.Linkage;
import smile.clustering.linkage.SingleLinkage;
import smile.clustering.linkage.UPGMALinkage;
import smile.clustering.linkage.UPGMCLinkage;
import smile.clustering.linkage.WPGMALinkage;
import smile.clustering.linkage.WPGMCLinkage;
import smile.clustering.linkage.WardLinkage;
import smile.math.MathEx;
import smile.math.distance.Distance;
import smile.math.distance.ManhattanDistance;

/**
 * Clusterer based on the SMILE library.
 */
public class ClustererSmile implements Clusterer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClustererSmile.class);

    private int numberOfThreads;
    private int numberOfExamplesPerThread;

    public ClustererSmile() {
        this(0);
    }

    /**
     * Clusterer based on the SMILE library.
     *
     * @param numberOfThreads number of threads to use (-1 to use all processors
     * and 0 to use no threads)
     */
    public ClustererSmile(int numberOfThreads) {
        this(numberOfThreads, 700);
    }

    /**
     * Clusterer based on the SMILE library.
     *
     * @param numberOfThreads number of threads to use (-1 to use all processors
     * and 0 to use no threads)
     * @param numberOfExamplesPerThread number of examples to compute in each
     * batch/thread
     */
    public ClustererSmile(int numberOfThreads, int numberOfExamplesPerThread) {
        if (numberOfThreads < 0) {
            this.numberOfThreads = Runtime.getRuntime().availableProcessors();
        }
        if (numberOfExamplesPerThread < 1) {
            throw new IllegalArgumentException("numberOfExamplesPerThread cannot be lower than one.");
        }
        this.numberOfThreads = numberOfThreads;
        this.numberOfExamplesPerThread = numberOfExamplesPerThread;
    }

    @Override
    public ClusterResult run(double[][] features, ClusterLinkage linkage, ClusterDistance distance) {
        float[] proximity = getProximity(features, distance);
        Linkage hacLinkage = getLinkage(features.length, proximity, linkage);

        HierarchicalClustering clusters = HierarchicalClustering.fit(hacLinkage);
        return new ClusterResult(clusters.getTree(), clusters.getHeight());
    }

    /**
     * Return the linkage which can be used to calculate the hierarchical
     * clustering.
     *
     * @param exampleSize the number of examples
     * @param proximity the lower triangle of the distance matrix (linearized)
     * @param linkage the linkage method (like eucledian etc)
     * @return linkage
     */
    private Linkage getLinkage(int exampleSize, float[] proximity, ClusterLinkage linkage) {
        switch (linkage) {
            case SINGLE:
                return new SingleLinkage(exampleSize, proximity);
            case AVERAGE:
                return new UPGMALinkage(exampleSize, proximity);
            case COMPLETE:
                return new CompleteLinkage(exampleSize, proximity);
            case CENTROID:
                return new UPGMCLinkage(exampleSize, proximity);
            case MEDIAN:
                return new WPGMCLinkage(exampleSize, proximity);
            case WARD:
                return new WardLinkage(exampleSize, proximity);
            case WPGMA:
                return new WPGMALinkage(exampleSize, proximity);
            default: {
                LOGGER.warn("Linkage was not found. Defaulting to single link.");
                return new SingleLinkage(exampleSize, proximity);
            }
        }
    }

    /**
     * Calculates the proximity which is the lower triangular part of the
     * distance matrix. This means the returned float array contains all
     * pairwise distances.
     * @param features the features to use
     * @param distance the distance matric to use
     * @return the proximity
     */
    public float[] getProximity(double[][] features, ClusterDistance distance){
        if(this.numberOfThreads > 1){
            if(distance == ClusterDistance.EUCLIDEAN)
                return proximityEuclideanParallel(features, this.numberOfThreads, this.numberOfExamplesPerThread, false);
            else if(distance == ClusterDistance.SQUARED_EUCLIDEAN)
                return proximityEuclideanParallel(features, this.numberOfThreads, this.numberOfExamplesPerThread, true);
            else{
                return proximityParallel(features, distance);
            }
        }else{
            return proximity(features, distance);
        }
    }
        
    private static Distance<double[]> getSmileDistanceFunction(ClusterDistance distance) {
        switch (distance) {
            case EUCLIDEAN:                
                return MathEx::distance;//new EuclideanDistance();
            case SQUARED_EUCLIDEAN:
                return MathEx::squaredDistance;
            case MANHATTAN:
                return new ManhattanDistance();
            default:
                LOGGER.warn("ClusterDistance was not found. Defaulting to EuclideanDistance.");
                return MathEx::distance;
        }
    }

    public static float[] proximity(double[][] data, ClusterDistance clusterDistance) {
        long n = data.length;
        if(n > 65535) {
            throw new IllegalArgumentException("This implementation does not scale to datasets which has more than 65535 instances" +
                    " because the pairwise distance does not fit in one array (integer index).");
        }
        long length = n * (n + 1) / 2;
        Distance<double[]> distance = getSmileDistanceFunction(clusterDistance);

        float[] proximity = new float[(int)length];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                int k = (int)(length - (n - j) * (n - j + 1) / 2 + i - j);
                proximity[k] = (float) distance.d(data[i], data[j]);
            }
        }
        return proximity;
    }
    
    public static float[] proximityParallel(double[][] data, ClusterDistance clusterDistance){
        long n = data.length;
        if(n > 65535) {
            throw new IllegalArgumentException("This implementation does not scale to datasets which has more than 65535 instances" +
                    " because the pairwise distance does not fit in one array (integer index).");
        }
        long length = n * (n+1) / 2;
        Distance<double[]> distance = getSmileDistanceFunction(clusterDistance);

        float[] proximity = new float[(int)length];
        IntStream.range(0, (int)n).parallel().forEach(i -> {
            for (int j = 0; j < i; j++) {
                int k = (int) (length - (n-j)*(n-j+1)/2 + i - j);
                proximity[k] = (float) distance.d(data[i], data[j]);
            }
        });
        return proximity;
    }
    
    public static float[] proximityEuclideanParallel(double[][] data, int numberOfThreads, int numberOfExamplesPerThread, boolean squared){
        ExecutorService exec = Executors.newFixedThreadPool(numberOfThreads);
        long n = data.length;
        if(n > 65535) {
            throw new IllegalArgumentException("This implementation does not scale to datasets which has more than 65535 instances" +
                    " because the pairwise distance does not fit in one array (integer index).");
        }
        long length = n * (n+1) / 2;
        
        String featureMatrix = HumanReadbleByteCount.convert(8 * n * data[0].length); //double needs 8 bytes
        String distanceMatrix = HumanReadbleByteCount.convert(4 * length); // float needs 4 bytes
        LOGGER.info("Feature matrix: Number of rows/documents: {}  Number of columns/features: {}  Memory consumption: {}", n, data[0].length, featureMatrix);
        LOGGER.info("Distance matrix: Length: {}  Memory consumption: {}", length, distanceMatrix);
        
        CompletionService<DistanceMatrixComputationResult> completionService = new ExecutorCompletionService<>(exec);
        long numberJobs = 0;
        for(int i=0; i < n; i+=numberOfExamplesPerThread){
            for(int j=0; j <= i; j+=numberOfExamplesPerThread){
                DistanceMatrixComputationJob calulation = new DistanceMatrixComputationJob(data, i, j, numberOfExamplesPerThread, squared);
                completionService.submit(calulation);
                numberJobs++;
            }
        }
        LOGGER.info("Number of jobs: {}  ({} in parallel)", numberJobs, numberOfThreads);

        int processedJobs = 0;
        float[] proximity = new float[(int)length];
        for(int i = 0; i < numberJobs; i++ ) {
            try {
                DistanceMatrixComputationResult r = completionService.take().get();
                if(i%100==0)
                    LOGGER.info("Process job {} / {}", processedJobs, numberJobs);
                LOGGER.debug("Process job {} / {}", processedJobs, numberJobs);
                processedJobs++;
                //insert
                for(int k = 0; k < r.getResult().length; k++){
                    double[] myrow = r.getResult()[k];
                    long rowPos = r.getI() + k;
                    for(int l = 0; l < myrow.length; l++){
                        long columnPos = r.getJ()+l;
                        if(rowPos >= columnPos){ //important because results arrive not in same order
                            long x = length - (n-columnPos)*(n-columnPos+1)/2 + rowPos - columnPos;
                            if(x >= length){
                                LOGGER.error("Wrong position {}: row:{} ({} + {}) col:{} ({} + {}) ||  {}, {}", x,
                                        rowPos,r.getI(), k, columnPos, r.getJ(), l, r.getResult().length, myrow.length);
                                throw new IllegalArgumentException("Could not process result because index position is wrong.");
                            }
                            proximity[(int)x] = (float) myrow[l];
                        }
                    }
                }
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.warn("Error when waiting for parallel results of matcher execution.", ex);
            }
        }
        exec.shutdown();
        return proximity;        
    }
}
