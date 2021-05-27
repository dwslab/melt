package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_base.OaeiOptions;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;

import java.util.HashMap;
import java.util.Properties;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

/**
 * A simple string matcher using String equivalence as matching criterion.
 */
public class SimpleStringMatcher extends MatcherYAAAJena {
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties p) throws Exception {
        Alignment alignment = new Alignment();
        if(OaeiOptions.isMatchingClassesRequired())//check if matching classes required - only set in HOBBIT (for seals oaeiOptions is always true)
            matchResources(source.listClasses(), target.listClasses(), alignment);//match only classes
        return alignment;
    }
    
    private void matchResources(ExtendedIterator<? extends OntResource> sourceResources, ExtendedIterator<? extends OntResource> targetResources, Alignment alignment) {
        HashMap<String, String> text2URI = new HashMap<>();
        while (sourceResources.hasNext()) {
            OntResource source = sourceResources.next();
            for(String s : getStringRepresentation(source))
                text2URI.put(s, source.getURI());
        }
        while (targetResources.hasNext()) {
            OntResource target = targetResources.next();
            for(String s : getStringRepresentation(target)){
                String sourceURI = text2URI.get(s);
                if(sourceURI != null){
                    alignment.add(sourceURI, target.getURI());
                }
            }
        }
    }
    
    private Set<String> getStringRepresentation(OntResource resource) {
        Set<String> texts = new HashSet<>();
        String uri = resource.getURI();
        if(uri == null)
            return texts;
        NodeIterator labelIterator = resource.listPropertyValues(RDFS.label);
        while(labelIterator.hasNext()){
            RDFNode label = labelIterator.next();
            if(label.isLiteral()){
                texts.add(label.asLiteral().getLexicalForm().toLowerCase());
            }
        }
        String fragment = getUriFragment(resource.getURI());
        if(fragment != null)
            texts.add(fragment.toLowerCase().replace("_", " "));
        return texts;
    }
    
    public static String getUriFragment(String uri){
        int lastIndex = uri.lastIndexOf("#");
        if(lastIndex >= 0){
            return uri.substring(lastIndex + 1);
        }
        lastIndex = uri.lastIndexOf("/");
        if(lastIndex >= 0){
            return uri.substring(lastIndex + 1);
        }
        return null;
    }
}
