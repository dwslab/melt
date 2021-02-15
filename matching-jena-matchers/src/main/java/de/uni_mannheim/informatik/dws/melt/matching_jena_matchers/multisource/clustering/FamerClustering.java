package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.clustering;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.ArrayList;
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

/**
 * 
 * https://git.informatik.uni-leipzig.de/dbs/FAMER/-/blob/master/famer-clustering/src/test/java/org/gradoop/famer/clustering/parallelClustering/center/CenterTest.java#L54
 * https://git.informatik.uni-leipzig.de/dbs/FAMER/-/blob/master/famer-preprocessing/src/main/java/org/gradoop/famer/preprocessing/io/benchmarks/abtbuy/AbtBuyReader.java
 */
public class FamerClustering {
    private static final Logger LOGGER = LoggerFactory.getLogger(FamerClustering.class);
    
    /**
     * Computes a map between uris and correspoding clusterId.
     * @param alignment alignment
     * @param clusteringAlgorithm the cluster algorithm to use. The <code>ClusteringOutputType</code> doesn't matter but for best performance choose <code>ClusteringOutputType.GRAPH</code>.
     * @param datsetIdExtractor the dataset id extractor to use. It gets an URI and returns the corresponding data source id.
     * @return a map between uris and correspoding clusterId
     */
    public Map<String, Long> getClusters(Alignment alignment, AbstractParallelClustering clusteringAlgorithm, DatasetIDExtractor datsetIdExtractor){
        LogicalGraph inputGraph = getLogicalGraphFromAlignment(alignment, datsetIdExtractor);
        LogicalGraph clusteredGraph = clusteringAlgorithm.execute(inputGraph);
        return getClusteringFromLogicalGraph(clusteredGraph);
    }
    
    private Map<String, Long> getClusteringFromLogicalGraph(LogicalGraph clusteredGraph){
        List<Tuple2<String, Long>> list = null;
        try {
            list = clusteredGraph.getVertices().map(
                    vertex -> Tuple2.of(vertex.getLabel(), vertex.getPropertyValue(CLUSTER_ID).getLong())
            ).returns(new TypeHint<Tuple2<String, Long>>() { }).collect();
        } catch (Exception ex) {
            LOGGER.error("Could not make the flink dataset to list", ex);
            list = new ArrayList<>();
        }
        
        Map<String, Long> vertexToClusterId = new HashMap<>();
        for(Tuple2<String, Long> tuple : list){
            vertexToClusterId.put(tuple.f0, tuple.f1);
        }
        return vertexToClusterId;
    }
    
    private LogicalGraph getLogicalGraphFromAlignment(Alignment a, DatasetIDExtractor datsetIdExtractor){
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
        
        Collection<ImportVertex<String>> vertices = new ArrayList<>(elements.size());
        for(String element : elements){
            Properties properties = Properties.createWithCapacity(1);
            properties.set(GRAPH_LABEL, PropertyValue.create(datsetIdExtractor.getDatasetID(element)));
            vertices.add(new ImportVertex<>(element, element, properties));
        }
        
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        GradoopFlinkConfig config = GradoopFlinkConfig.createConfig(env);
        
        return new GraphDataSource<>(env.fromCollection(vertices), env.fromCollection(edges), null, config).getLogicalGraph();
    }
}
