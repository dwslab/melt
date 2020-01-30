package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.instancelevel;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
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
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
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
     * Threshold for metric.
     * Values by metric larger than the threshold will be a match.
     */
    private double threshold;

    /**
     * Metric to be used.
     */
    private SimInstanceMetric metric;

    /**
     * What is an instance match? --> Minimal confidence for instance matches.
     */
    private double instanceMinConfidence;

    /**
     * If set, all metrics will be logged.
     */
    private File debugFile;

    public MatchClassBasedOnInstances(double threshold, SimInstanceMetric metric, double instanceMinConfidence, File debugFile) {
        this.threshold = threshold;
        this.metric = metric;
        this.instanceMinConfidence = instanceMinConfidence;
        this.debugFile = debugFile;
    }
    
    public MatchClassBasedOnInstances(double threshold, SimInstanceMetric metric, double instanceMinConfidence) {
        this(threshold, metric, instanceMinConfidence, null);
    }
    
    public MatchClassBasedOnInstances(double threshold, SimInstanceMetric metric) {
        this(threshold, metric, 0.0);
    }
    
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        inputAlignment.addAll(getClassMatches(source, target, inputAlignment));
        return inputAlignment;
    }
    
    public Alignment getClassMatches(OntModel source, OntModel target, Alignment inputAlignment) throws IOException{
        Map<Correspondence, Integer> classAlignment = new HashMap<>(); // a map from (class source, class target) to number of shared/matched instances
        Map<String, Integer> instanceCounts = new HashMap<>(); // a map from class to number of instances
        int instanceMappings = 0;
        for(Correspondence c : inputAlignment){
            if(c.getConfidence() < this.instanceMinConfidence)
                continue;
            Individual individualSource = source.getIndividual(c.getEntityOne());
            Individual individualTarget = target.getIndividual(c.getEntityTwo());
            if(individualSource == null || individualTarget == null){
                continue;
            }
            instanceMappings++;
            Set<Resource> sourceTypes = getMatchableClasses(individualSource);
            Set<Resource> targetTypes = getMatchableClasses(individualTarget);
            for(Resource sourceType : sourceTypes){
                instanceCounts.computeIfAbsent(sourceType.getURI(), classUri ->source.getOntClass(classUri).listInstances(true).toList().size());
                for(Resource targetType : targetTypes){
                    instanceCounts.computeIfAbsent(targetType.getURI(), classUri ->target.getOntClass(classUri).listInstances(true).toList().size());
                    Correspondence classCorrespondence = new Correspondence(sourceType.getURI(), targetType.getURI());  
                    classAlignment.put(classCorrespondence, classAlignment.getOrDefault(classCorrespondence, 0) + 1); 
                }
            }
        }
        
        saveValuesToFile(classAlignment, instanceCounts, instanceMappings); // DEBUG
        
        Alignment alignment = new Alignment(false, false, false, false);
        for(Entry<Correspondence, Integer> t : classAlignment.entrySet()){
            int instancesOne = instanceCounts.getOrDefault(t.getKey().getEntityOne(), 0);
            int instancesTwo = instanceCounts.getOrDefault(t.getKey().getEntityTwo(), 0);
            int instancesOverlap = t.getValue();
            
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
                LOGGER.trace("Add correspondence with MatchClassBasedOnInstances " + c.toString());
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
    
    private void saveValuesToFile(Map<Correspondence, Integer> classAlignment, Map<String, Integer> instanceCounts, int instanceMappings) throws IOException {
        if(this.debugFile == null)
            return;
        List<Entry<Correspondence, Integer>> list = new ArrayList<>(classAlignment.entrySet());
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
    
    
    private Set<Resource> getMatchableClasses(OntResource resource){
        Set<Resource> set = new HashSet<>();        
        ExtendedIterator<Resource> i = resource.listRDFTypes(false);
        while (i.hasNext()) {
            Resource type = i.next();
            if(type.isURIResource() == false)
                continue;
            
            if(type.equals(OWL.Thing))
                continue;
            set.add(type);
        }
        return set;
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
    
    
    
}
