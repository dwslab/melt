package de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutorParallel;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.DefaultExtensions;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * GridSearch for ontology matching with an arbitrary amount of parameter and values to optimize.
 * Important: when using parallel processing, ensure that the matcher does not write to the same results file.
 *
 * @author Sven Hertling
 */
public class GridSearch {


    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GridSearch.class);

    /**
     * constructor constant to be used in {@link GridSearch#addParameter(String, List)}.
     */
    private static final String CONSTRUCTOR = "constructor";

    /**
     * The matcher under evaluation.
     */
    private Class<?> matcher;
    private String matcherName;
    private List<String> paramName;
    private List<List<Object>> paramValues;

    private List<Class<?>> paramTypes;

    /**
     * Constructor
     * @param matcher The matcher for which the grid search shall be performed.
     * @param matcherName Name of the matcher.
     */
    public GridSearch(Class<?> matcher, String matcherName){
        this.matcher = matcher;
        this.matcherName = matcherName;
        this.paramName = new ArrayList<>();
        this.paramValues = new ArrayList<>();
        this.paramTypes = new ArrayList<>();
    }

    /**
     * Constructor
     * @param matcher The matcher for which the grid search shall be performed.
     */
    public GridSearch(Class<?> matcher){
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
     * @param name Possibly nested name of the property to be modified.
     * @param paramValues Values to which the property is to be set.
     * @return GridSearch object (for builder pattern).
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
        this.paramTypes.add(paramValues.get(0).getClass());
        return this;
    }
    
    /**
     * In case the matcher needs parameters in the constructor which should be changed in each run, use this method.
     * If the constructor of the matcher has n parameters you should call this method n times with values which should be tried out.
     * If the constructor looks like this:
     * <pre>{@code 
     * public MyMatcher(int number, String text){
     * }
     * }</pre>
     * then you should call it like that (order matters!)
     * <pre>{@code
     * gridsearch.addConstructorParameter(1,2,3);
     * gridsearch.addConstructorParameter("x", "y");
     * }</pre>
     * to have the following calls of the constructor:
     * <pre>{@code
     * new MyMatcher(1, "x")
     * new MyMatcher(2, "x")
     * new MyMatcher(3, "x")
     * new MyMatcher(1, "y")
     * new MyMatcher(2, "y")
     * new MyMatcher(3, "y")
     * }</pre>
     * In case you just want to provide constructor parameters which should not be changed, use {@link #addStaticConstructorParameter(java.lang.Object...) }
     * @param paramValues The parameters for the constructor in the correct order.
     * @return Edited {@code GridSearch} instance.
     */
    public GridSearch addConstructorParameter(List<Object> paramValues){
        return this.addConstructorParameter(paramValues, paramValues.get(0).getClass());
    }

    /**
     * In case the matcher needs parameters in the constructor which should be changed in each run, use this method.
     * If the constructor of the matcher has n parameters you should call this method n times with values which should be tried out.
     * If the constructor looks like this:
     * <pre>{@code 
     * public MyMatcher(int number, String text){
     * }
     * }</pre>
     * then you should call it like that (order matters!)
     * <pre>{@code
     * gridsearch.addConstructorParameter(Arrays.asList(1,2,3), Integer.class);
     * gridsearch.addConstructorParameter(Arrays.asList("x", "y"), String.class);
     * }</pre>
     * to have the following calls of the constructor:
     * <pre>{@code
     * new MyMatcher(1, "x")
     * new MyMatcher(2, "x")
     * new MyMatcher(3, "x")
     * new MyMatcher(1, "y")
     * new MyMatcher(2, "y")
     * new MyMatcher(3, "y")
     * }</pre>
     * In case you just want to provide constructor parameters which should not be changed, use {@link #addStaticConstructorParameter(java.lang.Object...) }
     * @param paramValues The parameters for the constructor in the correct order.
     * @param clazz The type of all the values.
     * @return Edited {@code GridSearch} instance.
     */
    public GridSearch addConstructorParameter(List<Object> paramValues, Class<?> clazz){
        this.paramName.add(CONSTRUCTOR);
        this.paramValues.add(paramValues);
        this.paramTypes.add(clazz);
        return this;
    }

    /**
     * In case you need parameters for the matcher constructor, but do not want the change them in each run, but keep them the same, then use this method.
     * In case you supply multiple values, the constructor should also need the same amount of parameters (position matters!).
     * This differ to {@link #addConstructorParameter(java.util.List)} because it does not change the constructor paramters in each run.
     * In case the constructor is not found (java.lang.NoSuchMethodException) then use {@link #addStaticConstructorParameter(java.util.List, java.util.List) } and specify the types explicitly.
     * @param paramValues The parameters for the constructor in the correct order.
     * @return Edited {@code GridSearch} instance.
     */
    public GridSearch addStaticConstructorParameter(Object... paramValues){
        for(Object param : paramValues){
            this.paramName.add(CONSTRUCTOR);
            this.paramValues.add(Arrays.asList(param));
            this.paramTypes.add(param.getClass());
        }
        return this;
    }
    
    /**
     * In case you need parameters for the matcher constructor, but do not want the change them in each run, but keep them the same, then use this method.
     * In case you supply multiple values, the constructor should also need the same amount of parameters (position matters!).
     * This differ to {@link #addConstructorParameter(java.util.List)} because it does not change the constructor paramters in each run.
     * @param paramValues The parameters values for the constructor in the correct order.
     * @param paramTypes The parameter type of the constructor in the correct order.
     * @return Edited {@code GridSearch} instance.
     */
    public GridSearch addStaticConstructorParameter(List<Object> paramValues, List<Class<?>> paramTypes){
        if(paramTypes.size() != paramValues.size()){
            throw new IllegalArgumentException("The size of paramTypes and paramValues is not equal.");
        }
        for(int i=0; i < paramTypes.size(); i++){
            this.paramName.add(CONSTRUCTOR);
            this.paramValues.add(Arrays.asList(paramValues.get(i)));
            this.paramTypes.add(paramTypes.get(i));
        }
        return this;
    }

    /**
     * Run in parallel on {@link TestCase}.
     * @param testCase The test case to use.
     * @return {@link ExecutionResultSet} instance.
     */
    public ExecutionResultSet runGridParallel(TestCase testCase){
        return runGridParallel(testCase, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Run in parallel on multiple {@link TestCase}.
     * @param testCases The test cases to use.
     * @return {@link ExecutionResultSet} instance.
     */
    public ExecutionResultSet runGridParallel(List<TestCase> testCases){
        return runGridParallel(testCases, Runtime.getRuntime().availableProcessors());
    }
    
    public ExecutionResultSet runGridParallel(Track track){
        return runGridParallel(track, Runtime.getRuntime().availableProcessors());
    }

    public ExecutionResultSet runGridParallelTracks(List<Track> tracks){
        return runGridParallelTrack(tracks, Runtime.getRuntime().availableProcessors());
    }

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

    /**
     * Run sequentially on a {@link TestCase}.
     * @param testCase The test case to use.
     * @return {@link ExecutionResultSet} instance.
     */
    public ExecutionResultSet runGridSequential(TestCase testCase){
        return updateExecutionResultSet(Executor.run(Arrays.asList(testCase), getMatcherConfigurations()));
    }

    /**
     * Run sequentially on multiple {@link TestCase}.
     * @param testCases The test cases to use.
     * @return {@link ExecutionResultSet} instance.
     */
    public ExecutionResultSet runGridSequential(List<TestCase> testCases){
        return updateExecutionResultSet(Executor.run(testCases, getMatcherConfigurations()));
    }

    /**
     * Run sequentially a {@link Track}.
     * @param track The track to use.
     * @return {@link ExecutionResultSet} instance.
     */
    public ExecutionResultSet runGridSequential(Track track){
        return updateExecutionResultSet(Executor.run(track, getMatcherConfigurations()));
    }

    /**
     * Run sequentially on multiple {@link Track}.
     * @param tracks The tracks to use.
     * @return {@link ExecutionResultSet} instance.
     */
    public ExecutionResultSet runGridSequentialTracks(List<Track> tracks){
        return updateExecutionResultSet(Executor.runTracks(tracks, getMatcherConfigurations()));
    }
    
    /**
     * Updates the execution result set with configuration attributes in the extension of the alignment.
     * @param set The execution result set to be updated.
     * @return The updated execution result set.
     */
    public ExecutionResultSet updateExecutionResultSet(ExecutionResultSet set){
        for(List<Object> paramSetting : cartesianProduct(0, this.paramValues)){
            Collections.reverse(paramSetting); //TODO: optimize
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
    
    
    public Map<String, Object> getMatcherConfigurations(){
        Map<String, Object> matchers = new HashMap<>();
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
    
    private Object getInstantiatedMatcher(List<Object> paramValue) throws ReflectiveOperationException {        
        List<Object> constructorValues = new ArrayList<>();
        List<Class<?>> constructorTypes = new ArrayList<>();
        for(int i=0; i < this.paramName.size(); i++){
            if(isConstructorParameter(this.paramName.get(i))){
                Object oneParamValue = paramValue.get(i);
                constructorValues.add(oneParamValue);
                constructorTypes.add(this.paramTypes.get(i));
            }
        }
        
        Object matcherInstance = null;
        if(constructorValues.isEmpty()){
            matcherInstance = this.matcher.newInstance();
        } else {
            Constructor<?> constructor = this.matcher.getConstructor(constructorTypes.toArray(new Class<?>[constructorTypes.size()]));
            if(constructor == null)
                throw new NoSuchMethodException("Constructor with param types" + constructorTypes.toString() + " not found.");
            matcherInstance = constructor.newInstance(constructorValues.toArray(new Object[constructorValues.size()]));
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
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException  ex) {
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
