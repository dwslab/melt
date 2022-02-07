package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge.ClusterLinkage;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge.ClusterDistance;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge.Clusterer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge.ClustererSmile;
import java.util.List;
import java.util.Set;
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
    
    @Override
    public int[][] getMergeTree(List<Set<Object>> models, Object parameters){
        //CSVFormat format = CSVFormat.DEFAULT.withDelimiter('\t').withIgnoreSurroundingSpaces(true);
        //double[][] data = Read.csv("", format).toArray(false, CategoricalEncoder.ONE_HOT);        
        double[][] data = getClusterFeatures(models, parameters);
        
        HierarchicalClustering clusters = HierarchicalClustering.fit(getLinkage(data));
        int[][] tree = clusters.getTree();
        //double[] height = clusters.getHeight();
        
        //JFrame f = new JFrame();
        //f.setSize(new Dimension(1000, 1000));
        //f.setLocationRelativeTo(null);
        //f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //f.getContentPane().add(new Dendrogram(clusters.getTree(), clusters.getHeight()).canvas().panel());
        //f.setVisible(true);
        return tree;
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
    
    
    
    private Linkage getLinkage(double[][] data){
        switch(this.linkage){
            case SINGLE:
                return SingleLinkage.of(data);
            case AVERAGE:
                return UPGMALinkage.of(data);
            case COMPLETE:
                return CompleteLinkage.of(data);
            case CENTROID:
                return UPGMCLinkage.of(data);
            case MEDIAN:
                return WPGMCLinkage.of(data);
            case WARD:
                return WardLinkage.of(data);
            case WPGMA:
                return WPGMALinkage.of(data);
            default:{
                LOGGER.warn("Linkage was not found. Defaulting to single link.");
                return SingleLinkage.of(data);
            }
        }
    }
    
    //WEKA
    /*
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
}