package de.uni_mannheim.informatik.dws.ontmatching.demomatcher;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.OaeiOptions;
import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.util.HashMap;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * A simple string matcher using String equivalence as matching criterion.
 */
public class SimpleStringMatcher extends MatcherYAAAJena {
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties p) throws Exception {
        Alignment alignment = new Alignment();
        if(OaeiOptions.isMatchingClassesRequired())//check if matching classes requried - only set in hobbit (for seals oaeiOptions is always true)
            matchResources(source.listClasses(), target.listClasses(), alignment);//match only classes
        return alignment;
    }
    
    private void matchResources(ExtendedIterator<? extends OntResource> sourceResources, ExtendedIterator<? extends OntResource> targetResources, Alignment alignment) {
        HashMap<String, String> text2URI = new HashMap<>();
        while (sourceResources.hasNext()) {
            OntResource source = sourceResources.next();
            text2URI.put(getStringRepresentation(source), source.getURI());
        }
        while (targetResources.hasNext()) {
            OntResource target = targetResources.next();
            String sourceURI = text2URI.get(getStringRepresentation(target));
            if(sourceURI != null){
                alignment.add(sourceURI, target.getURI());
            }
        }
    }
    
    private String getStringRepresentation(OntResource resource) {
        String arbitraryLabel = resource.getLabel(null);
        if(arbitraryLabel != null)
            return arbitraryLabel;
        return resource.getLocalName();
    }
}
