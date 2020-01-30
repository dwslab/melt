package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.filter;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.CorrespondenceRelation;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
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

    public InstanceFilterBasedOnCommonProperties(int minNumberOfCommonProperties, boolean excludeSameURIMapping, double minPropertyConfidence) {
        this.minNumberOfCommonProperties = minNumberOfCommonProperties;
        this.excludeSameURIMapping = excludeSameURIMapping;
        this.minPropertyConfidence = minPropertyConfidence;
    }
    
    public InstanceFilterBasedOnCommonProperties(){
        this(1,true, 0.0);
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
            
            int count = 0;
            Set<String> sourceProperties = getDistinctProperties(source, individualSource);            
            Set<String> targetProperties = getDistinctProperties(target, individualTarget);
            for(String sourcePropURI : sourceProperties){
                for(Correspondence propCorrespondence : inputAlignment.getCorrespondencesSourceRelation(sourcePropURI, CorrespondenceRelation.EQUIVALENCE)){
                    if(excludeSameURIMapping && sourcePropURI.equals(propCorrespondence.getEntityTwo()))
                        continue;
                    if(propCorrespondence.getConfidence() < this.minPropertyConfidence)
                        continue;
                    if(targetProperties.contains(propCorrespondence.getEntityTwo())){
                        count++;
                    }
                }                
            }
            if(count >= this.minNumberOfCommonProperties){
                finalAlignment.add(c);
            }else{
                LOGGER.trace("InstanceFilterBasedOnCommonProperties removed the following correspondence because number of shared properties is less than threshold: {}", c);
            }
        }
        return finalAlignment;
    }
    
    
    private static Set<String> getDistinctProperties(OntModel m, Individual subject){        
        Set<String> properties = new HashSet<>();        
        StmtIterator stmts = m.listStatements(subject, null, (RDFNode)null);
        while(stmts.hasNext()){
            Property p = stmts.next().getPredicate();
            if(p.isURIResource()){
                properties.add(p.getURI());   
            } 
        }
        return properties;
    }
    
}
