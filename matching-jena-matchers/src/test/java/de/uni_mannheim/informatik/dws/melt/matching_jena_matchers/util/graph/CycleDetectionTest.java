package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
import org.jgrapht.alg.cycle.TarjanSimpleCycles;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.util.SupplierUtil;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;


public class CycleDetectionTest {
    
    @Test
    void CycleDetectionTest() {
        for(GnmRandomGraphGenerator<String, DefaultEdge> gen : Arrays.asList(
                new GnmRandomGraphGenerator<String, DefaultEdge>(10, 15, 1051802520),
                new GnmRandomGraphGenerator<String, DefaultEdge>(500, 500, 1234),
                new GnmRandomGraphGenerator<String, DefaultEdge>(100, 120, 1234))){
            
            Supplier<String> vSupplier = new Supplier<String>(){
                private int id = 0;
                @Override
                public String get(){ return "v" + id++; }
            };

            Graph<String, DefaultEdge> directedGraph = new DefaultDirectedGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);
            gen.generateGraph(directedGraph);

            List<List<String>> cycles = getCycleDectection(directedGraph).getCycles();

            List<List<String>> cyclesExpected = new JohnsonSimpleCycles<>(directedGraph).findSimpleCycles();        
            assertEquals(new HashSet<>(CycleDetection.normalizePaths(cyclesExpected)), new HashSet<>(cycles));  
        }   
    }
    
    private static CycleDetection<String> getCycleDectection(Graph<String, DefaultEdge> g){
        CycleDetection<String> c = new CycleDetection<>();        
        for(DefaultEdge e : g.edgeSet()){
            c.addEdge(g.getEdgeSource(e), g.getEdgeTarget(e));
        }
        return c;
    }
    
    
    public static void main(String[] args){
        
        Supplier<String> vSupplier = new Supplier<String>(){
            private int id = 0;
            @Override
            public String get(){ return "v" + id++; }
        };
        
        
        Graph<String, DefaultEdge> directedGraph = new DefaultDirectedGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);
        
        //GnmRandomGraphGenerator<String, DefaultEdge> gen = new GnmRandomGraphGenerator<>(100, 200, 12324);
        GnmRandomGraphGenerator<String, DefaultEdge> gen = new GnmRandomGraphGenerator<>(10000, 11600, 12324);
        //GnmRandomGraphGenerator<String, DefaultEdge> gen = new GnmRandomGraphGenerator<>(500, 800, 12324);
        //GnmRandomGraphGenerator<String, DefaultEdge> gen = new GnmRandomGraphGenerator<>(10, 15, 1051802520);
        System.out.println("Generate");
        gen.generateGraph(directedGraph);
        
        List<DirectedSimpleCycles<String, DefaultEdge>> algos = Arrays.asList(
                
                //new HawickJamesSimpleCycles<>(directedGraph),
                new JohnsonSimpleCycles<>(directedGraph),
                new SzwarcfiterLauerSimpleCycles<>(directedGraph),
                new TarjanSimpleCycles<>(directedGraph),
                //new TiernanSimpleCycles<>(directedGraph),
                
                new DirectedSimpleCycles<String, DefaultEdge>() {
                    @Override
                    public List<List<String>> findSimpleCycles() {
                        return getCycleDectection(directedGraph).getCycles();
                    }
                }
        );
        List<Set<List<String>>> results = new ArrayList<>();
        for(int i = 0;  i < 10; i++){
            System.out.println("Run " + i);
            for(DirectedSimpleCycles<String, DefaultEdge> algo : algos){
                System.out.println("start: " + algo.getClass().getSimpleName());
                long start = System.currentTimeMillis();
                List<List<String>> output = algo.findSimpleCycles();
                results.add(new HashSet<>(output));
                double diff = (System.currentTimeMillis() - start) / 1000.0;
                //System.out.println(algo.getClass().getSimpleName() + " (" + diff + " seconds): " + output + "    (" + output.size() + ")");
                System.out.println(algo.getClass().getSimpleName() + " (" + diff + " seconds): " + output.size() + "");
            }
        }
    }
}
