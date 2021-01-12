package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The TypeTransformerRegistry is a registry for TypeTransformer which can transform an objetc of one type/class to another.
 * 
 * There exists many implementations available but none of them fits ours needs.
 * Nevertheless we list them here:
 * <ul>
 * <li>org.apache.commons.beanutils.ConvertUtilsBean (only target type and not source type) - from documentation: converting String scalar values to objects of the specified Class</li>
 * <li>org.apache.commons.collections4.Transformer - <a href="https://www.tutorialspoint.com/commons_collections/commons_collections_transforming_objects.htm">converts elements in lists</a></li>
 * <li><a href="https://guava.dev/releases/16.0/api/docs/com/google/common/base/Converter.html">com.google.common.base.Converter</a> - used for converting back and forth between different representations of the same information</li>
 * <li><a href="https://github.com/cchabanois/transmorph">transmorph</a></li>
 * <li><a href="http://morph.sourceforge.net">morph</a></li>
 * <li><a href="https://commons.apache.org/sandbox/commons-convert/">apache commons convert</a> - exactly what we need but only in sandbox and not properties</li>
 * </ul>
 * <a href="https://stackoverflow.com/questions/1432764/any-tool-for-java-object-to-object-mapping"> and many more</a>.
 */
public class TypeTransformerRegistry {
    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeTransformerRegistry.class);
    
    /**
     * The map which contains all transformers.
     */
    private static final Map<Class<?>, Map<Class<?>,Set<TypeTransformer>>> TRANFORMERS = new HashMap<>();
    static{
        //initialize TRANFORMERS
        
        ServiceLoader<TypeTransformerLoader> loaders = ServiceLoader.load(TypeTransformerLoader.class);
        for(TypeTransformerLoader l : loaders){
            l.registerTypeTransformers();
        }
        
        ServiceLoader<TypeTransformer> typeTransformers = ServiceLoader.load(TypeTransformer.class);
        for(TypeTransformer t : typeTransformers){
            addTransformer(t);
        }
    }
    
    /**
     * The additional tranformation cost (determined by the environment variable MELT_TRANSFORMATION_HIERARCHY_COST )to add
     * if hierarchy is allowed e.g. when we have a {@link TypeTransformer} between
     * A (source) and B(target) and we allow hierarchy then it is also possible to transform between any subclass of A and any superclass of B.
     * The HIERARCHY_TRANSFORMATION_COST is added to the initial transformaion cost with each layer in the class hierarchy.
     * If the number is smaller than zero (e.g. -1), then the hierarchy is disabled (which is the default).
     */
    private static final int HIERARCHY_TRANSFORMATION_COST = getHierarchyTransformationCost();
    private static int getHierarchyTransformationCost(){
        String cost = System.getProperty("MELT_TRANSFORMATION_HIERARCHY_COST", "-1");
        try{
            return Integer.parseInt(cost);
        }catch(NumberFormatException ex){
            LOGGER.error("Could not parse the number given by MELT_TRANSFORMATION_HIERARCHY_COST which is {}. Use default which is -1",
                    cost, ex);
            return -1;
        }
    }
    
    /**
     * If true, then the tranformation can be performed with multiple steps ({@link TypeTransformer}).
     * If false, only one {@link TypeTransformer} is allowed to be used for a transformation.
     * It is set through the environment variable MELT_TRANSFORMATION_ALLOW_MULTI_STEP .
     * The default is true.
     * If too many transformers are used, then settings this property to false might help.
     */
    private static final boolean ALLOW_MULTI_STEP = Boolean.parseBoolean(System.getProperty("MELT_TRANSFORMATION_ALLOW_MULTI_STEP", "true"));

    
    
    public static void addTransformer(TypeTransformer transformer){
        TRANFORMERS.computeIfAbsent(transformer.getSourceType(), __-> new HashMap<>())
                .computeIfAbsent(transformer.getTargetType(), __-> new HashSet<>())
                .add(transformer);
    }
    
    public static void removeTransformer(TypeTransformer transformer){
        Map<Class<?>,Set<TypeTransformer>> map = TRANFORMERS.get(transformer.getSourceType());
        if(map != null){
            Set<TypeTransformer> set = map.get(transformer.getTargetType());
            if(set != null){
                set.remove(transformer);
            }
        }
    }
    
    /**
     * Removes all transformers. Use it with care.
     */
    public static void clear(){
        TRANFORMERS.clear();
    }
    
    public static ObjectTransformationRoute transformObject(Object source, Class<?> target){
        return transformObject(Arrays.asList(source), target, new Properties());
    }
    public static ObjectTransformationRoute transformObject(Object source, Class<?> target, Properties parameters){
        return transformObject(Arrays.asList(source), target, parameters);
    }
    
    public static ObjectTransformationRoute transformObject(Iterable<Object> sources, Class<?> target, Properties parameters){
        return transformObject(sources, target, parameters, HIERARCHY_TRANSFORMATION_COST, ALLOW_MULTI_STEP);
    }
    
    public static ObjectTransformationRoute transformObject(Iterable<Object> sources, Class<?> target, Properties parameters, int hierarchyTransformationCost, boolean allowMultiStep){
        Map<Class<?>, Object> mapping = new HashMap<>();
        for(Object o : sources){
            mapping.put(o.getClass(), o); // override if multiple object of the same class appears
        }
        TransformationRoute route = transformClass(mapping.keySet(), target, parameters, hierarchyTransformationCost, allowMultiStep);
        if(route == null)
            return null;
        return new ObjectTransformationRoute(route, mapping.get(route.getSource()));
    }
    
    public static TransformationRoute transformClass(Class<?> source, Class<?> target){
        return transformClass(source, target, new Properties());
    }
    
    public static TransformationRoute transformClass(Class<?> source, Class<?> target, Properties parameters){
        return transformClass(Arrays.asList(source), target, parameters, HIERARCHY_TRANSFORMATION_COST, ALLOW_MULTI_STEP);
    }
    
    public static TransformationRoute transformClass(Iterable<Class<?>> sources, Class<?> target, Properties parameters){
        return transformClass(sources, target, parameters, HIERARCHY_TRANSFORMATION_COST, ALLOW_MULTI_STEP);
    }
    
    /**
     * Returns type transformers for one of the source classes to the target class. 
     * @param sources the iterable of source classes
     * @param target the target class
     * @param parameters the parameters which are forwarded to each type transformer
     * @param hierarchyTransformationCost negative value (below zero) if the hierarchy is not allowed, otherwise the cost for each subclass to superclass relation - see also {@link TypeTransformerRegistry#HIERARCHY_TRANSFORMATION_COST}
     * @param allowMultiStep allow multiple type transformers - see also {@link TypeTransformerRegistry#ALLOW_MULTI_STEP}
     * @return null if there is no path, otherwise instance of TransformationRoute (which can contain no transformers, when the source is a subclass of target class)
     */
    public static TransformationRoute transformClass(Iterable<Class<?>> sources, Class<?> target, Properties parameters, int hierarchyTransformationCost, boolean allowMultiStep){
        if(allowMultiStep == false)
            return transformInOneStep(sources, target, parameters, hierarchyTransformationCost);
        
        //simple non performant Dijkstra
        Set<Class<?>> settledNodes = new HashSet<>();
        Set<Class<?>> unSettledNodes = new HashSet<>();
        Map<Class<?>, TransformationEdge> predecessors = new HashMap<>();
        Map<Class<?>, Integer> distances = new HashMap<>();
        
        for(Class<?> sourceClass : sources){
            for(Entry<Class<?>, Integer> e : getAllSuperClassesAndIterfacesWithCost(sourceClass, hierarchyTransformationCost).entrySet()){
                distances.put(e.getKey(), e.getValue());
                unSettledNodes.add(e.getKey());
                if(e.getKey() != sourceClass){
                    predecessors.put(e.getKey(), new TransformationEdge(sourceClass, e.getKey(), null, e.getValue()));
                }
            }
        }
        
        while (unSettledNodes.size() > 0) {
            Class<?> node = getMinimum(distances, unSettledNodes);
            if(node.equals(target)){                
                //search path back starting from target
                List<TypeTransformer> transformers = new ArrayList();
                TransformationEdge previousEdge;
                Class<?> tmpNode = node;
                while((previousEdge = predecessors.get(tmpNode)) != null){
                    if(previousEdge.getTransformer() != null)
                        transformers.add(previousEdge.getTransformer());
                    tmpNode = previousEdge.getSource();
                }
                Collections.reverse(transformers);
                return new TransformationRoute(tmpNode, target, transformers, distances.get(node));
            }
            
            settledNodes.add(node);
            unSettledNodes.remove(node);
            
            //findMinimalDistances
            int distanceNode = distances.getOrDefault(node, Integer.MAX_VALUE);
            
            for(Entry<Class<?>, Set<TypeTransformer>> targetClassToTransformers : TRANFORMERS.getOrDefault(node, new HashMap<>()).entrySet()){
                for(Entry<Class<?>, Integer> targetClassToHierarchyCost : getAllSuperClassesAndIterfacesWithCost(targetClassToTransformers.getKey(), hierarchyTransformationCost).entrySet()){
                    Class<?> targetClazz = targetClassToHierarchyCost.getKey();
                    if(settledNodes.contains(targetClazz))
                        continue;
                    for(TypeTransformer transformer : targetClassToTransformers.getValue()){
                        int edgeCost = transformer.getTransformationCost(parameters) + targetClassToHierarchyCost.getValue();
                        int newCost = distanceNode + edgeCost;
                        if(distances.getOrDefault(targetClazz, Integer.MAX_VALUE) > newCost){
                            distances.put(targetClazz, newCost);
                            unSettledNodes.add(targetClazz);
                            predecessors.put(targetClazz, new TransformationEdge(node, targetClazz, transformer, edgeCost));
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private static Class<?> getMinimum(Map<Class<?>, Integer> distances, Set<Class<?>> vertexes) {
        Class<?> minimum = null;
        int minDistance = Integer.MAX_VALUE;
        for (Class<?> vertex : vertexes) {
            int d = distances.getOrDefault(vertex, Integer.MAX_VALUE);
            if (d < minDistance) {
                minimum = vertex;
                minDistance = d;
            }
        }
        return minimum;
    }
    
    
    private static TransformationRoute transformInOneStep(Iterable<Class<?>> sources, Class<?> target, Properties parameters, int hierarchyTransformationCost){
        List<TransformationRoute> transformationRoutes = new ArrayList();
        for(Class<?> src : sources){
            for(Entry<Class<?>, Integer> sourceHierarchy : getAllSuperClassesAndIterfacesWithCost(src, hierarchyTransformationCost).entrySet()){
                if(sourceHierarchy.getKey() == target){
                    transformationRoutes.add(new TransformationRoute(src, target, new ArrayList(), sourceHierarchy.getValue()));
                }
                for(Entry<Class<?>, Set<TypeTransformer>> targetToTransformers : TRANFORMERS.getOrDefault(sourceHierarchy.getKey(), new HashMap<>()).entrySet()){
                    Integer targetHierarchyCost = getAllSuperClassesAndIterfacesWithCost(targetToTransformers.getKey(), hierarchyTransformationCost).get(target);
                    if(targetHierarchyCost != null){
                        for(TypeTransformer transformer : targetToTransformers.getValue()){
                            transformationRoutes.add(new TransformationRoute(
                                    src, 
                                    target, 
                                    Arrays.asList(transformer), 
                                    sourceHierarchy.getValue() + transformer.getTransformationCost(parameters) +  targetHierarchyCost));
                        }
                    }
                }
            }
        }
        if(transformationRoutes.isEmpty())
            return null;
        transformationRoutes.sort(Comparator.comparing(TransformationRoute::getCost));
        return transformationRoutes.get(0);
    }
    
    /**
     * Given a class return all superclasses and interfaces except the Object class (which would be too generic).
     * The value of the map is multiplied with the hierarchyCost
     * @param clazz the class to start searching
     * @param hierarchyCost the hierarchy cost which is multiplied with every level
     * @return a map which contains all superclasses and interfaces with corresponding depth
     */
    public static Map<Class<?>, Integer> getAllSuperClassesAndIterfacesWithCost(Class<?> clazz, int hierarchyCost){
        if(hierarchyCost < 0){
            Map<Class<?>, Integer> newMap = new HashMap(1);
            newMap.put(clazz, 0);
            return newMap;
        }
        Map<Class<?>, Integer> map = getAllSuperClassesAndIterfaces(clazz);
        Map<Class<?>, Integer> newMap = new HashMap(map.size());
        for(Entry<Class<?>, Integer> e : map.entrySet()){
            newMap.put(e.getKey(), e.getValue() * hierarchyCost);
        }
        return newMap;
    }
    
    /**
     * Cache for Superclasses and interfaces for a given class.
     * Since tehre are ussually not so many classes, this information can directly be cache without much memory consumption.
     */
    private static Map<Class<?>, Map<Class<?>, Integer>> SUPER_CLASSES_CACHE = new HashMap();

    /**
     * Given a class return all superclasses and interfaces except the Object class (which woul be too generic).
     * @param clazz the class to start searching
     * @return a map which contains all superclasses and interfaces with corresponding depth
     */
    public static Map<Class<?>, Integer> getAllSuperClassesAndIterfaces(Class<?> clazz){
        Map<Class<?>, Integer> depths = SUPER_CLASSES_CACHE.get(clazz);
        if(depths != null)
            return depths; //use cache
        
        //breath first search
        depths = new HashMap<>();
        Queue<Class<?>> queue = new LinkedList<>();        
        queue.add(clazz);
        depths.put(clazz, 0);        
        while(!queue.isEmpty()){
            Class<?> current = queue.poll();            
            for (Class<?> next : current.getInterfaces()) {
                if (next != null && !depths.containsKey(next)) {
                    depths.put(next, depths.get(current) + 1);
                    queue.add(next);
                }
            }
            Class<?> next = current.getSuperclass();
            if (next != null && !depths.containsKey(next) && next != Object.class ) {
                depths.put(next, depths.get(current) + 1);
                queue.add(next);
            }
        }
        SUPER_CLASSES_CACHE.put(clazz, depths);
        return depths;
    }
    
}

class TransformationEdge{
    private Class<?> source;
    private Class<?> target;
    private TypeTransformer transformer;
    private int cost;

    public TransformationEdge(Class<?> source, Class<?> target, TypeTransformer transformer, int cost) {
        this.source = source;
        this.target = target;
        this.transformer = transformer;
        this.cost = cost;
    }
    
    public Class<?> getSource() {
        return source;
    }

    public Class<?> getTarget() {
        return target;
    }

    public TypeTransformer getTransformer() {
        return transformer;
    }

    public int getCost() {
        return cost;
    }
}
