package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.IMatcherMultiSource;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.IMatcherMultiSourceCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MatcherMultiSourceURL;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
public class GenericMatcherMultiSourceCaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericMatcherMultiSourceCaller.class);
    
    
    /**
     * Calls a multi source matcher object with the provided arguments.The type of ontologies must be same same for all ontologies e.g. all URLs.
     * @param <T> the type of the ontologies ( have to be the same for all ontologies)
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcherMultiSource}, or {@link IMatcherMultiSourceCaller}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
     * @param ontologies a list of objects which represents the different ontologies/ knowledge graphs to be aligned.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static <T> AlignmentAndParameters runMatcherMultiSourceSpecificType(Object matcher, List<T> ontologies) throws Exception{
        return runMatcherMultiSource(matcher, new ArrayList<>(ontologies), null, null);
    }
    
    /**
     * Calls a multi source matcher object with the provided arguments. The type of ontologies must be same same for all ontologies e.g. all URLs.
     * @param <T> the type of the ontologies ( have to be the same for all ontologies)
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcherMultiSource}, or {@link IMatcherMultiSourceCaller}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
     * @param ontologies a list of objects which represents the different ontologies/ knowledge graphs to be aligned.
     * @param inputAlignment the object which represents an input alignment. Can be null.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static <T> AlignmentAndParameters runMatcherMultiSourceSpecificType(Object matcher, List<T> ontologies, Object inputAlignment) throws Exception{
        return runMatcherMultiSource(matcher, new ArrayList<>(ontologies), inputAlignment, null);
    }
    
    /**
     * Calls a multi source matcher object with the provided arguments. The type of ontologies must be same same for all ontologies e.g. all URLs.
     * @param <T> the type of the ontologies ( have to be the same for all ontologies)
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcherMultiSource}, or {@link IMatcherMultiSourceCaller}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
     * @param ontologies a list of objects which represents the different ontologies/ knowledge graphs to be aligned.
     * @param inputAlignment the object which represents an input alignment. Can be null.
     * @param parameters an objetc which represents parameters. Can be null.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static <T> AlignmentAndParameters runMatcherMultiSourceSpecificType(Object matcher, List<T> ontologies, Object inputAlignment, Object parameters) throws Exception{
        return runMatcherMultiSource(matcher, new ArrayList<>(ontologies), inputAlignment, parameters);
    }
    
    /**
     * Calls a multi source matcher object with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcherMultiSource}, or {@link IMatcherMultiSourceCaller}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
     * @param ontologies a list of objects which represents the different ontologies/ knowledge graphs to be aligned.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static AlignmentAndParameters runMatcherMultiSource(Object matcher, List<Object> ontologies) throws Exception{
        return runMatcherMultiSource(matcher, ontologies, null, null);
    }
    
    /**
     * Calls a multi source matcher object with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcherMultiSource}, or {@link IMatcherMultiSourceCaller}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
     * @param ontologies a list of objects which represents the different ontologies/ knowledge graphs to be aligned.
     * @param inputAlignment the object which represents an input alignment. Can be null.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static AlignmentAndParameters runMatcherMultiSource(Object matcher, List<Object> ontologies, Object inputAlignment) throws Exception{
        return runMatcherMultiSource(matcher, ontologies, inputAlignment, null);
    }
    
    /**
     * Calls a multi source matcher object with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcherMultiSource}, or {@link IMatcherMultiSourceCaller}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
     * @param ontologies a list of objects which represents the different ontologies/ knowledge graphs to be aligned.
     * @param inputAlignment the object which represents an input alignment. Can be null.
     * @param parameters an objetc which represents parameters. Can be null.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static AlignmentAndParameters runMatcherMultiSource(Object matcher, List<Object> ontologies, Object inputAlignment, Object parameters) throws Exception{
        List<Set<Object>> list = new ArrayList<>(ontologies.size());
        for(Object ontology : ontologies){
            list.add(new HashSet<>(Arrays.asList(ontology)));
        }
        return runMatcherMultiSourceMultipleRepresentations(matcher, list, inputAlignment, parameters);
    }
    
    /**
     * Calls a multi source matcher object with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcherMultiSource}, or {@link IMatcherMultiSourceCaller}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
     * @param ontologies this is a list of sets of objects where each sets contains different representations of the dame ontologies/ knowledge graph.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static AlignmentAndParameters runMatcherMultiSourceMultipleRepresentations(Object matcher, List<Set<Object>> ontologies) throws Exception{
        return runMatcherMultiSourceMultipleRepresentations(matcher, ontologies, null, null);
    }
    
    /**
     * Calls a multi source matcher object with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcherMultiSource}, or {@link IMatcherMultiSourceCaller}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
      * @param ontologies this is a list of sets of objects where each sets contains different representations of the dame ontologies/ knowledge graph.
     * @param inputAlignment the object which represents an input alignment. Can be null.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static AlignmentAndParameters runMatcherMultiSourceMultipleRepresentations(Object matcher, List<Set<Object>> ontologies, Object inputAlignment) throws Exception{
        return runMatcherMultiSourceMultipleRepresentations(matcher, ontologies, inputAlignment, null);
    }
    
    /**
     * Calls a multi source matcher object with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>an object / instance which implements/extends {@link IMatcherMultiSource}, or {@link IMatcherMultiSourceCaller}</li>
     *  <li>a class object: a class which implements one of the above interfaces/classes - a new instance of this class will be created.</li>
     *  <li>a string: the fully qualified name of a class which implements one of the above interfaces/classes like de.uni_mannheim.informatik.dws.melt.matching_base.MyMatcher - a new instance of this class will be created.</li>
     * </ul>
      * @param ontologies this is a list of sets of objects where each sets contains different representations of the dame ontologies/ knowledge graph.
     * @param inputAlignment the object which represents an input alignment. Can be null.
     * @param parameters an objetc which represents parameters. Can be null.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static AlignmentAndParameters runMatcherMultiSourceMultipleRepresentations(Object matcher, List<Set<Object>> ontologies, Object inputAlignment, Object parameters) throws Exception{
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
        
        //if the matcher is also loading its own transformers, then call this first (maybe used in SEALS).
        if(matcherInstance instanceof TypeTransformerLoader){
            ((TypeTransformerLoader)matcherInstance).registerTypeTransformers();
        }
        
        if(matcherInstance instanceof IMatcherMultiSourceCaller){
            return runIMatcherMultiSourceCaller((IMatcherMultiSourceCaller)matcherInstance, ontologies, inputAlignment, parameters);
        } else if(matcherInstance instanceof IMatcherMultiSource){
            return runIMatcherMultiSource((IMatcherMultiSource)matcherInstance, ontologies, inputAlignment, parameters);
        } else if(matcherInstance instanceof MatcherMultiSourceURL){
            return runMatcherMultiSourceURL((MatcherMultiSourceURL)matcherInstance, ontologies, inputAlignment, parameters);
        }else{
            LOGGER.error("The given matcher instance does not implement IMatcherMultiSource nor IMatcherMultiSourceCaller. The given class is {}. The matcher will not be called.", matcherInstance.getClass());
            return null;
        }
    }
    
    
    /************************************
     * Helper section
     ************************************/
    
    /**
     * Runs a matcher which implements the {@link IMatcherMultiSource} interface.
     * @param matcher the matcher object
     * @param ontologies this is a list of sets of objects where each sets contains different representations of the dame ontologies/ knowledge graph.
     * @param inputAlignment the input alignment
     * @param parameters the parameters
     * @return alignment and parameters
     * @throws Exception in case somethign goes wrong
     */
    @SuppressWarnings("unchecked")
    private static AlignmentAndParameters runIMatcherMultiSource(IMatcherMultiSource matcher, List<Set<Object>> ontologies, Object inputAlignment, Object parameters) throws Exception{
        Method matchMethod = getIMatcherMultiSourceMethod(matcher.getClass());
        if(matchMethod == null){
            LOGGER.error("Could not find match method of object which implements IMatcher. The matcher is not called");
            return null;
        }
        LOGGER.debug("Choosing the following method to extract the parameter types: {}", matchMethod);
        Class<?>[] paramTypes = matchMethod.getParameterTypes();
        Type[] genericParamTypes = matchMethod.getGenericParameterTypes();
        
        ParameterizedType pType = (ParameterizedType) genericParamTypes[0];
        Class<?> modelType = (Class<?>) pType.getActualTypeArguments()[0];
        
        Properties p = TypeTransformerRegistry.getTransformedPropertiesOrNewInstance(parameters);
        
        List<?> transformedModels = TypeTransformerRegistry.getTransformedListOfObjectsMultipleRepresentations(ontologies, modelType, p);
        if(transformedModels == null)
            return null;
        
        //optional params
        Object transformedInputAlignment = null;
        if(inputAlignment == null || inputAlignment.getClass() == Object.class){
            //try to create an instance
            try{
                transformedInputAlignment = paramTypes[1].newInstance();
            }catch(IllegalAccessException | InstantiationException | ExceptionInInitializerError | SecurityException ex){
                LOGGER.warn("The optional inputAlignment parameter is null or object and thus a new instance of type {} was created which did not worked out (if you own the class, then you can add an empty constructor). Try to call the matcher with null value.", paramTypes[2], ex);
                transformedInputAlignment = null;
            }
        }else{
            transformedInputAlignment = TypeTransformerRegistry.getTransformedObject(inputAlignment, paramTypes[1], p);
            if(transformedInputAlignment == null)
                return null;
        }
        
        Object transformedParameter = null;
        //if the parameters object equals the object class, then it cannot contain any valuable information and we can try to create a new instance of the specified type.
        if(parameters == null || parameters.getClass() == Object.class){
            //try to create an instance
            try{
                transformedParameter = paramTypes[2].newInstance();
            }catch(IllegalAccessException | InstantiationException | ExceptionInInitializerError | SecurityException ex){
                LOGGER.warn("The optional params parameter is null or object and thus a new instance of type {} was created which did not worked out (if you own the class, then you can add an empty constructor). Try to call the matcher with null value.", paramTypes[2], ex);
                transformedParameter = null;
            }
        }else{
            transformedParameter = TypeTransformerRegistry.getTransformedObject(parameters, paramTypes[2], p);
            if(transformedParameter == null)
                return null;
        }
        
        Object resultingAlignment = matcher.match(transformedModels, transformedInputAlignment, transformedParameter);
        return new AlignmentAndParameters(resultingAlignment, transformedParameter);
    }
    
    
    private static Method getIMatcherMultiSourceMethod(Class<?> clazz){
        Class<?> c = clazz;
        while (c != null) {
            List<Entry<Method, Integer>> possibleMethods = new ArrayList<>();
            for(Method method : c.getDeclaredMethods()){
                //compare name 
                if(method.getName().equals("match")){
                    Class<?>[] paramTypes = method.getParameterTypes();
                    //compare parameter count
                    if(paramTypes.length == 3){
                        // compare parameter types
                        if(method.getReturnType() == paramTypes[1]){
                            int numberOfNonObjectParameters = 0;
                            for(int i=0; i < 3; i++){
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
     * Runs a matcher which implements the {@link IMatcherMultiSourceCaller} interface.
     * @param matcher the matcher object
     * @param ontologies  this is a list of sets of objects where each sets contains different representations of the dame ontologies/ knowledge graph.
     * @param inputAlignment the input alignment
     * @param parameters the parameters
     * @return alignment and parameters
     * @throws Exception in case somethign goes wrong
     */
    private static AlignmentAndParameters runIMatcherMultiSourceCaller(IMatcherMultiSourceCaller matcher, List<Set<Object>> ontologies, Object inputAlignment, Object parameters) throws Exception{
        if(ontologies == null || ontologies.isEmpty()){
            LOGGER.warn("ontology representatives are null or empty. Matcher {} is not called.", matcher.getClass());
            return null;
        }
        if(inputAlignment == null)
            inputAlignment = new Object();
        if(parameters == null)
            parameters = new Object();
        return matcher.match(ontologies, inputAlignment, parameters);
    }
    
    /**
     * Runs a matcher which implements the {@link IMatcherMultiSourceCaller} interface.
     * @param matcher the matcher object
     * @param ontologies  this is a list of sets of objects where each sets contains different representations of the dame ontologies/ knowledge graph.
     * @param inputAlignment the input alignment
     * @param parameters the parameters
     * @return alignment and parameters
     * @throws Exception in case somethign goes wrong
     */
    private static AlignmentAndParameters runMatcherMultiSourceURL(MatcherMultiSourceURL matcher, List<Set<Object>> ontologies, Object inputAlignment, Object parameters) throws Exception{
        if(ontologies == null || ontologies.isEmpty()){
            LOGGER.warn("ontology representatives are null or empty. Matcher {} is not called.", matcher.getClass());
            return null;
        }
        Properties p = TypeTransformerRegistry.getTransformedPropertiesOrNewInstance(parameters);
        
        List<URL> transformedModels = TypeTransformerRegistry.getTransformedListOfObjectsMultipleRepresentations(ontologies, URL.class, p);
        if(transformedModels == null)
            return null;
        URL transformedInputAlignment = TypeTransformerRegistry.getTransformedObject(inputAlignment, URL.class, p);
        URL transformedParameters = TypeTransformerRegistry.getTransformedObject(parameters, URL.class, p);  
        
        URL result = matcher.match(transformedModels, transformedInputAlignment, transformedParameters);
        return new AlignmentAndParameters(result, transformedParameters);
    }
    
    
    public static boolean needsTransitiveClosureForEvaluation(Object matcher){
        Object matcherInstance = matcher;
        if(matcher instanceof String){
            try {
                matcherInstance = Class.forName((String)matcher).newInstance();
            } catch (InstantiationException|IllegalAccessException|ClassNotFoundException ex) {
                LOGGER.error("Could not instantiate the class given by the fully qualified name {}. Return false for transive closure in evaluation.", matcher, ex);
                return false;
            }
        }else if(matcher instanceof Class){
            try {
                matcherInstance = ((Class)matcher).newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                LOGGER.error("Could not instantiate the class. Return false for transive closure in evaluation.", ex);
                return false;
            }
        }
        
        if(matcherInstance instanceof IMatcherMultiSourceCaller){
            return ((IMatcherMultiSourceCaller)matcherInstance).needsTransitiveClosureForEvaluation();
        } else if(matcherInstance instanceof IMatcherMultiSource){
            return ((IMatcherMultiSource)matcherInstance).needsTransitiveClosureForEvaluation();
        }else if(matcherInstance instanceof MatcherMultiSourceURL){
            return ((MatcherMultiSourceURL)matcherInstance).needsTransitiveClosureForEvaluation();
        }else{
            LOGGER.error("The given matcher instance does not implement IMatcherMultiSource, IMatcherMultiSourceCaller, or MatcherMultiSourceURL. The given class is {}. Return false for transive closure in evaluation.", matcherInstance.getClass());
            return false;
        }
    }
    
}
