package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
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
     * Calls a matcher objetc with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>a string: which is the the fully qualified name of class which implements {@link IMatcher} interface or {@link IOntologyMatchingToolBridge}</li>
     *  <li>a class: class object of class which implements {@link IMatcher} interface or {@link IOntologyMatchingToolBridge}</li>
     *  <li>an instance: instance object of class which implements {@link IMatcher} interface or {@link IOntologyMatchingToolBridge}</li>
     * </ul>
     * @param sourceOntology this is an iterable of objects which all represents the same ontology
     * @param targetOntology this is an iterable of objects which all represents the same ontology
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static Object runMatcher(Object matcher, Object sourceOntology, Object targetOntology) throws Exception{
        return runMatcher(matcher, Arrays.asList(sourceOntology), Arrays.asList(targetOntology), null, null);
    }
    
    /**
     * Calls a matcher objetc with the provided arguments.
     * @param matcher the matcher can be: <ul>
     *  <li>a string: which is the the fully qualified name of class which implements {@link IMatcher} interface or {@link IOntologyMatchingToolBridge}</li>
     *  <li>a class: class object of class which implements {@link IMatcher} interface or {@link IOntologyMatchingToolBridge}</li>
     *  <li>an instance: instance object of class which implements {@link IMatcher} interface or {@link IOntologyMatchingToolBridge}</li>
     * </ul>
     * @param sourceOntology this is an iterable of objects which all represents the same ontology
     * @param targetOntology this is an iterable of objects which all represents the same ontology
     * @param inputAlignment the object which represents an input alignment. Can be null.
     * @param parameter an objetc which represents parameters. Can be null.
     * @return the object which is returned by the matcher. This can be any arbitrary object, but you can call the TypeTransformerRegistry to get the representation you want.
     * @throws Exception in case something goes wrong
     */
    public static Object runMatcher(Object matcher, Iterable<Object> sourceOntology, Iterable<Object> targetOntology, Object inputAlignment, Object parameter) throws Exception{
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
        
        //TODO: maybe check hierarchy
        if(matcherInstance instanceof IMatcher){
            return runMatcher((IMatcher)matcherInstance, sourceOntology, targetOntology,inputAlignment, parameter);
        }else if(matcherInstance instanceof IOntologyMatchingToolBridge){
            return runMatcher((IOntologyMatchingToolBridge)matcherInstance, sourceOntology, targetOntology, inputAlignment, parameter);
        }else{
            LOGGER.error("The given matcher instance does not implement IMatcher nor IOntologyMatchingToolBridge. The given class is {}. The matcher will not be called.", matcherInstance.getClass());
            return null;
        }
    }
    
    public static Object runMatcher(IOntologyMatchingToolBridge matcher, Iterable<Object> sourceOntology, Iterable<Object> targetOntology, Object inputAlignment, Object parameter) throws Exception{
        //TODO: use parameters from parameter above (the ones from the matcher / which are also used for transformation)
        Object transformedSource = getTransformedObject(sourceOntology, URL.class, new Properties());
        if(transformedSource == null)
            return null;
        Object transformedTarget = getTransformedObject(targetOntology, URL.class, new Properties());
        if(transformedTarget == null)
            return null;
        
        if(inputAlignment == null){
            return matcher.align((URL)transformedSource, (URL)transformedTarget);
        }else{
            Object transformedInputAlignment = getTransformedObjectGivenOne(inputAlignment, URL.class, new Properties());
            if(transformedInputAlignment == null)
                return null;
            return matcher.align((URL)transformedSource, (URL)transformedTarget, (URL)transformedInputAlignment);
        }
        //no parameters for IOntologyMatchingToolBridge
    }
        
    public static Object runMatcher(IMatcher matcher, Iterable<Object> sourceOntology, Iterable<Object> targetOntology, Object inputAlignment, Object parameter) throws Exception{
        Method matchMethod = getMatcherMethod(matcher.getClass());
        if(matchMethod == null){
            LOGGER.error("Could not find match method of object which implements IMatcher. The matcher is not called");
            return null;
        }
        LOGGER.debug("Choosing the following method to call the matcher: {}", matchMethod);
        Class<?>[] paramTypes = matchMethod.getParameterTypes();
        
        //TODO: use parameters from parameter above (the ones from the matcher / which are also used for transformation)
        Object transformedSource = getTransformedObject(sourceOntology, paramTypes[0], new Properties());
        if(transformedSource == null)
            return null;
        Object transformedTarget = getTransformedObject(targetOntology, paramTypes[1], new Properties());
        if(transformedTarget == null)
            return null;
        
        //optional params
        Object transformedInputAlignment = null;
        if(inputAlignment == null){
            //try to create an instance
            try{
                transformedInputAlignment = paramTypes[2].newInstance();
            }catch(IllegalAccessException | InstantiationException | ExceptionInInitializerError | SecurityException ex){
                LOGGER.warn("The optional inputAlignment parameter is null and thus a new instance of type {} was created which did not worked out. Try to call the matcher with null value.", paramTypes[2], ex);
                transformedInputAlignment = null;
            }
        }else{
            transformedInputAlignment = getTransformedObjectGivenOne(inputAlignment, paramTypes[2], new Properties());
            if(transformedInputAlignment == null)
                return null;
        }
        
        Object transformedParameter = null;
        if(inputAlignment == null){
            //try to create an instance
            try{
                transformedParameter = paramTypes[3].newInstance();
            }catch(IllegalAccessException | InstantiationException | ExceptionInInitializerError | SecurityException ex){
                LOGGER.warn("The optional params parameter is null and thus a new instance of type {} was created which did not worked out. Try to call the matcher with null value.", paramTypes[2], ex);
                transformedParameter = null;
            }
        }else{
            transformedParameter = getTransformedObjectGivenOne(parameter, paramTypes[3], new Properties());
            if(transformedParameter == null)
                return null;
        }
        
        return matcher.match(transformedSource, transformedTarget, transformedInputAlignment, transformedParameter);
    }
    
    private static Object getTransformedObject(Iterable<Object> sourceObjects, Class<?> targetType, Properties transformationProperties){
        ObjectTransformationRoute route = TypeTransformerRegistry.transformObject(sourceObjects, targetType, transformationProperties);
        if(route == null){
            LOGGER.error("Did not find a transformation route from one of {} to {}. Please enhance the TypeTransformerRegistry with a corresponding TypeTranformer. The matcher is not called.", 
                    classRepresentation(sourceObjects), targetType);
            return null;
        }
        try {
            return route.getTransformedObject();
        } catch (Exception ex) {
            LOGGER.error("During conversion of object {} to class {} an exception occured. The matcher is not called.", route.getInitialObject(), targetType, ex);
            return null;
        }
    }
    private static Object getTransformedObjectGivenOne(Object sourceObject, Class<?> targetType, Properties transformationProperties){
        ObjectTransformationRoute route = TypeTransformerRegistry.transformObject(sourceObject, targetType, transformationProperties);
        if(route == null){
            LOGGER.error("Did not find a transformation route from one of {} to {}. Please enhance the TypeTransformerRegistry with a corresponding TypeTranformer. The matcher is not called.", 
                    sourceObject.getClass(), targetType);
            return null;
        }
        try {
            return route.getTransformedObject();
        } catch (Exception ex) {
            LOGGER.error("During conversion of object {} to class {} an exception occured. The matcher is not called.", route.getInitialObject(), targetType, ex);
            return null;
        }
    }
    
    private static Object makeInstance(Class<?> clazz){
        try{
            return clazz.newInstance();
        }catch(IllegalAccessException | InstantiationException | ExceptionInInitializerError | SecurityException ex){
            return null;
        }
    }
    
    /**
     * Search the method declared in the IMatcher interface.
     * It strats from the given class and moves on to the superclasses.
     * @param clazz the given class to start searching
     * @return the method from the IMatcher or null if non is found
     */
    private static Method getMatcherMethod(Class<?> clazz){
        //https://stackoverflow.com/questions/45729211/can-i-get-the-interface-that-a-method-is-implementing-via-java-reflection
        //TODO: maybe find the correct method which really implements the imterface and not only one method with the same name and parameter constellation
        Class<?> c = clazz;
        //search the method from the interface 
        while (c != null) {
            for(Method method : c.getDeclaredMethods()){
                //comprae name and parameter count
                if(method.getName().equals("match") && method.getParameterCount() == 4){
                    Class<?>[] paramTypes = method.getParameterTypes();
                    //at least one parameter has to be non Object - because the generic method is compiled into a method with only object params
                    if(paramTypes[0] != Object.class || paramTypes[1] != Object.class || paramTypes[2] != Object.class|| paramTypes[3] != Object.class){
                        // compare parameter types
                        if(paramTypes[0] == paramTypes[1] && method.getReturnType() == paramTypes[2]){
                            return method;
                        }
                    }
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }
    
    private static String classRepresentation(Iterable<Object> objects){
        Set<Class<?>> classes = new HashSet();
        for(Object o : objects){
            classes.add(o.getClass());
        }
        return classes.toString();
    }
}
