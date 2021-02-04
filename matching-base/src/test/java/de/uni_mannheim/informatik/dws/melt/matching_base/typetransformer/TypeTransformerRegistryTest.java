package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.function.Supplier;
import javassist.CannotCompileException;
import javassist.ClassPool;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TypeTransformerRegistryTest {    
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeTransformerRegistryTest.class);
        
    @Test
    public void getAllSuperClassesAndIterfacesTest(){
        Map<Class<?>, Integer> map = TypeTransformerRegistry.getAllSuperClassesAndIterfaces(SourceSubClass.class);
        assertEquals(0, map.getOrDefault(SourceSubClass.class, -1));
        assertEquals(1, map.getOrDefault(SourceSuperClass.class, -1));
        assertEquals(1, map.getOrDefault(SourceInterface.class, -1));
        assertFalse(map.containsKey(Object.class));
        assertEquals(3, map.size());
    }
    
    @Test
    public void getAllSuperClassesAndIterfacesWithCostTest(){
        Map<Class<?>, Integer> map = TypeTransformerRegistry.getAllSuperClassesAndIterfacesWithCost(SourceSubClass.class, 5);
        assertEquals(0, map.getOrDefault(SourceSubClass.class, -1));
        assertEquals(5, map.getOrDefault(SourceSuperClass.class, -1));
        assertEquals(5, map.getOrDefault(SourceInterface.class, -1));
    }
    
    
    @Test
    public void transformClassTest() throws Exception, Exception, Exception{
        TypeTransformerRegistry.clear();
        TypeTransformerRegistry.addTransformer(new TypeTransformerForTest(SourceSuperClass.class, TargetSubClass.class, 20));

        for(Boolean b : Arrays.asList(true, false)){
            TransformationRoute r = TypeTransformerRegistry.transformClassMultipleRepresentations(
                Arrays.asList(SourceSubClass.class), 
                TargetSubClass.class, 
                new Properties(), 
                10, b);
        
            assertEquals(SourceSubClass.class, r.getSource());
            assertEquals(TargetSubClass.class, r.getTarget());
            assertEquals(30, r.getCost());
            assertEquals(1, r.getTransformations().size());
            
            //no hierarchy allowed
            r = TypeTransformerRegistry.transformClassMultipleRepresentations(
                Arrays.asList(SourceSubClass.class), 
                TargetSubClass.class, 
                new Properties(), 
                -1, b);
        
            assertNull(r);
        }
    }
    
    @Test
    public void threeHopsTest() throws Exception{
        TypeTransformerRegistry.clear();
        TypeTransformerRegistry.addTransformer(new TypeTransformerForTest(SourceSuperClass.class, MiddleClass.class));        
        TypeTransformerRegistry.addTransformer(new TypeTransformerForTest(MiddleClass.class, TargetSubClass.class));
        
        SourceSubClass s = new SourceSubClass();
        ObjectTransformationRoute route = TypeTransformerRegistry.transformObjectMultipleRepresentations(
                Arrays.asList(s), TargetSubClass.class, new Properties(),-1, true);
        assertNull(route);
        
        route = TypeTransformerRegistry.transformObjectMultipleRepresentations(
                Arrays.asList(s, new Object()), TargetSubClass.class, new Properties(),5, true);
        assertEquals(s, route.getInitialObject());
        assertNotNull(route.getTransformedObject());
        assertEquals(TargetSubClass.class, route.getTransformedObject().getClass());
    }
    
    
    @Test
    public void testHierarchy() throws Exception{
        SourceSubClass c = new SourceSubClass();
        assertNull(TypeTransformerRegistry.transformObjectMultipleRepresentations(Arrays.asList(c), SourceSuperClass.class, new Properties(), -1, true));
        assertNull(TypeTransformerRegistry.transformObjectMultipleRepresentations(Arrays.asList(c), SourceSuperClass.class, new Properties(), -1, false));
        
        for(Boolean b : Arrays.asList(true, false)){
            ObjectTransformationRoute r = TypeTransformerRegistry.transformObjectMultipleRepresentations(Arrays.asList(c), SourceSuperClass.class, new Properties(), 10, true);

            assertNotNull(r);
            assertEquals(SourceSubClass.class, r.getSource());
            assertEquals(SourceSuperClass.class, r.getTarget());
            assertEquals(10, r.getCost());
            assertEquals(0, r.getTransformations().size());
            
            Object transformed = r.getTransformedObject();
            assertNotNull(transformed);            
            assertTrue(transformed instanceof SourceSuperClass);
        }
    }
    
    
    @Test
    public void testProperties(){
        TypeTransformerRegistry.clear();        
        TypeTransformerRegistry.addTransformer(new TypeTransformerForTest(SourceSubClass.class, MiddleClass.class, 10));        
        TypeTransformerRegistry.addTransformer(new TypeTransformerForTest(MiddleClass.class, TargetSubClass.class, 10));
        TypeTransformerRegistry.addTransformer(new AbstractTypeTransformer(SourceSubClass.class, TargetSubClass.class) {
            @Override
            public Object transform(Object value, Properties parameters) throws Exception {
                if(SourceSubClass.class.isInstance(value)){
                    return new TargetSubClass();
                }
                return null;
            }
            
            @Override
            public int getTransformationCost(Properties parameters) {
                if(parameters.contains("test")){
                    return 5;
                }else{
                    return 40;
                }
            }
        });
        
        TransformationRoute r = TypeTransformerRegistry.transformClassMultipleRepresentations(
                Arrays.asList(SourceSubClass.class), 
                TargetSubClass.class, 
                new Properties(), 
                -1, true);
        
        assertEquals(SourceSubClass.class, r.getSource());
        assertEquals(TargetSubClass.class, r.getTarget());
        assertEquals(20, r.getCost());
        assertEquals(2, r.getTransformations().size());
        
        Properties params = new Properties();
        params.put("test", "test");
        
        r = TypeTransformerRegistry.transformClassMultipleRepresentations(
                Arrays.asList(SourceSubClass.class), 
                TargetSubClass.class, 
                params, 
                -1, true);
        
        assertEquals(SourceSubClass.class, r.getSource());
        assertEquals(TargetSubClass.class, r.getTarget());
        assertEquals(5, r.getCost());
        assertEquals(1, r.getTransformations().size());
    }
    
    @Test
    public void testNullValues(){
        assertEquals(null, TypeTransformerRegistry.transformObject(null, null));
        assertEquals(null, TypeTransformerRegistry.transformObject(null, List.class));
        assertEquals(null, TypeTransformerRegistry.transformObject(new Object(), null));
        
        assertEquals(null, TypeTransformerRegistry.transformObjectMultipleRepresentations(null, null));
        assertEquals(null, TypeTransformerRegistry.transformObjectMultipleRepresentations(null, List.class));
        assertEquals(null, TypeTransformerRegistry.transformObjectMultipleRepresentations(Arrays.asList(), null));
        assertEquals(null, TypeTransformerRegistry.transformObjectMultipleRepresentations(Arrays.asList(), List.class));
        
        
        assertEquals(null, TypeTransformerRegistry.transformClass(null, null));
        assertEquals(null, TypeTransformerRegistry.transformClass(null, List.class));
        assertEquals(null, TypeTransformerRegistry.transformClass(List.class, null));
        
        assertEquals(null, TypeTransformerRegistry.transformClassMultipleRepresentations(null, null));
        assertEquals(null, TypeTransformerRegistry.transformClassMultipleRepresentations(null, List.class));
        assertEquals(null, TypeTransformerRegistry.transformClassMultipleRepresentations(Arrays.asList(), null));
        assertEquals(null, TypeTransformerRegistry.transformClassMultipleRepresentations(Arrays.asList(), List.class));
    }
    
    
    /**
     * This is a non repeatable test because it creates random graphs.
     * But it checks if the implementation find the correct path.
     */
    //@Test
    public void testDijkstraSearch(){        
        for(int k=0; k < 10; k++){
            DirectedWeightedMultigraph<Class<?>, DefaultWeightedEdge> graph = generateRandomGraph(200,500);
            compareImplementationsRandomSourceTarget(graph);
        }
    }
    
    //@Test
    //public void testClassSupplier(){
    //    ClassSupplier s = new ClassSupplier();
    //    assertNotEquals(s.get(), s.get());
    //    assertEquals(ClassSupplier.createClass("blub"), ClassSupplier.createClass("blub"));
    //}
    
    
    //Helper methods
    
    private void compareImplementationsRandomSourceTarget(DirectedWeightedMultigraph<Class<?>, DefaultWeightedEdge> graph){
        List<Class<?>> vertices = new ArrayList(graph.vertexSet());
        Collections.shuffle(vertices);
        compareImplementations(graph, vertices.get(0), vertices.get(1));
    }
    
    private void compareImplementations(DirectedWeightedMultigraph<Class<?>, DefaultWeightedEdge> graph, Class<?> source, Class<?> target){
        DijkstraShortestPath<Class<?>, DefaultWeightedEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        GraphPath<Class<?>, DefaultWeightedEdge> path = dijkstraAlg.getPath(source, target);
        
        TypeTransformerRegistry.clear();
        updateTypeTranformerRegistryWithGraph(graph);        
        TransformationRoute route = TypeTransformerRegistry.transformClass(source, target);
        
        if(path == null){
            assertNull(route);
            //System.out.println("Path null");
        }else{
            //System.out.println("Path with edges: " + path.getEdgeList().size());
            assertEquals((int)path.getWeight(), route.getCost());
            assertEquals(path.getStartVertex(), route.getSource());
            assertEquals(path.getEndVertex(), route.getTarget());
            assertEquals(path.getEdgeList().size(), route.getTransformations().size());
            for(int i = 0; i < path.getEdgeList().size(); i++){
                DefaultWeightedEdge edge = path.getEdgeList().get(i);
                TypeTransformer t = route.getTransformations().get(i);
                //if(graph.getEdgeSource(edge) != t.getSourceType() || graph.getEdgeTarget(edge) != t.getTargetType()){
                //    System.out.println("BLA");
                //}
                assertEquals(graph.getEdgeSource(edge), t.getSourceType());
                assertEquals(graph.getEdgeTarget(edge), t.getTargetType());
                assertEquals((int)graph.getEdgeWeight(edge), t.getTransformationCost(new Properties()));
            }
        }
    }
    
    private DirectedWeightedMultigraph<Class<?>, DefaultWeightedEdge> generateRandomGraph(int vertexCount, int edgeCount){
        DirectedWeightedMultigraph<Class<?>, DefaultWeightedEdge> graph =
            new DirectedWeightedMultigraph<>(new ClassSupplier(), SupplierUtil.createDefaultWeightedEdgeSupplier());
        
        Random rnd = new Random();
        GnmRandomGraphGenerator generator = new GnmRandomGraphGenerator(vertexCount, edgeCount, 1324, false, true);
        generator.generateGraph(graph);
        
        for(DefaultWeightedEdge e : graph.edgeSet()){
            graph.setEdgeWeight(e, rnd.nextInt(50));
        }
        return graph;
    }
    
    private void updateTypeTranformerRegistryWithGraph(DirectedWeightedMultigraph<Class<?>, DefaultWeightedEdge> graph){
        for(DefaultWeightedEdge edge : graph.edgeSet()){
            Class<?> source = graph.getEdgeSource(edge);
            Class<?> target = graph.getEdgeTarget(edge);
            double weight = graph.getEdgeWeight(edge);            
            TypeTransformerRegistry.addTransformer(new TypeTransformerForTest(source, target, (int)weight));
        }
    }
}


class ClassSupplier implements Supplier<Class<?>>{
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassSupplier.class);
    
    private int id = 0;

    @Override
    public Class<?> get() {
        return createClass("MeltClass" + id++);
    }
    
    private static Map<String, Class<?>> cache = new HashMap<>();

    public static Class<?> createClass(String name){
        Class<?> c = cache.get(name);
        if(c != null)
            return c;
        ClassPool cp = ClassPool.getDefault();
        try {
            c = cp.makeClass(name).toClass();
            cache.put(name, c);
            return c;
        } catch (CannotCompileException ex) {
            LOGGER.error("Canot create class", ex);
            return null;
        }
    }
}

//Test classes
    
class SourceSuperClass{ }
interface SourceInterface {}
class SourceSubClass extends SourceSuperClass implements SourceInterface { }

class MiddleClass{ }

class TargetSuperClass{ }
interface TargetInterface {}
class TargetSubClass extends TargetSuperClass implements TargetInterface { }