package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.clustering;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.IMatcherMultiSource;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.gradoop.common.model.impl.properties.Properties;
import org.gradoop.common.model.impl.properties.PropertyValue;
import org.gradoop.flink.io.impl.graph.GraphDataSource;
import org.gradoop.flink.io.impl.graph.tuples.ImportVertex;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gradoop.famer.clustering.parallelClustering.AbstractParallelClustering;
import org.gradoop.flink.io.impl.graph.tuples.ImportEdge;
import static org.gradoop.famer.clustering.common.PropertyNames.GRAPH_LABEL;
import static org.gradoop.famer.clustering.common.PropertyNames.SIM_VALUE;
import static org.gradoop.famer.clustering.common.PropertyNames.CLUSTER_ID;
import org.gradoop.famer.clustering.parallelClustering.center.Center;
import org.gradoop.famer.clustering.parallelClustering.clip.CLIP;
import org.gradoop.famer.clustering.parallelClustering.clip.dataStructures.CLIPConfig;
import org.gradoop.famer.clustering.parallelClustering.common.connectedComponents.ConnectedComponents;
import org.gradoop.famer.clustering.parallelClustering.common.dataStructures.ClusteringOutputType;
import org.gradoop.famer.clustering.parallelClustering.common.dataStructures.PrioritySelection;
import org.gradoop.famer.clustering.parallelClustering.correlationClustering.CorrelationClustering;
import org.gradoop.famer.clustering.parallelClustering.mergeCenter.MergeCenter;
import org.gradoop.famer.clustering.parallelClustering.star.Star;
import org.gradoop.famer.clustering.postprocessing.AbstractClusterPostprocessing;

/**
 * A filter for multi source matching.
 * It filters the input alignment by analyzing the structure of the correspondences.
 * E.g. if many entities are fully connected, then this indicates that all of those correspondences are correct.
 * More information on all possible algorithmn which should be chosen in the constructor can be found at <a href="https://dbs.uni-leipzig.de/file/eswc_0.pdf">Scalable Matching and Clustering of Entities with FAMER</a> .
 * <a href="https://git.informatik.uni-leipzig.de/dbs/FAMER/-/tree/master/famer-clustering">The source code can be found at gitlab</a>.
 */
public class FamerClustering implements IMatcherMultiSource<Object, Alignment, Object>, Filter{
    private static final Logger LOGGER = LoggerFactory.getLogger(FamerClustering.class);
    
    private DatasetIDExtractor datsetIdExtractor;
    private AbstractParallelClustering clusteringAlgorithm;

    public FamerClustering(DatasetIDExtractor datsetIdExtractor, AbstractParallelClustering clusteringAlgorithm) {
        this.datsetIdExtractor = datsetIdExtractor;
        this.clusteringAlgorithm = clusteringAlgorithm;
    }
    
    public FamerClustering(DatasetIDExtractor datsetIdExtractor) {
        this.datsetIdExtractor = datsetIdExtractor;
        this.clusteringAlgorithm = new Center(PrioritySelection.MIN, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE);
    }
    
    @Override
    public Alignment match(List<Object> models, Alignment inputAlignment, Object parameters) throws Exception {
        return filter(inputAlignment);
    }
    
    public Alignment filter(Alignment inputAlignment){
        Map<String, Set<Long>> clusters = getClusters(inputAlignment, this.clusteringAlgorithm, this.datsetIdExtractor);
        return RemoveCorrespondencesBasedOnClusterAssignments.removeCorrespondencesMultiCluster(inputAlignment, clusters);
    }
    
    /**
     * Computes a map between uris and correspoding clusterId.
     * @param alignment alignment
     * @param clusteringAlgorithm the cluster algorithm to use. The <code>ClusteringOutputType</code> doesn't matter but for best performance choose <code>ClusteringOutputType.GRAPH</code>.
     * @param datsetIdExtractor the dataset id extractor to use. It gets an URI and returns the corresponding data source id.
     * @return a map between uris and correspoding clusterId
     */
    public static Map<String, Set<Long>> getClusters(Alignment alignment, AbstractParallelClustering clusteringAlgorithm, DatasetIDExtractor datsetIdExtractor){
        if(clusteringAlgorithm == null){
            LOGGER.warn("Clustering algorithmn is null. Thus no clustering can be executed. Returning empty map which can result in wrong filtering.");
            return new HashMap<>();
        }
        LogicalGraphAndSourceIds l = getLogicalGraphFromAlignment(alignment, datsetIdExtractor);
        LogicalGraph inputGraph = l.getGraph();
        
        if(clusteringAlgorithm instanceof CLIP){
            //set the number of sources
            CLIP c = (CLIP)clusteringAlgorithm;            
            CLIPConfig clipConfig = new CLIPConfig(
                    c.getClipConfig().getDelta(), 
                    l.getIds().size(), //this value is changed.
                    c.getClipConfig().isRemoveSourceConsistentVertices(),
                    c.getClipConfig().getSimValueCoef(),
                    c.getClipConfig().getDegreeCoef(),
                    c.getClipConfig().getStrengthCoef()
            );
            clusteringAlgorithm = new CLIP(clipConfig, c.getClusteringOutputType(), c.getMaxIteration());
        }
        
        LogicalGraph clusteredGraph = clusteringAlgorithm.execute(inputGraph);
        
        if(instanceOfOne(clusteringAlgorithm, Center.class, CorrelationClustering.class, MergeCenter.class)){
            try {
                return getClusteringFromLogicalGraphWithLong(clusteredGraph);
            } catch (Exception ex) {
                LOGGER.warn("Tried to extract cluster IDs from graph but did not work. Returning empty map which can result in wrong filtering.");
                return new HashMap<>();
            }
        }
        else if(instanceOfOne(clusteringAlgorithm, Star.class, ConnectedComponents.class)){
            try {
                return getClusteringFromLogicalGraphWithString(clusteredGraph);
            } catch (Exception ex) {
                LOGGER.warn("Tried to extract cluster IDs from graph but did not work. Returning empty map which can result in wrong filtering.");
            }
        }else{
            LOGGER.info("The clusteringAlgorithm is not known and we try to extract the correct clusterIDs");
            try {
                return getClusteringFromLogicalGraphWithLong(clusteredGraph);
            } catch (Exception ex) {
                try {
                    return getClusteringFromLogicalGraphWithString(clusteredGraph);
                } catch (Exception e) {
                    LOGGER.warn("Tried to extract cluster IDs from graph but did not work. Returning empty map which can result in wrong filtering.");
                    
                }
            }
        }
        return new HashMap<>();
    }
    
