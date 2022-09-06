package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.graph;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.SKOS;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
import org.jgrapht.alg.cycle.TarjanSimpleCycles;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CycleRemovalTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CycleRemovalTest.class);
    
    @Test
    void cyclesRemovalTest() {
        Map<String, Set<String>> graph = new HashMap<>();
        addPath(graph, "A", "B", "C");
        addPath(graph, "X", "Z", "C");
        addPath(graph, "Y", "Z", "C");
        addPath(graph, "C", "Y");//back edge
        
        CycleRemoval<String> removal = new CycleRemoval<>(graph);
        //System.out.println(removal.getEdgesToBeRemoved());
        assertTrue(removal.getEdgesToBeRemoved().contains(new SimpleEntry<>("C", "Y")));        
    }
    
    @Test
    void cyclesRemovalTestTwoCycles() {
        Map<String, Set<String>> graph = new HashMap<>();
        addPath(graph, "A", "B", "C", "D");
        addPath(graph, "E", "B");
        addPath(graph, "F", "H", "C");
        addPath(graph, "G", "H");
        addPath(graph, "I", "K", "M", "D");
        addPath(graph, "J", "L", "M");
        
        //Back edge
        addPath(graph, "D", "A");
        addPath(graph, "C", "H");
        
        Set<Entry<String, String>> edges = new CycleRemoval<>(graph).getEdgesToBeRemoved();
        //System.out.println(edges);
        assertEquals(2, edges.size());
        assertTrue(edges.contains(new SimpleEntry<>("D", "A")));
        assertTrue(edges.contains(new SimpleEntry<>("C", "H")));
    }
    
    private static void addPath(Map<String, Set<String>> graph, String... pathNodes){
        for(int i = 1; i < pathNodes.length; i++){
            graph.computeIfAbsent(pathNodes[i-1], __->new HashSet<>()).add(pathNodes[i]);
        }
    }
    
    public static void main(String[] args){
        Map<String, Set<String>> graph = getKGCategoryGraph();
        
        DotGraphUtil.writeDirectedGraphToDotFile(new File("categoryTest.dot"), graph, r->r, "rankdir=BT;"); 
        CycleRemoval<String> removal = new CycleRemoval<>(graph);
        //removal.getEdgesToBeRemoved();        
        Map<String, Set<String>> cycleFreeGraph = removal.getCycleFreeGraph();
        DotGraphUtil.writeDirectedGraphToDotFile(new File("categoryTestCycleFree.dot"), cycleFreeGraph, r->r, "rankdir=BT;");
    }

    private static Map<String, Set<String>> getKGCategoryGraph(){
        OntModel m = TrackRepository.Knowledgegraph.V3.getTestCase("memoryalpha-memorybeta").getTargetOntology(OntModel.class);
        List<Individual> instances = m.listIndividuals().toList();
        Individual i = instances.get(new Random().nextInt(instances.size()));
        //Individual i = m.getIndividual("http://dbkwik.webdatacommons.org/memory-beta.wikia.com/resource/USS_Merrimac_(NCC-1715)");
        LOGGER.info("Instance: {}", i);
        Map<String, Set<String>> graph = new HashMap<>();

        Set<Resource> visted = new HashSet<>();
        Queue<Resource> q = new LinkedList<>();
        Iterator<Resource> resources = i.listPropertyValues(DCTerms.subject).filterKeep(node -> node.isResource()).mapWith(node->node.asResource());
        while(resources.hasNext())
            q.add(resources.next());
        while(!q.isEmpty()){
            Resource r = q.poll();
            visted.add(r);
            for(Resource succ : getObjectAsResource(r.listProperties(SKOS.broader))){
                if(visted.contains(succ) == false){
                    q.add(succ);
                }
                String src = DotGraphUtil.makeQuotedNodeID(URIUtil.getUriFragment(r.getURI()).substring(9));
                String tgt = DotGraphUtil.makeQuotedNodeID(URIUtil.getUriFragment(succ.getURI()).substring(9));
                graph.computeIfAbsent(src, __-> new HashSet<>()).add(tgt);
            }
        }
        return graph;
    }
    
    private static List<Resource> getObjectAsResource(StmtIterator i){
        List<Resource> resources = new ArrayList<>();
        while(i.hasNext()){
            Statement s = i.next();
            if(s.getObject().isURIResource())
                resources.add(s.getObject().asResource());
        }
        return resources;
    }
}
