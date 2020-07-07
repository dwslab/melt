package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.instancelevel;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A matcher which matches classes based on already instance matches.
 * @see <a href="https://docserv.uni-duesseldorf.de/servlets/DerivateServlet/Derivate-18253/DissKatrinZaiß.pdf">https://docserv.uni-duesseldorf.de/servlets/DerivateServlet/Derivate-18253/DissKatrinZaiß.pdf</a>
 * @see <a>https://dbs.uni-leipzig.de/file/rev-ontomatch-dils-2007-final.pdf</a>
 * @see <a>http://cs.emis.de/LNI/Proceedings/Proceedings103/gi-proc-103-026.pdf</a>
 * @see <a>https://link.springer.com/content/pdf/10.1007%2Fs13740-012-0011-z.pdf</a>
 */
public class MatchClassBasedOnInstances extends MatcherYAAAJena{
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchClassBasedOnInstances.class);

    /**
     * Property connecting the instance to the hierarhcy (usually rdf:type).
     */
    protected Property instanceToHierarchyProperty;

    /**
     * Threshold for metric.
     * Values by metric larger than the threshold will be a match.
     */
    private double threshold;

    /**
     * Metric to be used.
     */
    private SimInstanceMetric metric;

    /**
     * What is an instance match? -&gt; Minimal confidence for instance matches.
     */
    private double instanceMinConfidence;

    /**
     * If set, all metrics will be logged.
     */
    private File debugFile;

    /**
     * Constructor for matching and writing a debug file.
     * @param instanceToHierarchyProperty Property connecting the instance to the hierarhcy like rdf:type
     * @param debugFile debug file
     */
    public MatchClassBasedOnInstances(Property instanceToHierarchyProperty, File debugFile) {
        this.instanceToHierarchyProperty = instanceToHierarchyProperty;
        this.debugFile = debugFile;
        this.threshold = 0.0;
        this.metric = SimInstanceMetric.BASE;
        this.instanceMinConfidence = 0.0;
    }

    /**
     * Constructor.
     * @param instanceToHierarchyProperty Property connecting the instance to the hierarhcy like rdf:type
     * @param threshold the threshold for metric. Values by metric larger than the threshold will be a match.
     * @param metric the metric for class comparison
     * @param instanceMinConfidence minimal confidence for instance matches
     */
    public MatchClassBasedOnInstances(Property instanceToHierarchyProperty, double threshold, SimInstanceMetric metric, double instanceMinConfidence) {
        this.instanceToHierarchyProperty = instanceToHierarchyProperty;
        this.threshold = threshold;
        this.metric = metric;
        this.instanceMinConfidence = instanceMinConfidence;
        this.debugFile = null;
    }
    
    /**
     * Constructor.
     * @param threshold the threshold for metric. Values by metric larger than the threshold will be a match.
     * @param metric the metric for class comparison
     * @param instanceMinConfidence minimal confidence for instance matches
     */
    public MatchClassBasedOnInstances(double threshold, SimInstanceMetric metric, double instanceMinConfidence) {
        this(RDF.type, threshold, metric, instanceMinConfidence);
    }
    
    /**
     * Constructor.
     * @param threshold the threshold for metric. Values by metric larger than the threshold will be a match.
     * @param metric the metric for class comparison
     */
    public MatchClassBasedOnInstances(double threshold, SimInstanceMetric metric) {
        this(threshold, metric, 0.0);
    }
    
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        inputAlignment.addAll(getClassMatches(source, target, inputAlignment));
        return inputAlignment;
    }
    
    public Alignment getClassMatches(OntModel source, OntModel target, Alignment inputAlignment) throws IOException{
        Map<Correspondence, ClassMatchInfo> classAlignment = new HashMap<>(); // a map from (class source, class target) to number of shared/matched instances
        Map<String, Integer> instanceCounts = new HashMap<>(); // a map from class to number of instances
        
        Set<String> sourceInstanceMappings = new HashSet();
        Set<String> targetInstanceMappings = new HashSet(); 
        for(Correspondence c : inputAlignment){
            if(c.getConfidence() < this.instanceMinConfidence)
                continue;
            Individual individualSource = source.getIndividual(c.getEntityOne());
            Individual individualTarget = target.getIndividual(c.getEntityTwo());
            if(individualSource == null || individualTarget == null){
                continue;
            }
            sourceInstanceMappings.add(c.getEntityOne());
            targetInstanceMappings.add(c.getEntityTwo());
            Set<String> sourceTypes = getClassesOfInstance(individualSource);
            Set<String> targetTypes = getClassesOfInstance(individualTarget);
            for(String sourceType : sourceTypes){
                instanceCounts.computeIfAbsent(sourceType, classUri -> getInstancesOfClass(source.getResource(classUri)).size());
                for(String targetType : targetTypes){
                    instanceCounts.computeIfAbsent(targetType, classUri -> getInstancesOfClass(target.getResource(classUri)).size());
                    Correspondence classCorrespondence = new Correspondence(sourceType, targetType);
                    classAlignment.computeIfAbsent(classCorrespondence, x->new ClassMatchInfo()).addInstanceMatch(c.getEntityOne(), c.getEntityTwo());
                }
            }
        }
        //in case of n:m instance mappings only the minimum amount of source or target instances is the number of 1:1 matches
        int instanceMappings = Math.min(sourceInstanceMappings.size(), targetInstanceMappings.size());
 
        saveValuesToFile(classAlignment, instanceCounts, instanceMappings); // DEBUG
        
        Alignment alignment = new Alignment(inputAlignment, false);
        for(Entry<Correspondence, ClassMatchInfo> t : classAlignment.entrySet()){
            int instancesOne = instanceCounts.getOrDefault(t.getKey().getEntityOne(), 0);
            int instancesTwo = instanceCounts.getOrDefault(t.getKey().getEntityTwo(), 0);
            int instancesOverlap = t.getValue().getOverlap();
            
            double simValue = 0.0;
            switch (this.metric) {
                case BASE:
                    simValue = getSimValueBase(instancesOverlap, instancesOne, instancesTwo);
                    break;                    
                case MIN:
                    simValue = getSimValueMin(instancesOverlap, instancesOne, instancesTwo);
                    break;
                case DICE:
                    simValue = getSimValueDice(instancesOverlap, instancesOne, instancesTwo);
                    break;
                case MATCH_BASED:
                    simValue = getSimValueMatchBased(instancesOverlap, instanceMappings);
                    break;
                default:
                    throw new IllegalArgumentException("Metric is not implmented");
            }
            if(simValue >= threshold){
                Correspondence c = t.getKey();
                c.setConfidence(simValue);
                alignment.add(c);
                //LOGGER.trace("Add correspondence with MatchClassBasedOnInstances " + c.toString());
            }
        }        
        return alignment;
    }
        
    //SIM metrics:
    
    private double getSimValueBase(int instancesOverlap, int instancesOne, int instancesTwo){
        if(instancesOverlap > 0)
            return 1.0;
        return 0.0;
    }
    
    private double getSimValueMin(int instancesOverlap, int instancesOne, int instancesTwo){
        int min = Math.min(instancesOne, instancesTwo);
        if(min == 0)
            return 0.0;
        return (double)instancesOverlap / (double) min;
    }
    
    private double getSimValueDice(int instancesOverlap, int instancesOne, int instancesTwo){
        if(instancesOne + instancesTwo == 0)
            return 0.0;
        return (double)(2 * instancesOverlap) / (double)(instancesOne + instancesTwo);
    }
        
    private double getSimValueMatchBased(int instancesOverlap, int allMatchedInstances){
        if(allMatchedInstances == 0)
            return 0.0;
        return (double)instancesOverlap / (double) allMatchedInstances;
    }
    
    //Util methods
    
    private void saveValuesToFile(Map<Correspondence, ClassMatchInfo> classAlignment, Map<String, Integer> instanceCounts, int instanceMappings) throws IOException {
        if(this.debugFile == null)
            return;
        List<Entry<Correspondence, Integer>> list = new ArrayList<>();
        for(Entry<Correspondence, ClassMatchInfo> entry : classAlignment.entrySet()){
            list.add(new SimpleEntry(entry.getKey(), entry.getValue().getOverlap()));
        }
        list.sort(Entry.comparingByValue());
        Collections.reverse(list);
        DecimalFormat df = new DecimalFormat("#0.000"); 
        
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(this.debugFile))){
            writer.write("source URI;target URI;source instances;target instances;overlap;base;min;dice;matchbased");
            writer.newLine();
            for(Entry<Correspondence, Integer> t : list){
                int instancesOne = instanceCounts.getOrDefault(t.getKey().getEntityOne(), 0);
                int instancesTwo = instanceCounts.getOrDefault(t.getKey().getEntityTwo(), 0);
                int instancesOverlap = t.getValue();
                
                writer.write(t.getKey().getEntityOne() + ";" + t.getKey().getEntityTwo() + ";" + 
                        instancesOne + ";" + instancesTwo + ";" + instancesOverlap + ";" +
                        df.format(getSimValueBase(instancesOverlap, instancesOne, instancesTwo)) + ";" + 
                        df.format(getSimValueMin(instancesOverlap, instancesOne, instancesTwo)) + ";" + 
                        df.format(getSimValueDice(instancesOverlap, instancesOne, instancesTwo)) + ";" + 
                        df.format(getSimValueMatchBased(instancesOverlap, instanceMappings)));
                writer.newLine();
            }
        }
    }
    
    private Set<Resource> getInstancesOfClass(Resource clazz){
        return clazz.getModel().listSubjectsWithProperty(this.instanceToHierarchyProperty, clazz).toSet();
    }
    
    private Set<String> getClassesOfInstance(Resource resource){
        Set<String> classes = new HashSet<>();
        StmtIterator i = resource.listProperties(this.instanceToHierarchyProperty);
        while(i.hasNext()){
            RDFNode node = i.next().getObject();
            if(node.isURIResource() == false)
                continue;
            if(node.equals(OWL.Thing))
                continue;
            classes.add(node.asResource().getURI());            
        }
        return classes;
    }
    
    //getter setter

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public SimInstanceMetric getMetric() {
        return metric;
    }

    public void setMetric(SimInstanceMetric metric) {
        this.metric = metric;
    }

    public double getInstanceMinConfidence() {
        return instanceMinConfidence;
    }

    public void setInstanceMinConfidence(double instanceMinConfidence) {
        this.instanceMinConfidence = instanceMinConfidence;
    }

    public File getDebugFile() {
        return debugFile;
    }

    public void setDebugFile(File debugFile) {
        this.debugFile = debugFile;
    }
    
    class ClassMatchInfo{
        private final Set<String> sourceInstances;
        private final Set<String> targetInstances;
        
        public ClassMatchInfo(){
            this.sourceInstances = new HashSet();
            this.targetInstances = new HashSet();
        }
        
        public void addInstanceMatch(String source, String target){
            sourceInstances.add(source);
            targetInstances.add(target);
        }
        
        public int getOverlap(){
            //in case of n:m instance mappings only the minimum amount of resource is the number of the intersection
            return Math.min(sourceInstances.size(), targetInstances.size());
        }
    }
}
