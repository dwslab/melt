package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MergeOrder;
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

    /**
     * Number of threads to compute the distance matrix.
     */
    private int numberOfThreads;
    /**
     * Number of examples to process in each thread for computing the distance matrix.
     * E.g. if specified 10, then the distances between these 10 examples are computed in one thread.
     */
    private int numberOfExamplesPerThread;
    /**
     * If true uses the BLAS component to calculate the distance matrix (this might not be numerically stable).
     */
    private boolean useBLAS;

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
     * @param numberOfExamplesPerThread number of examples to compute in each batch/thread
     */
    public ClustererSmile(int numberOfThreads, int numberOfExamplesPerThread){
        this(numberOfThreads, numberOfExamplesPerThread, true);
    }

    /**
     * Clusterer based on the SMILE library.
     *
     * @param numberOfThreads number of threads to use (-1 to use all processors and 0 to use no threads)
     * @param numberOfExamplesPerThread number of examples to compute in each batch/thread
     * @param useBLAS if true uses the BLAS component to calculate the distance matrix (this might not be numerically stable)
     */
    public ClustererSmile(int numberOfThreads, int numberOfExamplesPerThread, boolean useBLAS) {
        if (numberOfThreads < 0) {
            this.numberOfThreads = Runtime.getRuntime().availableProcessors();
        }
        if (numberOfExamplesPerThread < 1) {
            throw new IllegalArgumentException("numberOfExamplesPerThread cannot be lower than one.");
        }
        this.numberOfThreads = numberOfThreads;
        this.numberOfExamplesPerThread = numberOfExamplesPerThread;
        this.useBLAS = useBLAS;
    }

    @Override
    public MergeOrder run(double[][] features, ClusterLinkage linkage, ClusterDistance distance) {
        if(features.length == 0){
            LOGGER.warn("Features for clustering is empty and do not contain any rows. Return empty merge order.");
            return new MergeOrder(new int[0][0]);
        }
        long n = features.length;
        long distanceMatrixLength = n * (n+1) / 2;
        
        String featureMatrix = HumanReadbleByteCount.convert(8 * n * features[0].length); //double needs 8 bytes
        String distanceMatrix = HumanReadbleByteCount.convert(4 * distanceMatrixLength); // float needs 4 bytes
        LOGGER.info("Compute distance matrix for {} instances/rows with {} features/columns (feature matrix requires {})."
                + "The distance matrix will have {} entries (requires {}).",
                features.length, features[0].length, featureMatrix, distanceMatrixLength, distanceMatrix);
        float[] proximity = getProximity(features, distance);
        
        LOGGER.info("Compute the linkage based on the distance matrix.");
        Linkage hacLinkage = getLinkage(features.length, proximity, linkage);
        HierarchicalClustering clusters = HierarchicalClustering.fit(hacLinkage);
        
        LOGGER.info("Finished computing the linkage.");
        return new MergeOrder(clusters.getTree(), clusters.getHeight());
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
            if(this.useBLAS){
                if(distance == ClusterDistance.EUCLIDEAN)
                    return proximityEuclideanParallel(features, this.numberOfThreads, this.numberOfExamplesPerThread, false);
                else if(distance == ClusterDistance.SQUARED_EUCLIDEAN)
                    return proximityEuclideanParallel(features, this.numberOfThreads, this.numberOfExamplesPerThread, true);
                else{
                    return proximityParallel(features, distance);
                }
            }else{
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
        if(n > 46340) {
            throw new IllegalArgumentException("This implementation does not scale to datasets which has more than 46340 instances");
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
        if(n > 46340) {
            throw new IllegalArgumentException("This implementation does not scale to datasets which has more than 46340 instances");
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
        if(n > 46340) {
            throw new IllegalArgumentException("This implementation does not scale to datasets which has more than 46340 instances");
        }
        long length = n * (n+1) / 2;
        
        CompletionService<DistanceMatrixComputationResult> completionService = new ExecutorCompletionService<>(exec);
        long numberJobs = 0;
        for(int i=0; i < n; i+=numberOfExamplesPerThread){
            for(int j=0; j <= i; j+=numberOfExamplesPerThread){
                DistanceMatrixComputationJob calulation = new DistanceMatrixComputationJob(data, i, j, numberOfExamplesPerThread, squared);
                completionService.submit(calulation);
                numberJobs++;
            }
        }
        LOGGER.info("Number of jobs created to compute the distance matrix: {}  ({} in parallel)", numberJobs, numberOfThreads);

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
/*
//WEKA
    
public static void main(String[] args){
    HierarchyVisualizer
    //graphs.
    Instances dataset=null;// = load(DATA);
    HierarchicalClusterer hc = new HierarchicalClusterer();
    hc.setLinkType(new SelectedTag(4, TAGS_LINK_TYPE));  // CENTROID
    hc.setNumClusters(3);
    try {
        hc.buildClusterer(dataset);
        for (Instance instance : dataset) {
            System.out.printf("(%.0f,%.0f): %s%n", 
                    instance.value(0), instance.value(1), 
                    hc.clusterInstance(instance));
        }
        hc.graph()
        //displayDendrogram(hc.graph());
    } catch (Exception e) {
        System.err.println(e);
    }
}


private Instances getInstances(List<String> texts) throws Exception {
    ArrayList<Attribute> attributes = new ArrayList<>();
    Attribute contents = new Attribute("contents");
    attributes.add(contents);
    Instances data = new Instances("texts", attributes, texts.size());
    for(String s : texts){
        Instance inst = new DenseInstance(1);
        inst.setValue(contents, s);
        data.add(inst);
    }

    StringToWordVector filter = new StringToWordVector();
    filter.setInputFormat(data);
    filter.setIDFTransform(true);
    filter.setStopwordsHandler(new Rainbow());
    filter.setLowerCaseTokens(true);

    HierarchicalClusterer hc = new HierarchicalClusterer();
    hc.setLinkType(new SelectedTag(4, TAGS_LINK_TYPE));  // CENTROID
    hc.setNumClusters(3);

    FilteredClusterer fc = new FilteredClusterer();
    fc.setFilter(filter);
    fc.setClusterer(hc);
    fc.buildClusterer(data);

    return data;
}
    
*/