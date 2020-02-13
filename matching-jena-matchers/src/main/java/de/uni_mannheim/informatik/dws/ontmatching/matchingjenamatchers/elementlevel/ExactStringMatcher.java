package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.elementlevel;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.OaeiOptions;
import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

/**
 * Matcher which creates correspondences based on exact string match.
 */
public class ExactStringMatcher extends MatcherYAAAJena {
    
    private Collection<Property> properties;

    public ExactStringMatcher(Collection<Property> properties){
        this.properties = properties;       
    }

    public ExactStringMatcher(Property... properties){
        this(Arrays.asList(properties));
    }
    
    public ExactStringMatcher(){
        this(RDFS.label);
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        if(OaeiOptions.isMatchingClassesRequired())
            matchResources(source.listClasses(), target.listClasses(), inputAlignment);        
        if(OaeiOptions.isMatchingDataPropertiesRequired() || OaeiOptions.isMatchingObjectPropertiesRequired())
            matchResources(source.listAllOntProperties(), target.listAllOntProperties(), inputAlignment);        
        if(OaeiOptions.isMatchingInstancesRequired())
            matchResources(source.listIndividuals(), target.listIndividuals(), inputAlignment);
        return inputAlignment;
    }
    
    private void matchResources(ExtendedIterator<? extends OntResource> sourceResources, ExtendedIterator<? extends OntResource> targetResources, Alignment alignment) {
        Map<String, Set<String>> text2URI = new HashMap<>();
        while (sourceResources.hasNext()) {
            OntResource source = sourceResources.next();
            String sourceURI = source.getURI();            
            for(String sourceText : getStringRepresentations(source)){
                Set<String> uris = text2URI.get(sourceText);
                if(uris == null){
                    uris = new HashSet<>();
                    text2URI.put(sourceText, uris);
                }
                uris.add(sourceURI);
            }
        }
        while (targetResources.hasNext()) {
            OntResource target = targetResources.next();
            for(String targetText : getStringRepresentations(target)){
                Set<String> sourceURIs = text2URI.get(targetText);
                if(sourceURIs != null){
                    for(String sourceURI : sourceURIs){
                        alignment.add(sourceURI, target.getURI());
                    }
                }
            }
        }
    }
    
    protected Set<String> getStringRepresentations(Resource r){
        Set<String> values = new HashSet<>();
        if(r.isURIResource() == false)
            return values;
        for(Property p : properties){
            StmtIterator i = r.listProperties(p);
            while(i.hasNext()){
                RDFNode n = i.next().getObject();
                if(n.isLiteral()){
                    String processed = processString(n.asLiteral().getLexicalForm());
                    if(StringUtils.isBlank(processed) == false)
                        values.add(processed);
                }
            }
        }
        return values;
    }
    
    protected String processString(String text){
        return text.toLowerCase().trim();
    }

}
