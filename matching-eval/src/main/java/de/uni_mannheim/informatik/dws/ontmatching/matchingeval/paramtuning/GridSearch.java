package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.paramtuning;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.Executor;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutorParallel;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.DefaultExtensions;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.logging.Level;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Gridsearch for ontology matching with arbitrary amount of parameter and values to optimize.
 * Important: when using parallel processing, ensure that the matcher doesnÄt write to the same results file.
 * @author Sven Hertling
 */
public class GridSearch {

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GridSearch.class);
    private static final String CONSTRUCTOR = "constructor";

    /**
     * The matcher under evaluation.
     */
    private Class<? extends IOntologyMatchingToolBridge> matcher;
    private String matcherName;
    private List<String> paramName;
    private List<List<Object>> paramValues;

    /**
     * Constructor
     * @param matcher The matcher for which the grid search shall be performed.
     * @param matcherName Name of the matcher.
     */
    public GridSearch(Class<? extends IOntologyMatchingToolBridge> matcher, String matcherName){
        this.matcher = matcher;
        this.matcherName = matcherName;
        this.paramName = new ArrayList<>();
        this.paramValues = new ArrayList<>();
    }

    /**
     * Constructor
     * @param matcher The matcher for which the grid search shall be performed.
     */
    public GridSearch(Class<? extends IOntologyMatchingToolBridge> matcher){
        this(matcher, Executor.getMatcherName(matcher));
    }
    
    /**
     * Adds a multiple parameter searches. The name (key of the map) can be a nested property.
     * Example values for parameter "name" are:
     * <ul>
     * <li> "a" -- sets the value of property a of the specified bean </li>
     * <li> "a.b" -- gets the value of property a of the specified bean,
     * then on that object sets the value of property b.</li>
     * <li> "a(key)" -- sets a value of mapped-property a on the specified
     * bean. This effectively means bean.setA("key").</li>
     * <li> "a[3]" -- sets a value of indexed-property a on the specified
     * bean. This effectively means bean.setA(3).</li>
     * </ul>
     * (this is from org.apache.commons.beanutils.PropertyUtilsBean)
     * @param paramsToValuesGrid a map where the key is a possibly nested name of the property to be modified and the values to which the property is to be set
     * @return GridSearch object (for builder pattern)
     */
    public GridSearch addParameters(Map<String, List<Object>> paramsToValuesGrid){
        for(Entry<String, List<Object>> entry : paramsToValuesGrid.entrySet()){
            addParameter(entry.getKey(), entry.getValue());
        }
        return this;
    }
    
    /**
     * Adds a multiple parameter searches. The names can be a nested property.
     * Example values for parameter "name" are:
     * <ul>
     * <li> "a" -- sets the value of property a of the specified bean </li>
     * <li> "a.b" -- gets the value of property a of the specified bean,
     * then on that object sets the value of property b.</li>
     * <li> "a(key)" -- sets a value of mapped-property a on the specified
     * bean. This effectively means bean.setA("key").</li>
     * <li> "a[3]" -- sets a value of indexed-property a on the specified
     * bean. This effectively means bean.setA(3).</li>
     * </ul>
     * (this is from org.apache.commons.beanutils.PropertyUtilsBean)
     * @param paramsNames Possibly nested name of the property to be modified
     * @param paramsValues Values to which the property is to be set
     * @return GridSearch object (for builder pattern)
     */
    public GridSearch addParameters(List<String> paramsNames, List<List<Object>> paramsValues){
        if(paramsNames.size() != paramsValues.size()){
            throw new IllegalArgumentException("The size of parameter name and parameter values is not equal.");
        }
        for(int i=0; i < paramsNames.size(); i++){
            addParameter(paramsNames.get(i), paramsValues.get(i));
        }
        return this;
    }
    
    /**
     * Adds a parameter search. The name can be a nested property.
     * Example values for parameter "name" are:
     * <ul>
     * <li> "a" -- sets the value of property a of the specified bean </li>
     * <li> "a.b" -- gets the value of property a of the specified bean,
     * then on that object sets the value of property b.</li>
     * <li> "a(key)" -- sets a value of mapped-property a on the specified
     * bean. This effectively means bean.setA("key").</li>
     * <li> "a[3]" -- sets a value of indexed-property a on the specified
     * bean. This effectively means bean.setA(3).</li>
     * </ul>
     * (this is from org.apache.commons.beanutils.PropertyUtilsBean)
     * @param name Possibly nested name of the property to be modified
     * @param paramValues Values to which the property is to be set
     * @return GridSearch object (for builder pattern)
     */
    public GridSearch addParameter(String name, Object... paramValues){
        return addParameter(name, Arrays.asList(paramValues));
    }
    
    /**
     * Adds a parameter search. The name can be a nested property.
     * Example values for parameter "name" are:
     * <ul>
     * <li> "a" -- sets the value of property a of the specified bean </li>
     * <li> "a.b" -- gets the value of property a of the specified bean,
     * then on that object sets the value of property b.</li>
     * <li> "a(key)" -- sets a value of mapped-property a on the specified
     * bean. This effectively means bean.setA("key").</li>
     * <li> "a[3]" -- sets a value of indexed-property a on the specified
     * bean. This effectively means bean.setA(3).</li>
     * </ul>
     * (this is from org.apache.commons.beanutils.PropertyUtilsBean)
     * @param name Possibly nested name of the property to be modified
     * @param paramValues Values to which the property is to be set
     * @return GridSearch object (for builder pattern)
     */
    public GridSearch addParameter(String name, List<Object> paramValues){
        this.paramName.add(name);
        this.paramValues.add(paramValues);
        return this;
    }
    
    /**
     * If the constructor value shall be held constant, use fill only one value
     * in the list.
     * Position matters!
     * @param paramValues
     * @return 
     */
    public GridSearch addConstructorParameter(List<Object> paramValues){
        this.paramName.add(CONSTRUCTOR);
        this.paramValues.add(paramValues);
        return this;
    }
    
    

    //run grid parallel 
    public ExecutionResultSet runGridParallel(TestCase testCase){
        return runGridParallel(testCase, Runtime.getRuntime().availableProcessors());
    }
    
    public ExecutionResultSet runGridParallel(List<TestCase> testCases){
        return runGridParallel(testCases, Runtime.getRuntime().availableProcessors());
    }
    
    public ExecutionResultSet runGridParallel(Track track){
        return runGridParallel(track, Runtime.getRuntime().availableProcessors());
    }

    public ExecutionResultSet runGridParallelTracks(List<Track> tracks){
        return runGridParallelTrack(tracks, Runtime.getRuntime().availableProcessors());
    }
    
    
    //run grid parallel 
    public ExecutionResultSet runGridParallel(TestCase testCase, int numberOfThreads){
        return updateExecutionResultSet(new ExecutorParallel(numberOfThreads).run(Arrays.asList(testCase), getMatcherConfigurations()));
    }
    
    public ExecutionResultSet runGridParallel(List<TestCase> testCases, int numberOfThreads){
        return updateExecutionResultSet(new ExecutorParallel(numberOfThreads).run(testCases, getMatcherConfigurations()));
    }
    
    public ExecutionResultSet runGridParallel(Track track, int numberOfThreads){
        return updateExecutionResultSet(new ExecutorParallel(numberOfThreads).run(track, getMatcherConfigurations()));
    }
    
    public ExecutionResultSet runGridParallelTrack(List<Track> tracks, int numberOfThreads){
        return updateExecutionResultSet(new ExecutorParallel(numberOfThreads).runTracks(tracks, getMatcherConfigurations()));
    }
    
    
    //run Grid Sequential
    public ExecutionResultSet runGridSequential(TestCase tc){
        return updateExecutionResultSet(Executor.run(Arrays.asList(tc), getMatcherConfigurations()));
    }
    
    public ExecutionResultSet runGridSequential(List<TestCase> tc){
        return updateExecutionResultSet(Executor.run(tc, getMatcherConfigurations()));
    }
    
    public ExecutionResultSet runGridSequential(Track track){
        return updateExecutionResultSet(Executor.run(track, getMatcherConfigurations()));
    }
    
    public ExecutionResultSet runGridSequentialTracks(List<Track> tracks){
        return updateExecutionResultSet(Executor.runTracks(tracks, getMatcherConfigurations()));
    }
    
    /**
     * Updates the execution result set with configuration attributes in the extension of the alignment.
     * @param set
     * @return 
     */
    public ExecutionResultSet updateExecutionResultSet(ExecutionResultSet set){
        for(List<Object> paramSetting : cartesianProduct(0, this.paramValues)){
            Collections.reverse(paramSetting); //TODO: optimze
            int counter = 0;
            for(ExecutionResult result : set.getGroup(getMatcherNameWithSettings(paramSetting))){
                for(int i=0; i < paramSetting.size(); i++){
                    String name = this.paramName.get(i);
                    if(isConstructorParameter(name)){
                        name += "_" + counter;
                        counter++;
                    }
                    String key = DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + name;
                    result.getSystemAlignment().addExtensionValue(key, paramSetting.get(i).toString());                           
                }
            }
        }
        return set;
    }
    
    
    public Map<String, IOntologyMatchingToolBridge> getMatcherConfigurations(){
        Map<String, IOntologyMatchingToolBridge> matchers = new HashMap<>();
        List<List<Object>> paramCombinations = cartesianProduct(0, this.paramValues);
        for(List<Object> paramSetting : paramCombinations){
            Collections.reverse(paramSetting); //TODO: optimze
            try {
                matchers.put(getMatcherNameWithSettings(paramSetting), getInstantiatedMatcher(paramSetting));
            } catch (ReflectiveOperationException ex) {
                LOGGER.error("Cannot instantiate new Matcher", ex);
            }
        }
        return matchers;
    }
    
    
    protected String getMatcherNameWithSettings(List<Object> paramValue){
        int counter = 0;
        StringJoiner setting = new StringJoiner(",");
        for(int i=0; i < paramValue.size(); i++){  
            String name = this.paramName.get(i);
            if(isConstructorParameter(name)){
                name += "_" + counter;
                counter++;
            }
            setting.add(name + "=" + paramValue.get(i));
        }
        return String.format("%s (%s)", this.matcherName, setting.toString());
    }
    
    private IOntologyMatchingToolBridge getInstantiatedMatcher(List<Object> paramValue) throws ReflectiveOperationException {        
        List<Object> constructorValues = new ArrayList();
        List<Class<?>> constructorTypes = new ArrayList<>();
        for(int i=0; i < this.paramName.size(); i++){
            if(isConstructorParameter(this.paramName.get(i))){
                Object oneParamValue = paramValue.get(i);
                constructorValues.add(oneParamValue);
                constructorTypes.add(oneParamValue.getClass());
            }
        }
        
        IOntologyMatchingToolBridge matcherInstance = null;
        if(constructorValues.isEmpty()){
            matcherInstance = this.matcher.newInstance();
        }else{
            Constructor constructor = this.matcher.getConstructor(constructorTypes.toArray(new Class<?>[constructorTypes.size()]));
            if(constructor == null)
                throw new NoSuchMethodException("Constructor with param types" + constructorTypes.toString() + " not found.");
            matcherInstance = (IOntologyMatchingToolBridge)constructor.newInstance(constructorValues.toArray(new Object[constructorValues.size()]));
        }
        
        if(matcherInstance == null)
            throw new InstantiationException("Matcher is null and could not be instantiated.");
        
        //set the parameter
        PropertyUtilsBean pub = new PropertyUtilsBean();
        for(int i=0; i < paramValue.size(); i++){
            if(isConstructorParameter(this.paramName.get(i)))
                continue;
            try {
                pub.setNestedProperty(matcherInstance, this.paramName.get(i), paramValue.get(i));
            } catch (IllegalAccessException| InvocationTargetException|NoSuchMethodException ex) {
                LOGGER.error("Cannot set property", ex);
            }
        }
        
        return matcherInstance;
    }
    
    private static boolean isConstructorParameter(String parameterName){
        return parameterName.toLowerCase().equals(CONSTRUCTOR);
    }
      
    private static List<List<Object>> cartesianProduct(int index, List<List<Object>> paramValues) {
        List<List<Object>> result = new ArrayList<>();
        if (index >= paramValues.size()) {
            result.add(new ArrayList<>());
        }else{
            for(Object obj : paramValues.get(index)) {
                for(List<Object> list : cartesianProduct(index + 1, paramValues)){
                    list.add(obj);
                    result.add(list);
                }
            }
        }
        return result;
    }
        
    //https://stackoverflow.com/questions/714108/cartesian-product-of-arbitrary-sets-in-java
    //https://stackoverflow.com/questions/23418855/how-to-get-cartesian-product-from-map-of-string-set
    //https://dzone.com/articles/java-cartesian-iterator-array
    //https://stackoverflow.com/questions/30585064/grid-search-better-performance-using-threads
}