    public static boolean instanceOfOne(Object o, Class<?>... classes) {
        for (Class<?> c : classes) {
            if(c.isInstance(o)){
                return true;
            }
        }
        return false;
    } 
    
    private static Map<String, Set<Long>> getClusteringFromLogicalGraphWithString(LogicalGraph clusteredGraph) throws Exception{
        // https://git.informatik.uni-leipzig.de/dbs/FAMER/-/blob/master/famer-clustering/src/test/java/org/gradoop/famer/clustering/parallelClustering/star/StarTest.java#L137

        List<Tuple2<String, String>> list = clusteredGraph.getVertices().map(
                    vertex -> Tuple2.of(vertex.getLabel(), vertex.getPropertyValue(CLUSTER_ID).getString())
            ).returns(new TypeHint<Tuple2<String, String>>() { }).collect();
        
        Map<String, Set<Long>> vertexToClusterId = new HashMap<>();
        for(Tuple2<String, String> tuple : list){
            for(String s : tuple.f1.split(",")){
                try{
                    long clusterId = Long.parseLong(s);
                    vertexToClusterId.computeIfAbsent(tuple.f0, __-> new HashSet<>()).add(clusterId);
                }catch(NumberFormatException ex){
                    LOGGER.warn("Could not parse clusterID to Long: {} This mapping between vetrex and cluster id will be skipped. Be warned.", s);
                }
            }
        }
        return vertexToClusterId;
    }
    
    private static Map<String, Set<Long>> getClusteringFromLogicalGraphWithLong(LogicalGraph clusteredGraph) throws Exception{
        // https://git.informatik.uni-leipzig.de/dbs/FAMER/-/blob/master/famer-clustering/src/test/java/org/gradoop/famer/clustering/parallelClustering/center/CenterTest.java#L54
        // https://git.informatik.uni-leipzig.de/dbs/FAMER/-/blob/master/famer-preprocessing/src/main/java/org/gradoop/famer/preprocessing/io/benchmarks/abtbuy/AbtBuyReader.java
        List<Tuple2<String, Long>> list = clusteredGraph.getVertices().map(
                    vertex -> Tuple2.of(vertex.getLabel(), vertex.getPropertyValue(CLUSTER_ID).getLong())
            ).returns(new TypeHint<Tuple2<String, Long>>() { }).collect();
        
        Map<String, Set<Long>> vertexToClusterId = new HashMap<>();
        for(Tuple2<String, Long> tuple : list){
            vertexToClusterId.put(tuple.f0, new HashSet<>(Arrays.asList(tuple.f1)));
        }
        return vertexToClusterId;
    }
    
    private static LogicalGraphAndSourceIds getLogicalGraphFromAlignment(Alignment a, DatasetIDExtractor datsetIdExtractor){
        Collection<ImportEdge<String>> edges = new ArrayList<>(a.size());
        Set<String> elements = new HashSet<>();
        long artificialEdgeId = 0;
        for(Correspondence c : a){            
            Properties properties = Properties.createWithCapacity(1);
            properties.set(SIM_VALUE, PropertyValue.create(c.getConfidence()));
            edges.add(new ImportEdge<>(Long.toString(artificialEdgeId++), c.getEntityOne(), c.getEntityTwo(), "", properties));//getRandomString(10)
            elements.add(c.getEntityOne());
            elements.add(c.getEntityTwo());
        }
        
        Set<String> sourceIds = new HashSet<>();
        Collection<ImportVertex<String>> vertices = new ArrayList<>(elements.size());
        for(String element : elements){
            Properties properties = Properties.createWithCapacity(1);
            String sourceId = datsetIdExtractor.getDatasetID(element);
            sourceIds.add(sourceId);
            properties.set(GRAPH_LABEL, PropertyValue.create(sourceId));
            vertices.add(new ImportVertex<>(element, element, properties));
        }
        
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        GradoopFlinkConfig config = GradoopFlinkConfig.createConfig(env);
        
        return new LogicalGraphAndSourceIds(
            new GraphDataSource<>(env.fromCollection(vertices), env.fromCollection(edges), null, config).getLogicalGraph(),
            sourceIds
        );
    }
}
class LogicalGraphAndSourceIds{
    private LogicalGraph graph;
    private Set<String> ids;

    public LogicalGraphAndSourceIds(LogicalGraph graph, Set<String> ids) {
        this.graph = graph;
        this.ids = ids;
    }

    public LogicalGraph getGraph() {
        return graph;
    }

    public Set<String> getIds() {
        return ids;
    }
}