package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter which deletes instance mappings if they have no matched properties in common.
 */
public class InstanceFilterBasedOnCommonProperties extends MatcherYAAAJena {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceFilterBasedOnCommonProperties.class);
    
    /**
     * Minimum number of properties which two instances has to have to be a valid and non filtered match.
     */
    private int minNumberOfCommonProperties;
    
    /**
     * If true, this excludes correspodences which maps to the same URI.
     * e.g. rdf:type = rdf:type
     */
    private boolean excludeSameURIMapping;
    
    /**
     * The minmum confidence for which a property mapping is counted.
     */
    private double minPropertyConfidence;

    /**
     * Constructor with all neccessary parameters.
     * @param minNumberOfCommonProperties Minimum number of properties which two instances has to have to be a valid and non filtered match.
     * @param excludeSameURIMapping If true, this excludes correspodences which maps to the same URI e.g. rdf:type = rdf:type
     * @param minPropertyConfidence The minmum confidence for which a property mapping is counted.
     */
    public InstanceFilterBasedOnCommonProperties(int minNumberOfCommonProperties, boolean excludeSameURIMapping, double minPropertyConfidence) {
        this.minNumberOfCommonProperties = minNumberOfCommonProperties;
        this.excludeSameURIMapping = excludeSameURIMapping;
        this.minPropertyConfidence = minPropertyConfidence;
    }
    
    /**
     * Constructor with reduced parameters. It count all Property mappings in the alignment (regardless of their confidence - at least non negative) and
     * excludes correspodences which maps to the same URI e.g. rdf:type = rdf:type
     * @param minNumberOfCommonProperties Minimum number of properties which two instances has to have to be a valid and non filtered match.
     */
    public InstanceFilterBasedOnCommonProperties(int minNumberOfCommonProperties) {
        this(minNumberOfCommonProperties, true, 0.0);
    }
    
    /**
     * Constructor with default parameters. It count all Property mappings in the alignment (regardless of their confidence - at least non negative) and
     * excludes correspodences which maps to the same URI e.g. rdf:type = rdf:type. 
     * Furthermore it needs to have at least one overlapping property.
     */
    public InstanceFilterBasedOnCommonProperties() {
        this(1, true, 0.0);
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return filter(source, target, inputAlignment);
    }
    
    public Alignment filter(OntModel source, OntModel target, Alignment inputAlignment){
        Alignment finalAlignment = new Alignment();
        for(Correspondence c : inputAlignment){
            Individual individualSource = source.getIndividual(c.getEntityOne());
            Individual individualTarget = target.getIndividual(c.getEntityTwo());
            if(individualSource == null || individualTarget == null){
                finalAlignment.add(c);
                continue;
            }
            
            int count = sharedProperties(individualSource, individualTarget, inputAlignment, this.excludeSameURIMapping, this.minPropertyConfidence);
            if(count >= this.minNumberOfCommonProperties){
                finalAlignment.add(c);
            }else{
                LOGGER.trace("InstanceFilterBasedOnCommonProperties removed the following correspondence because number of shared properties is less than threshold: {}", c);
            }
        }
        return finalAlignment;
    }
    
    
    
    /**
     * Return the number of overlapping distinct properties.
     * @param individualSource the individual source
     * @param individualTarget the individual target
     * @param inputAlignment the input alignment to check for property matches
     * @param excludeSameURIMapping if true, this excludes correspodences which maps to the same URI e.g. rdf:type = rdf:type
     * @param minPropertyConfidence the minmum confidence for which a property mapping is counted.
     * @return number of distinct properties
     */
    public static int sharedProperties(Individual individualSource, Individual individualTarget, Alignment inputAlignment, boolean excludeSameURIMapping, double minPropertyConfidence){
        int count = 0;
        Set<String> sourceProperties = getDistinctProperties(individualSource);            
        Set<String> targetProperties = getDistinctProperties(individualTarget);
        for(String sourcePropURI : sourceProperties){
            for(Correspondence propCorrespondence : inputAlignment.getCorrespondencesSourceRelation(sourcePropURI, CorrespondenceRelation.EQUIVALENCE)){
                if(excludeSameURIMapping && sourcePropURI.equals(propCorrespondence.getEntityTwo()))
                    continue;
                if(propCorrespondence.getConfidence() < minPropertyConfidence)
                    continue;
                if(targetProperties.contains(propCorrespondence.getEntityTwo())){
                    count++;
                }
            }                
        }
        return count;
    }
    
    private static Set<String> getDistinctProperties(Individual resource){ 
        Set<String> properties = new HashSet<>();  
        StmtIterator stmts = resource.listProperties();
        while(stmts.hasNext()){
            Property p = stmts.next().getPredicate();
            if(p.isURIResource()){
                properties.add(p.getURI());   
            } 
        }
        return properties;
    }
    
}
