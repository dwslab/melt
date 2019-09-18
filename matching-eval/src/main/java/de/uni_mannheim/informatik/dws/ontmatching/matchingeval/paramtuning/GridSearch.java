package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.paramtuning;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.Executor;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutorParallel;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
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
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Gridsearch for ontology matching with arbitrary amount of parameter and values to optimize.
 * Important: when using parallel processing, ensure that the matcher doesnÄt write to the same results file.
 * @author Sven Hertling
 */
public class GridSearch {

    private static final Logger logger = LoggerFactory.getLogger(GridSearch.class);
    
    private Class<? extends IOntologyMatchingToolBridge> matcher;
    private String matcherName;
    private List<String> paramName;
    private List<List<Object>> paramValues;
    
    public GridSearch(Class<? extends IOntologyMatchingToolBridge> matcher, String matcherName){
        this.matcher = matcher;
        this.matcherName = matcherName;
        this.paramName = new ArrayList<>();
        this.paramValues = new ArrayList<>();
    }
    
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
            this.paramName.add(entry.getKey());
            this.paramValues.add(entry.getValue());
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
        this.paramName.addAll(paramsNames);
        this.paramValues.addAll(paramsValues);
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
    public GridSearch addParameter(String name, List<Object> paramValues){
        this.paramName.add(name);
        this.paramValues.add(paramValues);
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
        this.paramName.add(name);
        this.paramValues.add(Arrays.asList(paramValues));
        return this;
    }
    
    
    public ExecutionResultSet runGridParallel(TestCase tc){
        return runGridParallel(tc, Runtime.getRuntime().availableProcessors());
    }

    public ExecutionResultSet runGridParallel(TestCase tc, int numberOfThreads){
        Map<String, IOntologyMatchingToolBridge> matchers = new HashMap<>();
        List<List<Object>> paramCombinations = cartesianProduct(0, this.paramValues);
        for(List<Object> paramSetting : paramCombinations){
            Collections.reverse(paramSetting); //TODO: optimze
            try {
                matchers.put(getMatcherNameWithSettings(paramSetting), getInstantiatedMatcher(paramSetting));
            } catch (InstantiationException|IllegalAccessException ex) {
                logger.error("Cannot instantiate new Matcher", ex);
            }
        }
        return new ExecutorParallel(numberOfThreads).run(Arrays.asList(tc), matchers);
    }
    
    public ExecutionResultSet runGridSequential(TestCase tc){
        Map<String, IOntologyMatchingToolBridge> matchers = new HashMap<>();
        List<List<Object>> paramCombinations = cartesianProduct(0, this.paramValues);
        for(List<Object> paramSetting : paramCombinations){
            Collections.reverse(paramSetting); //TODO: optimze
            try {
                matchers.put(getMatcherNameWithSettings(paramSetting), getInstantiatedMatcher(paramSetting));
            } catch (InstantiationException|IllegalAccessException ex) {
                logger.error("Cannot instantiate new Matcher", ex);
            }
        }
        return Executor.run(Arrays.asList(tc), matchers);
    }
    
    protected String getMatcherNameWithSettings(List<Object> paramValue){
        StringJoiner setting = new StringJoiner(",");
        for(int i=0; i < paramValue.size(); i++){     
            setting.add(this.paramName.get(i) + "=" + paramValue.get(i));
        }
        return String.format("%s (%s)", this.matcherName, setting.toString());
    }
    
    private IOntologyMatchingToolBridge getInstantiatedMatcher(List<Object> paramValue) throws InstantiationException, IllegalAccessException{
        //initialize matcher
        IOntologyMatchingToolBridge matcherInstance = this.matcher.newInstance();

        //set the parameter
        PropertyUtilsBean pub = new PropertyUtilsBean();
        for(int i=0; i < paramValue.size(); i++){
            try {
                pub.setNestedProperty(matcherInstance, this.paramName.get(i), paramValue.get(i));
            } catch (IllegalAccessException| InvocationTargetException|NoSuchMethodException ex) {
                logger.error("Cannot set property", ex);
            }
        }
        
        return matcherInstance;
    }
    
    private static void runMatcherSetting(TestCase tc, Class<? extends IOntologyMatchingToolBridge> matcher, List<String> paramName, List<Object> paramValue){
        //initialize matcher
        IOntologyMatchingToolBridge matcherInstance = null;
        try {
            matcherInstance = matcher.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            logger.error("Cannot instantiate new Matcher", ex);
            return;
        }
        //set the parameter
        PropertyUtilsBean pub = new PropertyUtilsBean();
        for(int i=0; i < paramValue.size(); i++){
            try {
                pub.setNestedProperty(matcherInstance, paramName.get(i), paramValue.get(i));
            } catch (IllegalAccessException| InvocationTargetException|NoSuchMethodException ex) {
                logger.error("Cannot set property", ex);
            }
        }
        
        //run the matcher:
        try {            
            matcherInstance.align(tc.getSource().toURL(), tc.getTarget().toURL());
        } catch (ToolBridgeException | MalformedURLException ex) {
            logger.error("Exception during matching", ex);
        }
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
