package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherMultiSourceCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ReflexiveCorrespondenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.TransitiveClosure;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the result of a multi source matcher execution.
 */
public class ExecutionResultMultiSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionResultMultiSource.class);
    
    private final Object alignment;
    private final Object parameters;
    private final Object matcher;
    private final String matcherName;
    private final List<URL> allGraphs;
    private final List<TestCase> testCases;
    /**
     * The total runtime in nanoseconds
     */
    private long totalRuntime;
    private boolean computeTransitiveClosure;
    private final Partitioner partitioner;

    
    public ExecutionResultMultiSource(Object alignment, String matcherName, List<TestCase> testCases, long totalRuntime, boolean computeTransitiveClosure, Partitioner partitioner) {
        this(alignment, null, null, matcherName, new ArrayList<>(), testCases, totalRuntime, computeTransitiveClosure, partitioner);
    }
    
    
    public ExecutionResultMultiSource(Object alignment, Object parameters, Object matcher, String matcherName, List<URL> allGraphs, List<TestCase> testCases, long totalRuntime, Partitioner partitioner) {
        this(alignment, parameters, matcher, matcherName, allGraphs, testCases, totalRuntime, GenericMatcherMultiSourceCaller.needsTransitiveClosureForEvaluation(matcher), partitioner);
    }
    
    public ExecutionResultMultiSource(Object alignment, Object parameters, Object matcher, String matcherName, List<URL> allGraphs, List<TestCase> testCases, long totalRuntime, boolean computeTransitiveClosure, Partitioner partitioner) {
        this.alignment = alignment;
        this.parameters = parameters;
        this.matcher = matcher;
        this.matcherName = matcherName;
        this.allGraphs = allGraphs;
        this.testCases = testCases;
        this.totalRuntime = totalRuntime;
        this.computeTransitiveClosure = computeTransitiveClosure;
        this.partitioner = partitioner;
    }
    
    public ExecutionResultSet toExecutionResultSet(){
        Alignment fullAlignment;
        try {
            fullAlignment = TypeTransformerRegistry.getTransformedObject(this.alignment, Alignment.class);
        } catch (TypeTransformationException ex) {
            LOGGER.error("Could not transform alignemnt to Alignment class. Return empty ExecutionResultSet.", ex);
            return new ExecutionResultSet();
        }
        //remove reflexive edges
        fullAlignment = ReflexiveCorrespondenceFilter.removeReflexiveCorrespondences(fullAlignment);
        
        Map<TestCase, Alignment> testcaseToAlignment = new HashMap<>();
        if(computeTransitiveClosure){
            TransitiveClosure<String> alignmentClosure = new TransitiveClosure<>();
            for(Correspondence c : fullAlignment){
                alignmentClosure.add(c.getEntityOne(), c.getEntityTwo());
            }
            for(Set<String> sameAs : alignmentClosure.getClosure()){
                Map<TestCase, SourceTargetURIs> map = partitioner.partition(sameAs);
                for(Map.Entry<TestCase, SourceTargetURIs> entry : map.entrySet()){
                    SourceTargetURIs sourceTargetUris = entry.getValue();
                    if(sourceTargetUris.containsSourceAndTarget() == false)
                        continue;
                    Alignment alignment = testcaseToAlignment.computeIfAbsent(entry.getKey(), __->new Alignment());
                    for(String sourceURI : sourceTargetUris.getSourceURIs()){
                        for(String targetURI : sourceTargetUris.getTargetURIs()){
                            //TODO: confidence extensions etc
                            alignment.add(sourceURI, targetURI);
                        }
                    }
                }
            }
        }else{
            for(Correspondence c : fullAlignment){
                Map<TestCase, SourceTargetURIs> map = partitioner.partition(Arrays.asList(c.getEntityOne(), c.getEntityTwo()));
                for(Map.Entry<TestCase, SourceTargetURIs> entry : map.entrySet()){
                    SourceTargetURIs sourceTargetUris = entry.getValue();
                    if(sourceTargetUris.containsSourceAndTarget() == false)
                        continue;
                    Alignment alignment = testcaseToAlignment.computeIfAbsent(entry.getKey(), __->new Alignment());
                    for(String sourceURI : sourceTargetUris.getSourceURIs()){
                        for(String targetURI : sourceTargetUris.getTargetURIs()){
                            alignment.add(sourceURI, targetURI, c.getConfidence(), c.getRelation(), c.getExtensions());
                        }
                    }
                }
            }
        }
        
        long runtimePerTestCase = totalRuntime / testCases.size();
        
        ExecutionResultSet resultSet = new ExecutionResultSet();
        for(TestCase testCase : testCases){
            resultSet.add(new ExecutionResult(
                    testCase, 
                    matcherName, 
                    null,
                    runtimePerTestCase,
                    testcaseToAlignment.getOrDefault(testCase, new Alignment()), 
                    testCase.getParsedReferenceAlignment(),
                    null,
                    null
            ));
        }
        return resultSet;
    }

    public Object getAlignment() {
        return alignment;
    }
    
    public <T> T getAlignment(Class<T> clazz){
        return getAlignment(clazz, new Properties());
    }
    
    public <T> T getAlignment(Class<T> clazz, Properties parameters){
        try {
            return TypeTransformerRegistry.getTransformedObject(this.alignment, clazz, parameters);
        } catch (TypeTransformationException ex) {
            LOGGER.error("Could not transform alignment to {}. Returning null.", clazz, ex);
            return null;
        }
    }

    public Object getParameters() {
        return parameters;
    }
    
    public <T> T getParameters(Class<T> clazz){
        return getParameters(clazz, new Properties());
    }
    
    public <T> T getParameters(Class<T> clazz, Properties parameters){
        try {
            return TypeTransformerRegistry.getTransformedObject(this.parameters, clazz, parameters);
        } catch (TypeTransformationException ex) {
            LOGGER.error("Could not transform parameters to {}. Returning null.", clazz, ex);
            return null;
        }
    }

    public Object getMatcher() {
        return matcher;
    }

    public String getMatcherName() {
        return matcherName;
    }

    public List<URL> getAllGraphs() {
        return allGraphs;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public long getTotalRuntime() {
        return totalRuntime;
    }
    
    public void addRuntime(long additonalRuntime){
        this.totalRuntime += additonalRuntime;
    }
    
    /**
     * Sets the value of compute transitive closure to true, if the parameter is true.
     * Otherwise it still uses the old value.
     * @param computeTransitiveClosure the new value if the transitive closure should be computed or not.
     */
    public void updateComputeTransitiveClosure(boolean computeTransitiveClosure){
        if(computeTransitiveClosure){
            this.computeTransitiveClosure = true;
        }
    }

    public boolean isComputeTransitiveClosure() {
        return computeTransitiveClosure;
    }

    public Partitioner getPartitioner() {
        return partitioner;
    }
}
