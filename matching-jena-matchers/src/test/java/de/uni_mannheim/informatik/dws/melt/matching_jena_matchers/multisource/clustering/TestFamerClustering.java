package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.clustering;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractorUrlPattern;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.flink.api.java.ExecutionEnvironment;
import static org.gradoop.famer.clustering.common.PropertyNames.GRAPH_LABEL;
import static org.gradoop.famer.clustering.common.PropertyNames.SIM_VALUE;
import org.gradoop.famer.clustering.parallelClustering.AbstractParallelClustering;
import org.gradoop.famer.clustering.parallelClustering.center.Center;
import org.gradoop.famer.clustering.parallelClustering.clip.CLIP;
import org.gradoop.famer.clustering.parallelClustering.clip.dataStructures.CLIPConfig;
import org.gradoop.famer.clustering.parallelClustering.common.connectedComponents.ConnectedComponents;
import org.gradoop.famer.clustering.parallelClustering.common.dataStructures.ClusteringOutputType;
import org.gradoop.famer.clustering.parallelClustering.common.dataStructures.PrioritySelection;
import org.gradoop.famer.clustering.parallelClustering.mergeCenter.MergeCenter;
import org.gradoop.famer.clustering.parallelClustering.star.Star;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;


@EnabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_11)
public class TestFamerClustering {
    
    /***********************************
     * Run these test only on JRE 8-11 because Flink does not yet support java 17:
     * https://stackoverflow.com/questions/71951793/what-is-the-java-version-that-the-flink-can-support-in-2022
     * https://issues.apache.org/jira/browse/FLINK-15736
     ***********************************/
   
    @Test
    public void testCenter(){
        testClusteringApproach(new Center(PrioritySelection.MIN, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE), 4);
    }
    
    @Test
    public void testMergeCenter(){
        testClusteringApproach(new MergeCenter(PrioritySelection.MIN, 1.0, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE), 4);
    }
    
    @Test
    public void testClip(){
        CLIPConfig clipconfig = new CLIPConfig(0.0, 1, false, 0.5, 0.2, 0.3);
        testClusteringApproach(new CLIP(clipconfig, ClusteringOutputType.GRAPH, Integer.MAX_VALUE), 5);
    }
    
    @Test
    public void testStarOne(){
        testClusteringApproach(new Star(PrioritySelection.MIN, Star.StarType.ONE, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE), 4);
    }
    
    @Test
    public void testStarTwo(){
        testClusteringApproach(new Star(PrioritySelection.MIN, Star.StarType.TWO, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE), 4);
    }
    
    @Test
    public void testConnectedComponents(){
        testClusteringApproach(new ConnectedComponents(Integer.MAX_VALUE, "", null, ClusteringOutputType.GRAPH), 3);
    }
    
    private void testClusteringApproach(AbstractParallelClustering clustering, int numClusters){
        Alignment alignment = getTestAlignment();
        
        Map<String, Set<Long>> actual = FamerClustering.getClusters(alignment, clustering, getTestExtractor());
            
        Set<Long> actualClusterIds = getAllClusterIds(actual);

        assertEquals(numClusters, actualClusterIds.size()); // 4 clusters
        Set<String> entities = alignment.getDistinctSourcesAsSet();
        entities.addAll(alignment.getDistinctTargetsAsSet());
        for(String k : actual.keySet()){
            assertTrue(entities.contains(k));
        }
    }
        
    private Set<Long> getAllClusterIds(Map<String, Set<Long>> mapping){
        Set<Long> clusterIds = new HashSet<>();
        for(Set<Long> set : mapping.values()){
            clusterIds.addAll(set);
        }
        return clusterIds;
    }
    
    @Test
    public void testDifferentClusteringOutputType(){
        Alignment alignment = getTestAlignment();
                
        Map<String, Set<Long>> one = FamerClustering.getClusters(alignment, 
                new Center(PrioritySelection.MIN, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE), getTestExtractor());
        Map<String, Set<Long>> two = FamerClustering.getClusters(alignment, 
                new Center(PrioritySelection.MIN, false, ClusteringOutputType.GRAPH_COLLECTION, Integer.MAX_VALUE), getTestExtractor());
        Map<String, Set<Long>> three = FamerClustering.getClusters(alignment, 
                new Center(PrioritySelection.MIN, false, ClusteringOutputType.VERTEX_SET, Integer.MAX_VALUE), getTestExtractor());
        
        assertEquals(one, two);
        assertEquals(two, three);
    }

