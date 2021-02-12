package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcherCaller;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This matcher caller expects some matcher object and all other paramters as objects as well
 * and call it with apropiate type transformers such that the call can actually happen.
 */
public class GenericMatcherCaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericMatcherCaller.class);
    
    /**
     * Calls a matcher object with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcher}, {@link IMatcherCaller}, or {@link IOntologyMatchingToolBridge}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
     * @param sourceOntology this object represents the source ontology
     * @param targetOntology this object represents the taregt ontology
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static AlignmentAndParameters runMatcher(Object matcher, Object sourceOntology, Object targetOntology) throws Exception{
        return runMatcher(matcher, sourceOntology, targetOntology, null, null);
    }
    
    /**
     * Calls a matcher object with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcher}, {@link IMatcherCaller}, or {@link IOntologyMatchingToolBridge}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
     * @param sourceOntology this object represents the source ontology
     * @param targetOntology this object represents the taregt ontology
     * @param inputAlignment the object which represents an input alignment. Can be null.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static AlignmentAndParameters runMatcher(Object matcher, Object sourceOntology, Object targetOntology, Object inputAlignment) throws Exception{
        return runMatcher(matcher, sourceOntology, targetOntology, inputAlignment, null);
    }
    
    /**
     * Calls a matcher object with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcher}, {@link IMatcherCaller}, or {@link IOntologyMatchingToolBridge}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
     * @param sourceOntology this object represents the source ontology
     * @param targetOntology this object represents the taregt ontology
     * @param inputAlignment the object which represents an input alignment. Can be null.
     * @param parameters an objetc which represents parameters. Can be null.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static AlignmentAndParameters runMatcher(Object matcher, Object sourceOntology, Object targetOntology, Object inputAlignment, Object parameters) throws Exception{
        return runMatcherMultipleRepresentations(matcher, new HashSet<>(Arrays.asList(sourceOntology)), new HashSet<>(Arrays.asList(targetOntology)), inputAlignment, parameters);
    }
    
    /**
     * Calls a matcher objetc with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcher}, {@link IMatcherCaller}, or {@link IOntologyMatchingToolBridge}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
     * @param sourceOntology this is an iterable of objects which all represents the same source ontology / knowledge graph
     * @param targetOntology this is an iterable of objects which all represents the same target ontology / knowledge graph
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static AlignmentAndParameters runMatcherMultipleRepresentations(Object matcher, Set<Object> sourceOntology, Set<Object> targetOntology) throws Exception{
        return runMatcherMultipleRepresentations(matcher, sourceOntology, targetOntology, null, null);
    }
    
    /**
     * Calls a matcher objetc with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcher}, {@link IMatcherCaller}, or {@link IOntologyMatchingToolBridge}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
     * @param sourceOntology this is an iterable of objects which all represents the same source ontology / knowledge graph
     * @param targetOntology this is an iterable of objects which all represents the same target ontology / knowledge graph
     * @param inputAlignment the object which represents an input alignment. Can be null.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static AlignmentAndParameters runMatcherMultipleRepresentations(Object matcher, Set<Object> sourceOntology, Set<Object> targetOntology, Object inputAlignment) throws Exception{
        return runMatcherMultipleRepresentations(matcher, sourceOntology, targetOntology, inputAlignment, null);
    }
    
    /**
     * Calls a matcher objetc with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcher}, {@link IMatcherCaller}, or {@link IOntologyMatchingToolBridge}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
     * @param sourceOntology this is an iterable of objects which all represents the same source ontology / knowledge graph
     * @param targetOntology this is an iterable of objects which all represents the same target ontology / knowledge graph
     * @param inputAlignment the object which represents an input alignment. Can be null.
     * @param parameters an objetc which represents parameters. Can be null.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static AlignmentAndParameters runMatcherMultipleRepresentations(Object matcher, Set<Object> sourceOntology, Set<Object> targetOntology, Object inputAlignment, Object parameters) throws Exception{
        Object matcherInstance = matcher;
        if(matcher instanceof String){
            try {
                matcherInstance = Class.forName((String)matcher).newInstance();
            } catch (InstantiationException|IllegalAccessException|ClassNotFoundException ex) {
                LOGGER.error("Could not instantiate the class given by the fully qualified name {}. The matcher will not be called.", matcher, ex);
                return null;
            }
        }else if(matcher instanceof Class){
            try {
                matcherInstance = ((Class)matcher).newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                LOGGER.error("Could not instantiate the class. The matcher will not be called.", ex);
                return null;
            }
        }
        if(matcherInstance instanceof IMatcherCaller){
            return runIMatcherCaller((IMatcherCaller)matcherInstance, sourceOntology, targetOntology, inputAlignment, parameters);
        } else if(matcherInstance instanceof IMatcher){
            return runIMatcher((IMatcher)matcherInstance, sourceOntology, targetOntology, inputAlignment, parameters);
        } else if(matcherInstance instanceof IOntologyMatchingToolBridge){
            return runIOntologyMatchingToolBridge((IOntologyMatchingToolBridge)matcherInstance, sourceOntology, targetOntology, inputAlignment, parameters);
        }//else if(matcherInstance instanceof IMatcherMultiSource){
         //   return GenericMatcherMultiSourceCaller.runMatcherMultiSourceMultipleRepresentations(matcherInstance, oneToOneInputToMultiSource(sourceOntology, targetOntology), inputAlignment, parameters);
        //}
        else{
            LOGGER.error("The given matcher instance does not implement IMatcher nor IOntologyMatchingToolBridge. The given class is {}. The matcher will not be called.", matcherInstance.getClass());
            return null;
        }
    }
    
    // in case we want to call multi source even if we have only two ontologies
    //private static Set<List<Object>> oneToOneInputToMultiSource(Set<Object> sourceOntology, Set<Object> targetOntology){
    //    Map<Class<?>, List<Object>> map = new HashMap();
    //    for(Object s : sourceOntology){
    //        map.computeIfAbsent(s.getClass(), __-> new ArrayList()).add(s);
    //    }
    //    for(Object s : targetOntology){
    //        map.computeIfAbsent(s.getClass(), __-> new ArrayList()).add(s);
    //    }
    //    return new HashSet(map.values());
    //}
    
    
    /************************************
     * IOntologyMatchingToolBridge section
     ************************************/
    
    /**
     * Runs a matcher which implements the {@link IOntologyMatchingToolBridge} interface.
     * @param matcher the matcher object
     * @param sourceOntology the source ontology / knowledge graph
     * @param targetOntology the taregt ontology / knowledge graph
     * @param inputAlignment the input alignment
     * @param parameters the parameters
     * @return alignment and parameters
     * @throws Exception in case somethign goes wrong
     */
    private static AlignmentAndParameters runIOntologyMatchingToolBridge(IOntologyMatchingToolBridge matcher, Set<Object> sourceOntology, Set<Object> targetOntology, Object inputAlignment, Object parameters) throws Exception{
        Properties p = TypeTransformerRegistry.getTransformedProperties(parameters);
        
        Object transformedSource = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(sourceOntology, URL.class, p);
        if(transformedSource == null)
            return null;
        Object transformedTarget = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(targetOntology, URL.class, p);
        if(transformedTarget == null)
            return null;
        
        URL result;
        if(inputAlignment == null || inputAlignment.getClass() == Object.class){
            result = matcher.align((URL)transformedSource, (URL)transformedTarget);
        }else{
            Object transformedInputAlignment = TypeTransformerRegistry.getTransformedObject(inputAlignment, URL.class, p);
            if(transformedInputAlignment == null)
                return null;
            result = matcher.align((URL)transformedSource, (URL)transformedTarget, (URL)transformedInputAlignment);
        }
        //no parameters for IOntologyMatchingToolBridge
        
        return new AlignmentAndParameters(result, parameters); // just return the same input parameters
    }
    
    
    /*****************************************
     * IMatcher and IMatcherCaller section
     *****************************************/
    
    
    /**
     * Runs a matcher which implements the {@link IMatcher} interface.
     * @param matcher the matcher object
     * @param sourceOntology the source ontology / knowledge graph
     * @param targetOntology the taregt ontology / knowledge graph
     * @param inputAlignment the input alignment
     * @param parameters the parameters
     * @return alignment and parameters
     * @throws Exception in case somethign goes wrong
     */
    @SuppressWarnings("unchecked")
    private static AlignmentAndParameters runIMatcher(IMatcher matcher, Set<Object> sourceOntology, Set<Object> targetOntology, Object inputAlignment, Object parameters) throws Exception{
        Method matchMethod = getIMatcherMethod(matcher.getClass());
        if(matchMethod == null){
            LOGGER.error("Could not find match method of object which implements IMatcher. The matcher is not called");
            return null;
        }
        LOGGER.debug("Choosing the following method to extract the parameter types: {}", matchMethod);
        Class<?>[] paramTypes = matchMethod.getParameterTypes();
        
        Properties p = TypeTransformerRegistry.getTransformedProperties(parameters);
        
        Object transformedSource = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(sourceOntology, paramTypes[0], p);
        if(transformedSource == null)
            return null;
        Object transformedTarget = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(targetOntology, paramTypes[1], p);
        if(transformedTarget == null)
            return null;
        
        //optional params
        Object transformedInputAlignment;
        if(inputAlignment == null || inputAlignment.getClass() == Object.class){
            //try to create an instance
            try{
                transformedInputAlignment = paramTypes[2].newInstance();
            }catch(IllegalAccessException | InstantiationException | ExceptionInInitializerError | SecurityException ex){
                LOGGER.warn("The optional inputAlignment parameter is null or object and thus a new instance of type {} was created which did not worked out (if you own the class, then you can add an empty constructor). Try to call the matcher with null value.", paramTypes[2], ex);
                transformedInputAlignment = null;
            }
        }else{
            transformedInputAlignment = TypeTransformerRegistry.getTransformedObject(inputAlignment, paramTypes[2], p);
            if(transformedInputAlignment == null)
                return null;
        }
        
        Object transformedParameter;
        //if the parameters object equals the object class, then it cannot contain any valuable information and we can try to create a new instance of the specified type.
        if(parameters == null || parameters.getClass() == Object.class){
            //try to create an instance
            try{
                transformedParameter = paramTypes[3].newInstance();
            }catch(IllegalAccessException | InstantiationException | ExceptionInInitializerError | SecurityException ex){
                LOGGER.warn("The optional params parameter is null or object and thus a new instance of type {} was created which did not worked out (if you own the class, then you can add an empty constructor). Try to call the matcher with null value.", paramTypes[2], ex);
                transformedParameter = null;
            }
        }else{
            transformedParameter = TypeTransformerRegistry.getTransformedObject(parameters, paramTypes[3], p);
            if(transformedParameter == null)
                return null;
        }
        Object resultingAlignment = matcher.match(transformedSource, transformedTarget, transformedInputAlignment, transformedParameter);
        return new AlignmentAndParameters(resultingAlignment, transformedParameter);        
    }
    
    /**
     * Search the method declared in the IMatcher interface.
     * It starts from the given class and moves on to the superclasses.
     * @param clazz the given class to start searching
     * @return the method from the IMatcher or null if non is found
     */
    private static Method getIMatcherMethod(Class<?> clazz){
        //https://stackoverflow.com/questions/45729211/can-i-get-the-interface-that-a-method-is-implementing-via-java-reflection
        //TODO: maybe find the correct method which really implements the imterface and not only one method with the same name and parameter constellation
        Class<?> c = clazz;
        //search the method from the interface 
        while (c != null) {
            List<Entry<Method, Integer>> possibleMethods = new ArrayList<>();
            for(Method method : c.getDeclaredMethods()){
                //compare name 
                if(method.getName().equals("match")){
                    Class<?>[] paramTypes = method.getParameterTypes();
                    //compare parameter count
                    if(paramTypes.length == 4){
                        // compare parameter types
                        if(paramTypes[0] == paramTypes[1] && method.getReturnType() == paramTypes[2]){
                            int numberOfNonObjectParameters = 0;
                            for(int i=0; i < 4; i++){
                                if(paramTypes[i] != Object.class)
                                    numberOfNonObjectParameters++;
                            }
                            possibleMethods.add(new SimpleEntry<>(method, numberOfNonObjectParameters));
                        }
                    }
                }
            }
            if(possibleMethods.isEmpty() == false){
                //return the method which has the highest number of parameters which are NOT object.
                //Because the generic method also creates a method with only object parameters.
                //In case there is only a method which acceots Objects, then this is expected and this method should be chosen.
                possibleMethods.sort((c1, c2) -> c2.getValue().compareTo(c1.getValue()));
                return possibleMethods.get(0).getKey();
            }
            c = c.getSuperclass();
        }
        return null;
    }
    
    
    /**
     * Runs a matcher which implements the {@link IMatcherCaller} interface.
     * @param matcher the matcher object
     * @param sourceOntology the set of different representations of source ontology / knowledge graph
     * @param targetOntology the set of different representations of taregt ontology / knowledge graph
     * @param inputAlignment the input alignment
     * @param parameters the parameters
     * @return alignment and parameters
     * @throws Exception in case somethign goes wrong
     */
    private static AlignmentAndParameters runIMatcherCaller(IMatcherCaller matcher, Set<Object> sourceOntology, Set<Object> targetOntology, Object inputAlignment, Object parameters) throws Exception{
        if(sourceOntology == null || sourceOntology.isEmpty() || targetOntology == null || targetOntology.isEmpty()){
            LOGGER.warn("source or target representatives are null or empty. Matcher {} is not called.", matcher.getClass());
            return null;
        }
        if(inputAlignment == null)
            inputAlignment = new Object();
        if(parameters == null)
            parameters = new Object();
        return matcher.match(sourceOntology, targetOntology, inputAlignment, parameters);
    }
}
