package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.HashMap;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * A simple string matcher using String equivalence as matching criterion.
 */
public class SimpleStringMatcher implements IMatcher<OntModel,Alignment,Properties> {
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties p) throws Exception {
        Alignment alignment = new Alignment();
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