    private Alignment getTestAlignment(){
        Alignment a = new Alignment();
        
        // based on 
        // https://git.informatik.uni-leipzig.de/dbs/FAMER/-/blob/master/famer-clustering/src/test/java/org/gradoop/famer/clustering/parallelClustering/center/CenterTest.java#L54
        
        //cluster 1
        a.add("http://a.com/v0", "http://b.com/v1", 0.9);
        //cluster 2
        a.add("http://a.com/v0", "http://b.com/v2", 0.1);
        a.add("http://a.com/v0", "http://b.com/v3", 0.01);
        a.add("http://b.com/v2", "http://b.com/v3", 0.5);
        a.add("http://b.com/v2", "http://b.com/v4", 0.5);
        a.add("http://b.com/v3", "http://b.com/v4", 0.7);
        //cluster 3
        a.add("http://a.com/v5", "http://a.com/v6", 0.8);
        a.add("http://a.com/v5", "http://b.com/v7", 0.6);
        a.add("http://a.com/v6", "http://b.com/v7", 0.5);
        //cluster 4
        a.add("http://a.com/v8", "http://a.com/v9", 0.8);
        
        return a;
    }
    
    @Test
    public void testTestExtractor(){
        Set<String> possibilities = new HashSet<>(Arrays.asList("a", "b"));
        
        DatasetIDExtractor e = getTestExtractor();
        for(Correspondence c : getTestAlignment()){
            assertTrue(possibilities.contains(e.getDatasetID(c.getEntityOne())));
            assertTrue(possibilities.contains(e.getDatasetID(c.getEntityTwo())));
        }
        assertEquals("a", e.getDatasetID("http://a.com/v0"));
        assertEquals("b", e.getDatasetID("http://b.com/v1"));
    }
    
    private DatasetIDExtractor getTestExtractor(){
        return new DatasetIDExtractorUrlPattern("http://", ".com", s->s);
    }
    
    private LogicalGraph getTestLogicalGraph(){
        // based on 
        // https://git.informatik.uni-leipzig.de/dbs/FAMER/-/blob/master/famer-clustering/src/test/java/org/gradoop/famer/clustering/parallelClustering/center/CenterTest.java#L54
        
        String graphString = "similarityGraph[" +
            "/* cluster 1 */" +
            "(v0 {id:0, " + GRAPH_LABEL + ":\"A\"})" +
            "(v1 {id:1, " + GRAPH_LABEL + ":\"B\"})" +
            "(v0)-[e0 {" + SIM_VALUE + ":0.9}]->(v1)" +
            "/* cluster 2 */" +
            "(v2 {id:2, " + GRAPH_LABEL + ":\"B\"})" +
            "(v3 {id:3, " + GRAPH_LABEL + ":\"B\"})" +
            "(v4 {id:4, " + GRAPH_LABEL + ":\"B\"})" +
            "(v0)-[e1 {" + SIM_VALUE + ":0.1}]->(v2)" +
            "(v0)-[e2 {" + SIM_VALUE + ":0.01}]->(v3)" +
            "(v2)-[e3 {" + SIM_VALUE + ":0.5}]->(v3)" +
            "(v2)-[e4 {" + SIM_VALUE + ":0.5}]->(v4)" +
            "(v3)-[e5 {" + SIM_VALUE + ":0.7}]->(v4)" +
            "/* cluster 3 */" +
            "(v5 {id:5, " + GRAPH_LABEL + ":\"A\"})" +
            "(v6 {id:6, " + GRAPH_LABEL + ":\"A\"})" +
            "(v7 {id:7, " + GRAPH_LABEL + ":\"B\"})" +
            "(v5)-[e6 {" + SIM_VALUE + ":0.8}]->(v6)" +
            "(v5)-[e7 {" + SIM_VALUE + ":0.6}]->(v7)" +
            "(v6)-[e8 {" + SIM_VALUE + ":0.5}]->(v7)" +
            "/* cluster 4 */" +
            "(v8 {id:8, " + GRAPH_LABEL + ":\"A\"})" +
            "(v9 {id:9, " + GRAPH_LABEL + ":\"A\"})" +
            "(v8)-[e9 {" + SIM_VALUE + ":0.8}]->(v9)" +
            "/* cluster 5 - single vertex */" +
            "(v10 {id:10, " + GRAPH_LABEL + ":\"B\"})" +
            "]";
         // LogicalGraph inputGraph = getLoaderFromString(graphString).getLogicalGraphByVariable("similarityGraph");
        
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        GradoopFlinkConfig config = GradoopFlinkConfig.createConfig(env);
        
        FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(config);
        loader.initDatabaseFromString(graphString);        
        LogicalGraph inputGraph = loader.getLogicalGraphByVariable("similarityGraph");
        return inputGraph;
    }
    
}
