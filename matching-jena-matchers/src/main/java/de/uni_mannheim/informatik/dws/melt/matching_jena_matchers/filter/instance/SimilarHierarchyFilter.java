package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance;

import com.googlecode.cqengine.query.QueryFactory;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel.hierarchical.agony.Agony;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check if already matched individuals have a similar hierarchy (class hierarchy).
 * For different computation methods see {@link SimilarHierarchyFilterApproach}.
 */
public class SimilarHierarchyFilter extends MatcherYAAAJena implements Filter {

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SimilarHierarchyFilter.class);

    /**
     * Property connecting the instance to the hierarchy (usually rdf:type).
     */
    protected Property instanceToHierarchyProperty;
    
    /**
     * Property connecting the hierarchy together (usually rdfs:subClassOf).
     */
    protected Property hierarchyProperty;
    
    /**
     * A matcher which selects or creates some correspondences which forms an alignment for the hierarchy nodes.
     */
    protected MatcherYAAAJena hierarchyMatcher;
    
    /**
     * Which approach to determine the confidence.
     */
    protected SimilarHierarchyFilterApproach approach;
    
    /**
     * The threshold to compare with the calculated confidence.
     */
    protected double threshold;
    
    public SimilarHierarchyFilter(){
        this.instanceToHierarchyProperty = RDF.type;
        this.hierarchyProperty = RDFS.subClassOf;
        this.hierarchyMatcher = new MatcherYAAAJena() {
            @Override
            public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                return inputAlignment; //just return the full input alignment which also contains the class matches
            }
        };
        this.approach = SimilarHierarchyFilterApproach.DEPTH_DEPENDEND_MATCHES;
    }

    public SimilarHierarchyFilter(Property instanceToHierarchyProperty, Property hierarchyProperty, MatcherYAAAJena hierarchyMatcher, SimilarHierarchyFilterApproach approach, double threshold) {
        this.instanceToHierarchyProperty = instanceToHierarchyProperty;
        this.hierarchyProperty = hierarchyProperty;
        this.hierarchyMatcher = hierarchyMatcher;
        this.approach = approach;
        this.threshold = threshold;
    }

    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        Alignment hierarchyMatches = this.hierarchyMatcher.match(source, target, inputAlignment, properties);
        Alignment finalAlignment = new Alignment(inputAlignment, false);
        for(Correspondence correspondence : inputAlignment){            
            Individual sourceIndividual = source.getIndividual(correspondence.getEntityOne());
            Individual targetIndividual = target.getIndividual(correspondence.getEntityTwo());
            if(sourceIndividual == null || targetIndividual == null){
                finalAlignment.add(correspondence);
                continue;
            }

            Map<String, Double> sourceHierarchyWeights;
            Map<String, Double> targetHierarchyWeights;
            if(this.approach == SimilarHierarchyFilterApproach.HIERARCHY_LEVEL_DEPENDED_MATCHES){
                sourceHierarchyWeights = getNormalizedHierarchyLevels(sourceIndividual);
                targetHierarchyWeights = getNormalizedHierarchyLevels(targetIndividual);
            }else{
                sourceHierarchyWeights = getNormalizedDepths(sourceIndividual);
                targetHierarchyWeights = getNormalizedDepths(targetIndividual);
            }
            
            Iterable<Correspondence> i = hierarchyMatches.retrieve(
                QueryFactory.and(
                    QueryFactory.in(Correspondence.SOURCE, sourceHierarchyWeights.keySet()),
                    QueryFactory.in(Correspondence.TARGET, targetHierarchyWeights.keySet())
                ));
            
            double maxWeigth = 0;
            int absoluteMatches = 0;
            for(Correspondence c : i){
                double sourceWeight = sourceHierarchyWeights.get(c.getEntityOne());
                double targetWeight = targetHierarchyWeights.get(c.getEntityTwo());
                double averageWeight = (sourceWeight + targetWeight) / 2.0d;
                if(averageWeight > maxWeigth){
                    maxWeigth = averageWeight;
                }
                absoluteMatches++;
            }
            
            double confidence;
            if(this.approach == SimilarHierarchyFilterApproach.ABSOLUTE_MATCHES){
                confidence = absoluteMatches;
            }else{
                confidence = maxWeigth;
            }
            
            if(confidence >= this.threshold){
                correspondence.addAdditionalConfidence(this.getClass(), confidence);
                finalAlignment.add(correspondence);
            }
        }
        return finalAlignment;
    }
    
    /**
     * Returns the hierarchy levels computed by the agony algorithm.
     * @param i the individual to start with.
     * @return map from hierarchical element uri to the normalized hierarchy level
     */
    public Map<String, Double> getNormalizedHierarchyLevels(Individual i){
        // compute graph with breath first search        
        Set<Resource> visited = new HashSet<>();
        Queue<Resource> q = new LinkedList<>();
        for(Resource firstHierarchyElement : getObjectAsResource(i.listProperties(this.instanceToHierarchyProperty))){
            q.add(firstHierarchyElement);
            visited.add(firstHierarchyElement);
        }
        List<Entry<Resource, Resource>> hierarchyGraph = new ArrayList<>();
        while(!q.isEmpty()){
            Resource current = q.poll();
            for(Resource succ : getObjectAsResource(current.listProperties(this.hierarchyProperty))){
                if(visited.contains(succ) == false){
                    visited.add(succ);
                    q.add(succ);
                }
                hierarchyGraph.add(new SimpleEntry(current, succ));
            }
            
        }
        if(hierarchyGraph.isEmpty())
            return new HashMap<>();        
        //AgonyUtil.writeDotFile(new File("category.dot"), hierarchyGraph, e->URIUtil.getUriFragment(e.getURI()).substring(9));
        Agony<Resource> agony = new Agony(hierarchyGraph);
        Map<Resource, Integer> hierarchyLevels = agony.computeAgony();
        
        return inverseAndNormalizeMapValues(hierarchyLevels);
    }
    
    public Map<String, Double> getNormalizedDepths(Individual i){
        //breath first search
        Map<Resource, Integer> depths = new HashMap<>();
        Set<Resource> visited = new HashSet<>();
        Queue<Resource> q = new LinkedList<>();
        for(Resource firstHierarchyElement : getObjectAsResource(i.listProperties(this.instanceToHierarchyProperty))){
            depths.put(firstHierarchyElement, 0);
            q.add(firstHierarchyElement);
            visited.add(firstHierarchyElement);
        }
        while(!q.isEmpty()){
            Resource current = q.poll();
            for(Resource succ : getObjectAsResource(current.listProperties(this.hierarchyProperty))){
                if(visited.contains(succ) == false){
                    depths.put(succ, depths.get(current) + 1);
                    visited.add(succ);                    
                    q.add(succ);
                }
            }
        }
        return inverseAndNormalizeMapValues(depths);
    }
    
    private Map<String, Double> inverseAndNormalizeMapValues(Map<Resource, Integer> map){
        if(map.isEmpty())
            return new HashMap<>();
        Map<String, Double> normalized = new HashMap<>();
        int maxValue = Collections.max(map.values());
        for(Entry<Resource, Integer> e : map.entrySet()){
            if(e.getKey().isURIResource())
                normalized.put(e.getKey().getURI(), (double) (maxValue - e.getValue()) / (double) maxValue );
        }
        return normalized;
    }
    
    protected List<Resource> getObjectAsResource(StmtIterator i){
        //i.filterKeep(s->s.getObject().isResource()).mapWith(s->s.getObject().asResource());
        List<Resource> resources = new ArrayList<>();
        while(i.hasNext()){
            Statement s = i.next();
            if(s.getObject().isResource())
                resources.add(s.getObject().asResource());
        }
        return resources;
    }

    @Override
    public String toString() {
        return "SimilarHierarchyFilter";
    }
}
