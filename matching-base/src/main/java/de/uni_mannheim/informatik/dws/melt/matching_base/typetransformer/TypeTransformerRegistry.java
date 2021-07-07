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
import java.util.logging.Level;
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
    private static final Map<Class<?>, Map<Class<?>,Set<TypeTransformer<?,?>>>> TRANFORMERS = new HashMap<>();
    static{
        //initialize TRANFORMERS
        addAllTransformersViaServiceRegistry();
    }
    
    /**
     * The additional tranformation cost (determined by the environment variable MELT_TRANSFORMATION_HIERARCHY_COST )to add
     * if hierarchy is allowed e.g. when we have a {@link TypeTransformer} between
     * A (source) and B(target) and we allow hierarchy then it is also possible to transform between any subclass of A and any superclass of B.
     * The HIERARCHY_TRANSFORMATION_COST is added to the initial transformaion cost with each layer in the class hierarchy.
     * If the number is smaller than zero (e.g. -1), then the hierarchy is disabled. Default: hierarchy enabled with 30 as cost.
     */
    private static final int HIERARCHY_TRANSFORMATION_COST = getHierarchyTransformationCost();
    private static int getHierarchyTransformationCost(){
        String cost = System.getProperty("MELT_TRANSFORMATION_HIERARCHY_COST", "30");
        try{
            return Integer.parseInt(cost);
        }catch(NumberFormatException ex){
            LOGGER.error("Could not parse the number given by MELT_TRANSFORMATION_HIERARCHY_COST which is {}. Use default which is 30.",
                    cost, ex);
            return 30;
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

    public static void addTransformer(TypeTransformer<?,?> transformer){
        TRANFORMERS.computeIfAbsent(transformer.getSourceType(), __-> new HashMap<>())
                .computeIfAbsent(transformer.getTargetType(), __-> new HashSet<>())
                .add(transformer);
    }
    
    public static void addAllTransformersViaServiceRegistry(){
        addAllTransformersViaServiceRegistry(Thread.currentThread().getContextClassLoader());
    }
    
    @SuppressWarnings("unchecked")
    public static void addAllTransformersViaServiceRegistry(ClassLoader classloader){
        ServiceLoader<TypeTransformerLoader> loaders = ServiceLoader.load(TypeTransformerLoader.class, classloader);
        for(TypeTransformerLoader l : loaders){
            l.registerTypeTransformers();
        }
        
        ServiceLoader<TypeTransformer> typeTransformers = ServiceLoader.load(TypeTransformer.class, classloader);
        for(TypeTransformer t : typeTransformers){
            addTransformer(t);
        }
    }
    
    private static final List<String> MELT_DEFAULT_TRANSFORMERS = Arrays.asList(
        //alignment
        "de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.typetransformation.Alignment2URLTransformer",
        "de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.typetransformation.URL2AlignmentTransformer",
        
        //properties
        "de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformers.URL2PropertiesTransformer",
        "de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformers.Properties2URLTransformer",
        
        //jena related
        "de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation.OntModel2URLTransformer",
        "de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation.URL2OntModelTransformer",
        "de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation.Model2OntModelTransformer",
        
        //owlapi related
        "de.uni_mannheim.informatik.dws.melt.matching_owlapi.typetransformation.URL2OWLOntology"
    );
    
    /**
     * This can be called in case the service registry does not work (in case of SEALS - due to the classloader).
     * It will add a static set of default transformers available in melt if the corresponding class is available in the classpath.
     */
    @SuppressWarnings("unchecked")
    public static void addMeltDefaultTransformers(){
        for(String className : MELT_DEFAULT_TRANSFORMERS){
            try {
                TypeTransformer<?,?> transformer = (TypeTransformer)Class.forName(className).newInstance();
                addTransformer(transformer);
            } catch (ClassNotFoundException ex) {
                LOGGER.info("Class {} is not added to the typetransformer because it is not available in the classpath. Nothing to worry about as long as the transformer is not needed.", className);
            }catch(InstantiationException | IllegalAccessException ex){
                LOGGER.info("Class {} can not be instantiated. Check that there is an empty constructor.", className);
            }
        }
        
    }
    
    public static void removeTransformer(TypeTransformer<?,?> transformer){
        Map<Class<?>,Set<TypeTransformer<?,?>>> map = TRANFORMERS.get(transformer.getSourceType());
        if(map != null){
            Set<TypeTransformer<?,?>> set = map.get(transformer.getTargetType());
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
    
    /**
     * Get all registered tranformers. For debugging purposes.
     * @return all registered transformers.
     */
    public static Set<TypeTransformer<?,?>> getAllRegisteredTypeTransformers(){
        Set<TypeTransformer<?,?>> transformers = new HashSet<>();
        for(Map<Class<?>,Set<TypeTransformer<?,?>>> entry : TRANFORMERS.values()){
            for(Set<TypeTransformer<?,?>> typetransformers : entry.values()){
                transformers.addAll(typetransformers);
            }
        }
        return transformers;
    }
    
    public static String getAllRegisteredTypeTransformersAsString(){
        Set<String> classes = new HashSet<>();
        for(TypeTransformer<?,?> o : getAllRegisteredTypeTransformers()){
            classes.add(o.getClass().getName());
        }
        return classes.toString();
    }
    
    
    
    public static <T> ObjectTransformationRoute<T> getObjectTransformationRoute(Object source, Class<T> target) throws TypeTransformationException{
        return TypeTransformerRegistry.getObjectTransformationRoute(source, target, new Properties());
    }
    public static <T> ObjectTransformationRoute<T> getObjectTransformationRoute(Object source, Class<T> target, Properties parameters) throws TypeTransformationException{
        return TypeTransformerRegistry.getObjectTransformationRoute(source, target, parameters, HIERARCHY_TRANSFORMATION_COST, ALLOW_MULTI_STEP);
    }
    public static <T> ObjectTransformationRoute<T> getObjectTransformationRoute(Object source, Class<T> target, Properties parameters, int hierarchyTransformationCost, boolean allowMultiStep) throws TypeTransformationException{
        if(source == null)
            return null;
        TransformationRoute<T> route = TypeTransformerRegistry.getClassTransformationRoute(source.getClass(), target, parameters, hierarchyTransformationCost, allowMultiStep);
        if(route == null)
            return null;
        return new ObjectTransformationRoute<>(route, source);
    }
    
    
    public static <T> ObjectTransformationRoute<T> getObjectTransformationRouteMultipleRepresentations(Iterable<Object> sources, Class<T> target) throws TypeTransformationException{
        return TypeTransformerRegistry.getObjectTransformationRouteMultipleRepresentations(sources, target, new Properties());
    }
    
    public static <T> ObjectTransformationRoute<T> getObjectTransformationRouteMultipleRepresentations(Iterable<Object> sources, Class<T> target, Properties parameters) throws TypeTransformationException{
        return TypeTransformerRegistry.getObjectTransformationRouteMultipleRepresentations(sources, target, parameters, HIERARCHY_TRANSFORMATION_COST, ALLOW_MULTI_STEP);
    }
    
    /**
     * Transforms an object with multiple representations to one class.This means, you have multiple objects representing the same information and want to choose the right one with the lowest transformation cost.
     * @param <T> the target type
     * @param sources the possible objects which all contains the same information (but in different classes).
     * @param target the target class in which the object should be converted.
     * @param parameters optional parameters for the conversion.
     * @param hierarchyTransformationCost hierarchy transformation cost: see {@link TypeTransformerRegistry#HIERARCHY_TRANSFORMATION_COST}
     * @param allowMultiStep allow multi step: see {@link TypeTransformerRegistry#ALLOW_MULTI_STEP}
     * @return ObjectTransformationRoute which contains the transformers as well as the source object. The actual transformation is not yet executed.
     * @throws TypeTransformationException in case no transformation route is found
     */
    public static <T> ObjectTransformationRoute<T> getObjectTransformationRouteMultipleRepresentations(Iterable<Object> sources, Class<T> target, Properties parameters, int hierarchyTransformationCost, boolean allowMultiStep) throws TypeTransformationException{
        if(sources == null)
            return null;
        Map<Class<?>, Object> mapping = new HashMap<>();
        for(Object o : sources){
            if(o == null)
                continue;
            mapping.put(o.getClass(), o); // override if multiple object of the same class appears
        }
        TransformationRoute<T> route = TypeTransformerRegistry.getClassTransformationRouteMultipleRepresentations(mapping.keySet(), target, parameters, hierarchyTransformationCost, allowMultiStep);
        if(route == null)
            return null;
        return new ObjectTransformationRoute<>(route, mapping.get(route.getSource()));
    }
    
    /*************************
     * getClassTransformationRoute section
     *************************/
    
    
    /**
     * Return the transformation from a given class to a target class.
     * @param <T> the target type
     * @param source the source class
     * @param target the target class
     * @return the transformation route
     * @throws TypeTransformationException if no route is found
     */
    public static <T> TransformationRoute<T> getClassTransformationRoute(Class<?> source, Class<T> target) throws TypeTransformationException{
        return getClassTransformationRoute(source, target, new Properties());
    }
    
    public static <T> TransformationRoute<T> getClassTransformationRoute(Class<?> source, Class<T> target, Properties parameters) throws TypeTransformationException{
        return getClassTransformationRoute(source, target, parameters, HIERARCHY_TRANSFORMATION_COST, ALLOW_MULTI_STEP);
    }
    
    public static <T> TransformationRoute<T> getClassTransformationRoute(Class<?> source, Class<T> target, Properties parameters, int hierarchyTransformationCost, boolean allowMultiStep) throws TypeTransformationException{
        return getClassTransformationRouteMultipleRepresentations(Arrays.asList(source), target, parameters, hierarchyTransformationCost, allowMultiStep);
    }
    
    
    public static <T> TransformationRoute<T> getClassTransformationRouteMultipleRepresentations(Iterable<Class<?>> sources, Class<T> target) throws TypeTransformationException{
        return getClassTransformationRouteMultipleRepresentations(sources, target, new Properties());
    }
    
    public static <T> TransformationRoute<T> getClassTransformationRouteMultipleRepresentations(Iterable<Class<?>> sources, Class<T> target, Properties parameters) throws TypeTransformationException{
        return getClassTransformationRouteMultipleRepresentations(sources, target, parameters, HIERARCHY_TRANSFORMATION_COST, ALLOW_MULTI_STEP);
    }
    
    /**
     * Returns type transformation route for one of the source classes to the target class.If no transformation is available, then a TypeTransformationException is thrown.
     * If no classes are gioven, then null is returned.
     * @param <T> the target type
     * @param sources the iterable of source classes
     * @param target the target class
     * @param parameters the parameters which are forwarded to each type transformer
     * @param hierarchyTransformationCost negative value (below zero) if the hierarchy is not allowed, otherwise the cost for each subclass to superclass relation - see also {@link TypeTransformerRegistry#HIERARCHY_TRANSFORMATION_COST}
     * @param allowMultiStep allow multiple type transformers - see also {@link TypeTransformerRegistry#ALLOW_MULTI_STEP}
     * @return null if there is no path, otherwise instance of TransformationRoute (which can contain no transformers, when the source is a subclass of target class)
     * @throws TypeTransformationException if no route is found
     */
    public static <T> TransformationRoute<T> getClassTransformationRouteMultipleRepresentations(Iterable<Class<?>> sources, Class<T> target, Properties parameters, int hierarchyTransformationCost, boolean allowMultiStep) throws TypeTransformationException{
        if(sources == null || target == null)
            return null;
        //transformation to object class always work - quick path
        if(target == Object.class){
            for(Class<?> sourceClass : sources){
                if(sourceClass != null)
                    return new TransformationRoute<>(sourceClass, target, new ArrayList<>(), 0);
            }
            return null;
        }
        if(allowMultiStep == false)
            return transformInOneStep(sources, target, parameters, hierarchyTransformationCost);
        
        //simple non performant Dijkstra
        Set<Class<?>> settledNodes = new HashSet<>();
        Set<Class<?>> unSettledNodes = new HashSet<>();
        Map<Class<?>, TransformationEdge> predecessors = new HashMap<>();
        Map<Class<?>, Integer> distances = new HashMap<>();
        
        for(Class<?> sourceClass : sources){
            if(sourceClass == null)
                continue;
            for(Entry<Class<?>, Integer> e : getAllSuperClassesAndIterfacesWithCost(sourceClass, hierarchyTransformationCost).entrySet()){
                distances.put(e.getKey(), e.getValue());
                unSettledNodes.add(e.getKey());
                if(e.getKey() != sourceClass){
                    predecessors.put(e.getKey(), new TransformationEdge(sourceClass, e.getKey(), null, e.getValue()));
                }
            }
        }
        
        if(unSettledNodes.isEmpty())
            return null;
        
        while (unSettledNodes.size() > 0) {
            Class<?> node = getMinimum(distances, unSettledNodes);
            if(node.equals(target)){                
                //search path back starting from target
                List<TypeTransformer<?,?>> transformers = new ArrayList<>();
                TransformationEdge previousEdge;
                Class<?> tmpNode = node;
                while((previousEdge = predecessors.get(tmpNode)) != null){
                    if(previousEdge.getTransformer() != null)
                        transformers.add(previousEdge.getTransformer());
                    tmpNode = previousEdge.getSource();
                }
                Collections.reverse(transformers);
                return new TransformationRoute<>(tmpNode, target, transformers, distances.get(node));
            }
            
            settledNodes.add(node);
            unSettledNodes.remove(node);
            
            //findMinimalDistances
            int distanceNode = distances.getOrDefault(node, Integer.MAX_VALUE);
            
            for(Entry<Class<?>, Set<TypeTransformer<?,?>>> targetClassToTransformers : TRANFORMERS.getOrDefault(node, new HashMap<>()).entrySet()){
                for(Entry<Class<?>, Integer> targetClassToHierarchyCost : getAllSuperClassesAndIterfacesWithCost(targetClassToTransformers.getKey(), hierarchyTransformationCost).entrySet()){
                    Class<?> targetClazz = targetClassToHierarchyCost.getKey();
                    if(settledNodes.contains(targetClazz))
                        continue;
                    for(TypeTransformer<?,?> transformer : targetClassToTransformers.getValue()){
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
        throw new TypeTransformationException("No transformation route is found between " + sources + " to " + target);
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
    
    
    private static <T> TransformationRoute<T> transformInOneStep(Iterable<Class<?>> sources, Class<T> target, Properties parameters, int hierarchyTransformationCost) throws TypeTransformationException{
        //target is not null because this method is called from transformClassMultipleRepresentations
        List<TransformationRoute<T>> transformationRoutes = new ArrayList<>();
        for(Class<?> src : sources){
            if(src == null)
                continue;
            for(Entry<Class<?>, Integer> sourceHierarchy : getAllSuperClassesAndIterfacesWithCost(src, hierarchyTransformationCost).entrySet()){
                if(sourceHierarchy.getKey() == target){
                    transformationRoutes.add(new TransformationRoute<>(src, target, new ArrayList<>(), sourceHierarchy.getValue()));
                }
                for(Entry<Class<?>, Set<TypeTransformer<?,?>>> targetToTransformers : TRANFORMERS.getOrDefault(sourceHierarchy.getKey(), new HashMap<>()).entrySet()){
                    Integer targetHierarchyCost = getAllSuperClassesAndIterfacesWithCost(targetToTransformers.getKey(), hierarchyTransformationCost).get(target);
                    if(targetHierarchyCost != null){
                        for(TypeTransformer<?,?> transformer : targetToTransformers.getValue()){
                            transformationRoutes.add(new TransformationRoute<>(
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
            throw new TypeTransformationException("No transformation route is found between " + sources + " to " + target);
        transformationRoutes.sort(Comparator.comparing(TransformationRoute::getCost));
        return transformationRoutes.get(0);
    }
    
    /****************************************************
     * Section getTransformedListOfObjectsMultipleRepresentations
     ****************************************************/
    
    /**
     * Directly get the transformed list of objects or null if something went wrong.
     * @param <T> the type
     * @param sourceObjects the objects which all represent the same information. To this set, the transformed list of objects will be added.
     * @param targetType the tyoe of class to transform each object in the list to 
     * @param transformationProperties additional properties.
     * @return the transformed list of objects or null
     * @throws TypeTransformationException in case no transformation route is found
     */
    public static <T> List<T> getTransformedListOfObjectsMultipleRepresentations(List<Set<Object>> sourceObjects, Class<T> targetType, Properties transformationProperties) throws TypeTransformationException{
        List<T> transformedObjects = new ArrayList<>(sourceObjects.size());
        for(Set<Object> representations : sourceObjects){
            T transformed = getTransformedObjectMultipleRepresentations(representations, targetType, transformationProperties);
            if(transformed == null)
                return null;
            transformedObjects.add(transformed);
        }
        return transformedObjects;
        
        /*
        //this was for set<list<object>> as first parameter
        int maxNumberOfElements = -1;
        for(List<Object> list : sourceObjects){
            if(maxNumberOfElements < 0){
                maxNumberOfElements = list.size();
            }else if(list.size() != maxNumberOfElements){
                LOGGER.warn("The size of the list of different oobject representations are not the same. This is likely an error but we just try to convert the maximum.");
                if(list.size() > maxNumberOfElements){
                    maxNumberOfElements = list.size();
                }
            }
        }
        
        List<Object> transformedObjects = new ArrayList(maxNumberOfElements);
        int numberOfRepresentations = sourceObjects.size();
        for(int i=0; i < maxNumberOfElements; i++){
            Set<Object> possibleRepresentationsForIthElement = new HashSet(numberOfRepresentations);
            for(List<Object> list : sourceObjects){
                try{
                    possibleRepresentationsForIthElement.add(list.get(i));
                }catch(IndexOutOfBoundsException ex){ 
                    // if one lists is shorter, we just use the remaining representations and one list has to have this number of elements
                }
            }
            Object transformed = getTransformedObjectMultipleRepresentations(possibleRepresentationsForIthElement, targetType, transformationProperties);
            if(transformed == null)
                return null;
            transformedObjects.add(transformed);
        }
        return transformedObjects;
        */
    }
    
    /**
     * Directly get the transformed object or null if something went wrong.
     * @param <T> the type
     * @param sourceObjects the objects which all represent the same information. To this set, the transformed object will be added.
     * @param targetType the tyoe of class to trasnform to 
     * @param transformationProperties additional properties.
     * @return the transformed object or null
     * @throws TypeTransformationException in case no transformation route is found
     */
    public static <T> List<T> getTransformedListOfObjectsMultipleRepresentations(List<Set<Object>> sourceObjects, Class<T> targetType, Object transformationProperties) throws TypeTransformationException{
        return getTransformedListOfObjectsMultipleRepresentations(sourceObjects, targetType, getTransformedPropertiesOrNewInstance(transformationProperties));
    }
    
    /**
     * Directly get the transformed object or null if something went wrong.No transformation properties are provided.
     * @param <T> the type
     * @param sourceObjects the objects which all represent the same information. To this set, the transformed object will be added.
     * @param targetType the tyoe of class to trasnform to 
     * @return the transformed object or null
     * @throws TypeTransformationException in case no transformation route is found
     */
    public static <T> List<T> getTransformedListOfObjectsMultipleRepresentations(List<Set<Object>> sourceObjects, Class<T> targetType) throws TypeTransformationException{
        return getTransformedListOfObjectsMultipleRepresentations(sourceObjects, targetType, new Properties());
    }
    
    
    /****************************************************
     * Section getTransformedObjectMultipleRepresentations
     ****************************************************/
    
    
    /**
     * Directly get the transformed object or null if something went wrong.
     * @param <T> the type of the return value
     * @param sourceObjects the objects which all represent the same information. To this set, the transformed object will be added.
     * @param targetType the tyoe of class to trasnform to 
     * @param transformationProperties additional properties.
     * @return the transformed object or null
     * @throws TypeTransformationException in case no transformation route is found
     */
    public static <T> T getTransformedObjectMultipleRepresentations(Set<Object> sourceObjects, Class<T> targetType, Properties transformationProperties) throws TypeTransformationException{
        ObjectTransformationRoute<T> route = TypeTransformerRegistry.getObjectTransformationRouteMultipleRepresentations(sourceObjects, targetType, transformationProperties);
        if(route == null){
            return null;
        }
        T transformedObject = route.getTransformedObject(transformationProperties);
        sourceObjects.add(transformedObject);
        return transformedObject;
    }
    
    /**
     * Directly get the transformed object or null if something went wrong.
     * @param <T> the type of the return value
     * @param sourceObjects the objects which all represent the same information. To this set, the transformed object will be added.
     * @param targetType the tyoe of class to trasnform to 
     * @param transformationProperties additional properties.
     * @return the transformed object or null
     * @throws TypeTransformationException in case no transformation route is found
     */
    public static <T> T getTransformedObjectMultipleRepresentations(Set<Object> sourceObjects, Class<T> targetType, Object transformationProperties) throws TypeTransformationException{
        return getTransformedObjectMultipleRepresentations(sourceObjects, targetType, getTransformedPropertiesOrNewInstance(transformationProperties));
    }
    
    /**
     * Directly get the transformed object or null if something went wrong.No transformation parameters are provided.
     * @param <T> the type of the return value
     * @param sourceObjects the objects which all represent the same information. To this set, the transformed object will be added.
     * @param targetType the tyoe of class to trasnform to 
     * @return the transformed object or null
     * @throws TypeTransformationException in case no transformation route is found
     */
    public static <T> T getTransformedObjectMultipleRepresentations(Set<Object> sourceObjects, Class<T> targetType) throws TypeTransformationException{
        return getTransformedObjectMultipleRepresentations(sourceObjects, targetType, new Properties());
    }
    
    
    /****************************************************
     * Section getTransformedObject
     ****************************************************/
    
    
    /**
     * Directly get the transformed object or null if something went wrong.
     * @param <T> the type of the return value
     * @param sourceObject the source object
     * @param targetType the tyoe of class to trasnform to 
     * @param transformationProperties additional properties which can be used during transformation.
     * @return the transformed object or null if source is null
     * @throws TypeTransformationException in case no route is found
     */
    public static <T> T getTransformedObject(Object sourceObject, Class<T> targetType, Properties transformationProperties) throws TypeTransformationException{
        ObjectTransformationRoute<T> route = TypeTransformerRegistry.getObjectTransformationRoute(sourceObject, targetType, transformationProperties);
        if(route == null){
            return null;
        }
        return route.getTransformedObject(transformationProperties);
    }
    
    /**
     * Directly get the transformed object or null if something went wrong.
     * @param <T> the type of the return value
     * @param sourceObject the source object
     * @param targetType the tyoe of class to trasnform to 
     * @param transformationProperties additional properties which can be used during transformation.
     * @return the transformed object or null if source is null
     * @throws TypeTransformationException in case no route is found
     */
    public static <T> T getTransformedObject(Object sourceObject, Class<? extends T> targetType, Object transformationProperties) throws TypeTransformationException{
        return getTransformedObject(sourceObject, targetType, getTransformedProperties(transformationProperties));
    }
    
    /**
     * Directly get the transformed object or null if something went wrong. No tranformation parameters are provided.
     * @param <T> the type of the return value
     * @param sourceObject the source object
     * @param targetType the tyoe of class to trasnform to 
     * @return the transformed object or null if source is null
     * @throws TypeTransformationException in case no route is found
     */
    public static <T> T getTransformedObject(Object sourceObject, Class<? extends T> targetType) throws TypeTransformationException{
        return getTransformedObject(sourceObject, targetType, new Properties());
    }
    
    /**
     * Directly get the transformed object or new instance if something went wrong.
     * If a new instance could not be generated, null is returned.
     * @param <T> the type of the return value
     * @param sourceObject the source object
     * @param targetType the type of class to trasnform to 
     * @param transformationProperties additional properties which can be used during transformation.
     * @return the transformed object
     */
    public static <T> T getTransformedObjectOrNewInstance(Object sourceObject, Class<T> targetType, Properties transformationProperties){
        try {
            ObjectTransformationRoute<T> route = TypeTransformerRegistry.getObjectTransformationRoute(sourceObject, targetType, transformationProperties);
            if(route == null){
                //sourceObject was null
                return getNewInstance(targetType);
            }
            return route.getTransformedObject(transformationProperties);
        } catch (TypeTransformationException ex) {
            return getNewInstance(targetType);
        }
    }
    
    /**
     * Directly get the transformed object or a new instance something went wrong.
     * If a new instance could not be generated, null is returned.
     * @param <T> the type of the return value
     * @param sourceObject the source object
     * @param targetType the tyoe of class to trasnform to 
     * @param transformationProperties additional properties which can be used during transformation.
     * @return the transformed object or new instance
     */
    public static <T> T getTransformedObjectOrNewInstance(Object sourceObject, Class<? extends T> targetType, Object transformationProperties){
        return getTransformedObjectOrNewInstance(sourceObject, targetType, getTransformedPropertiesOrNewInstance(transformationProperties));
    }
    
    /**
     * Directly get the transformed object or a new instance something went wrong. No tranformation parameters are provided.
     * If a new instance could not be generated, null is returned.
     * @param <T> the type of the return value
     * @param sourceObject the source object
     * @param targetType the tyoe of class to trasnform to 
     * @return the transformed object or new instance
     */
    public static <T> T getTransformedObjectOrNewInstance(Object sourceObject, Class<? extends T> targetType){
        return getTransformedObjectOrNewInstance(sourceObject, targetType, new Properties());
    }
    
    
    /**
     * Transforms a given object to java.lang:Properties or throws an exception if something went wrong.If parameter is null, then null is returned.
     * @param parameters the object which represents parameters.
     * @return java.lang:Properties or new Properties() if something went wrong
     * @throws TypeTransformationException in case transformation route is not found.
     */
    public static Properties getTransformedProperties(Object parameters) throws TypeTransformationException{
        return getTransformedObject(parameters, Properties.class, new Properties());
    }
    
    /**
     * Transforms a given object to java.lang:Properties or throws an exception if something went wrong.
     * If parameter is null, then null is returned.
     * @param parameters the object which represents parameters.
     * @return java.lang:Properties or new Properties() if something went wrong
     */
    public static Properties getTransformedPropertiesOrNewInstance(Object parameters){
        return getTransformedObjectOrNewInstance(parameters, Properties.class, new Properties());
    }
    
    public static <T> T getNewInstance(Class<T> clazz){
        try{
            return clazz.newInstance();
        }catch(InstantiationException | IllegalAccessException ex){
            LOGGER.warn("Could not create a new instance of {} as a default value. Check if there is an empty constructor. Return null for now.");
            return null;
        }
    }
    
    private static String classRepresentation(Set<Object> objects){
        Set<Class<?>> classes = new HashSet<>();
        for(Object o : objects){
            classes.add(o.getClass());
        }
        return classes.toString();
    }
    
    /**
     * Given a class return all superclasses and interfaces except the Object class (which would be too generic).
     * The value of the map is multiplied with the hierarchyCost
     * @param clazz the class to start searching
     * @param hierarchyCost the hierarchy cost which is multiplied with every level
     * @return a map which contains all superclasses and interfaces with corresponding depth
     */
    static Map<Class<?>, Integer> getAllSuperClassesAndIterfacesWithCost(Class<?> clazz, int hierarchyCost){
        if(hierarchyCost < 0){
            Map<Class<?>, Integer> newMap = new HashMap<>(1);
            newMap.put(clazz, 0);
            return newMap;
        }
        Map<Class<?>, Integer> map = getAllSuperClassesAndIterfaces(clazz);
        Map<Class<?>, Integer> newMap = new HashMap<>(map.size());
        for(Entry<Class<?>, Integer> e : map.entrySet()){
            newMap.put(e.getKey(), e.getValue() * hierarchyCost);
        }
        return newMap;
    }
    
    /**
     * Cache for Superclasses and interfaces for a given class.
     * Since there are ussually not so many classes, this information can directly be cache without much memory consumption.
     */
    private static final Map<Class<?>, Map<Class<?>, Integer>> SUPER_CLASSES_CACHE = new HashMap<>();

    /**
     * Given a class return all superclasses and interfaces except the Object class (which woul be too generic).
     * @param clazz the class to start searching
     * @return a map which contains all superclasses and interfaces with corresponding depth
     */
    static Map<Class<?>, Integer> getAllSuperClassesAndIterfaces(Class<?> clazz){
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
//can be extended by searching not only shortest path but k-shortest path (if an error occurs at shortest path) : https://en.wikipedia.org/wiki/Yen%27s_algorithm

class TransformationEdge{
    private final Class<?> source;
    private final Class<?> target;
    private final TypeTransformer<?,?> transformer;
    private final int cost;

    public TransformationEdge(Class<?> source, Class<?> target, TypeTransformer<?,?> transformer, int cost) {
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

    public TypeTransformer<?,?> getTransformer() {
        return transformer;
    }

    public int getCost() {
        return cost;
    }
}

