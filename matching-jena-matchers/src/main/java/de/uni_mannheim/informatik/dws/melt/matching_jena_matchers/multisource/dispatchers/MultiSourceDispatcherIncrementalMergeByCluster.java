package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge.ClusterLinkage;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge.ClusterDistance;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge.Clusterer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge.ClustererSmile;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matches multiple ontologies / knowledge graphs with an incremental merge approach.
 * This means that two ontologies are merged together and then possibly the union is merged with another ontology and so on.
 * The order how they are merged is defined by subclasses.
 */
public abstract class MultiSourceDispatcherIncrementalMergeByCluster extends MultiSourceDispatcherIncrementalMerge{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiSourceDispatcherIncrementalMergeByCluster.class);
    
    /**
     * the cluster linkage method lie single averge or complete.
     */
    private ClusterLinkage linkage;
    
    /**
     * The cluster distance like euclidean (the default) etc.
     */
    private ClusterDistance distance;
    
    /**
     * The cluster implementation.
     */
    private Clusterer clusterer;
    
    public MultiSourceDispatcherIncrementalMergeByCluster(Object oneToOneMatcher) {
        this(oneToOneMatcher, ClusterLinkage.COMPLETE);
    }
    
    public MultiSourceDispatcherIncrementalMergeByCluster(Object oneToOneMatcher, ClusterLinkage linkage) {
        this(oneToOneMatcher, linkage, ClusterDistance.EUCLIDEAN);
    }
    
    public MultiSourceDispatcherIncrementalMergeByCluster(Object oneToOneMatcher, ClusterLinkage linkage, ClusterDistance distance) {
        this(oneToOneMatcher, linkage, distance, new ClustererSmile());
    }
    
    public MultiSourceDispatcherIncrementalMergeByCluster(Object oneToOneMatcher, ClusterLinkage linkage, ClusterDistance distance, Clusterer clusterer) {
        super(oneToOneMatcher);
        this.linkage = linkage;
        this.distance = distance;
        this.clusterer = clusterer;
    }
    
    //with Supplier instead of matcher object
    
    public MultiSourceDispatcherIncrementalMergeByCluster(Supplier<Object> matcherSupplier, ClusterLinkage linkage) {
        this(matcherSupplier, linkage, ClusterDistance.EUCLIDEAN);
    }
    
    public MultiSourceDispatcherIncrementalMergeByCluster(Supplier<Object> matcherSupplier, ClusterLinkage linkage, ClusterDistance distance) {
        this(matcherSupplier, linkage, distance, new ClustererSmile());
    }
    
    public MultiSourceDispatcherIncrementalMergeByCluster(Supplier<Object> matcherSupplier, ClusterLinkage linkage, ClusterDistance distance, Clusterer clusterer) {
        super(matcherSupplier);
        this.linkage = linkage;
        this.distance = distance;
        this.clusterer = clusterer;
    }
    
    
    
    @Override
    public MergeOrder getMergeTree(List<Set<Object>> models, Object parameters){
        LOGGER.info("Compute cluster features");
        double[][] features = getClusterFeatures(models, parameters);
        LOGGER.info("Run clustering");
        return this.clusterer.run(features, linkage, distance);
    }
    
    public abstract double[][] getClusterFeatures(List<Set<Object>> models, Object parameters);

    
    public ClusterLinkage getLinkage() {
        return linkage;
    }

    public void setLinkage(ClusterLinkage linkage) {
        this.linkage = linkage;
    }

    public ClusterDistance getDistance() {
        return distance;
    }

    public void setDistance(ClusterDistance distance) {
        this.distance = distance;
    }

    public Clusterer getClusterer() {
        return clusterer;
    }

    public void setClusterer(Clusterer clusterer) {
        if(clusterer == null)
            throw new IllegalArgumentException("Clusterer is null");
        this.clusterer = clusterer;
    }
}