package de.uni_mannheim.informatik.dws.ontmatching.kgbaseline;

import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BaselineUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaselineUtil.class);
    
    public static Alignment match(OntModel ont1, OntModel ont2, Alignment alignment, List<Property> properties, boolean lowerCase) {
        LOGGER.info("Start matching");
        
        LOGGER.info("Match classes");
        matchResources(ont1.listClasses(), ont2.listClasses(), alignment, properties, lowerCase);
        
        LOGGER.info("Match Properties");
        matchResources(ont1.listAllOntProperties(), ont2.listAllOntProperties(), alignment, properties, lowerCase);
        
        LOGGER.info("Match Instances");
        matchResources(ont1.listIndividuals(), ont2.listIndividuals(), alignment, properties, lowerCase);
        
        LOGGER.info("Finished matching");
        return alignment;
    }
    
    
    private static void matchResources(ExtendedIterator<? extends OntResource> sourceResources, ExtendedIterator<? extends OntResource> targetResources, Alignment alignment, List<Property> properties, boolean lowerCase) {
        DefaultHashMap<String, List<String>> text2URI = new DefaultHashMap<>(ArrayList.class);
        while (sourceResources.hasNext()) {
            OntResource source = sourceResources.next();
            String sourceURI = source.getURI();
            for(String sourceText : getStringRepresentations(source, properties, lowerCase)){
                text2URI.get(sourceText).add(sourceURI);
            }
        }
        while (targetResources.hasNext()) {
            OntResource target = targetResources.next();
            String targetURI = target.getURI();
            for(String targetText : getStringRepresentations(target, properties, lowerCase)){
                for(String sourceURI : text2URI.get(targetText)){
                    alignment.add(sourceURI, targetURI);
                }
            }
        }
    }
    
    private static Set<String> getStringRepresentations(Resource r, Collection<Property> properties, boolean lowerCase){
        Set<String> values = new HashSet<>();
        if(r.isURIResource() == false)
            return values;
        
        for(Property p : properties){
            StmtIterator i = r.listProperties(p);
            while(i.hasNext()){
                RDFNode n = i.next().getObject();
                if(n.isLiteral()){
                    String text = n.asLiteral().getLexicalForm();
                    //preprocessing
                    text = text.trim();
                    if(lowerCase)
                        text = text.toLowerCase();
                    if(text.length() > 0){
                        values.add(text);
                    }
                }
            }
        }
        
        return values;
    } 
    
}
