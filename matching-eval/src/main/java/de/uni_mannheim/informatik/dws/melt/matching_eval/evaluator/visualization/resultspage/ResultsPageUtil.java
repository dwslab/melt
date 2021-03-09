package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.visualization.resultspage;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ResourceType;
import de.uni_mannheim.informatik.dws.melt.matching_eval.refinement.TypeRefiner;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrix;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrixMetric;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ResultsPageUtil {


    private static final Logger LOGGER = LoggerFactory.getLogger(ResultsPageUtil.class);
    
    private DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols(Locale.US);
    private DecimalFormat formatOneDecimalPlace = new DecimalFormat("0.0", decimalSymbols);
    private DecimalFormat formatTwoDecimalPlace = new DecimalFormat("0.00", decimalSymbols);
    
    private List<String> refinementTypes = Arrays.asList("class", "property", "instance", "overall");
    private ExecutionResultSet results;
    private ConfusionMatrixMetric cmMetric;
    private List<String> matchers;
    private List<TestCase> testcases;
    private boolean isMicro;
    
    /**
     * Constructor
     * @param results the execution result set
     * @param isMicro true means to compute micro, false means macro
     */
    public ResultsPageUtil(ExecutionResultSet results, boolean isMicro){
        this.results = results;
        this.cmMetric = new ConfusionMatrixMetric();
        this.matchers = getOrderedMatchers();
        this.testcases = getOrderedTestCases();
        this.isMicro = isMicro;
    }
    
    private List<String> getOrderedMatchers(){
        List<String> list = new ArrayList<>();
        this.results.getDistinctMatchers().forEach(list::add);
        list.sort(Comparator.comparing(x->x.toLowerCase()));
        return list;
    }
    
    private List<TestCase> getOrderedTestCases(){
        List<TestCase> list = new ArrayList<>();
        this.results.getDistinctTestCases().forEach(list::add);
        list.sort(Comparator.comparing(TestCase::getName));
        return list;
    }
    
    
    
    public String getSummedRuntime(Set<ExecutionResult> results){
        return formatTime(results.stream().mapToLong(ExecutionResult::getRuntime).sum());
    }
    public String getRuntime(ExecutionResult result){
        long seconds = result == null ? 0 : result.getRuntime();
        return formatTime(seconds);
    }
    public String formatTime(long seconds){
        return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60)); 
    }
    
    public String getAvgSystemSize(Set<ExecutionResult> results){
        return formatOneDecimalPlace.format(results.stream().mapToLong(e->e.getSystemAlignment().size()).average().orElse(0.0));
    }    
    public int getSystemSize(ExecutionResult result){
        if(result == null)
            return 0;
        return result.getSystemAlignment().size();
    }
    
    public String formatCmMeasure(double d){
        return formatTwoDecimalPlace.format(d);
    }
    
    public ConfusionMatrix getAverageConfusionMatrix(Set<ExecutionResult> results){
        if(isMicro){
            return this.cmMetric.getMicroAveragesForResults(results);        
        }else{
            return this.cmMetric.getMacroAveragesForResults(results);        
        }
    }
    
    public ConfusionMatrix getAverageConfusionMatrixOverAll(Set<ExecutionResult> results){
        if(isMicro){
            throw new NotImplementedException("Micro over all not implemented");
        }else{
            return this.cmMetric.getMacroAveragesForResults(results, this.testcases.size());     
        }
    }
    
    public ConfusionMatrix getConfusionMatrix(ExecutionResult result){
        if(result == null)
            return new ConfusionMatrix(new Alignment(), new Alignment(), new Alignment(), 0.0, 0.0);
        return this.cmMetric.get(result);
    }
    
    public ExecutionResult getMatcherRefinement(String matcher, TestCase testcase, String refinement){
        LOGGER.info("Compute refinements for {} testcase {} of track {}({}) for refinement {}", 
                matcher, testcase.getName(), testcase.getTrack().getName(), testcase.getTrack().getVersion(), refinement);
        if(refinement.equals("class")){
            return this.results.get(testcase, matcher, new TypeRefiner(ResourceType.CLASS));
        }else if(refinement.equals("property")){
            return this.results.get(testcase, matcher, new TypeRefiner(ResourceType.RDF_PROPERTY));
        }else if(refinement.equals("instance")){
            return this.results.get(testcase, matcher, new TypeRefiner(ResourceType.INSTANCE));
        }else if(refinement.equals("overall")){
            return this.results.get(testcase, matcher);
        }else{
            throw new IllegalArgumentException("refinement not one of class, property, instance, overall");
        }
    }
    
    public Set<ExecutionResult> getMatcherRefinement(String matcher, String refinement){
        LOGGER.info("Compute refinements for {} for refinement {}", matcher, refinement);
        if(refinement.equals("class")){
            return this.results.getGroup(matcher, new TypeRefiner(ResourceType.CLASS));
        }else if(refinement.equals("property")){
            return this.results.getGroup(matcher, new TypeRefiner(ResourceType.RDF_PROPERTY));
        }else if(refinement.equals("instance")){
            return this.results.getGroup(matcher, new TypeRefiner(ResourceType.INSTANCE));
        }else if(refinement.equals("overall")){
            return this.results.getGroup(matcher);
        }else{
            throw new IllegalArgumentException("refinement not of");
        }
    }
    
    public List<String> getRefinementTypes(){
        return refinementTypes;
    }
    
    
    public Iterable<String> getMatchers(){
        return matchers;
    }
    
    public Iterable<TestCase> getTestCases(){
        return this.testcases;
    }
}
