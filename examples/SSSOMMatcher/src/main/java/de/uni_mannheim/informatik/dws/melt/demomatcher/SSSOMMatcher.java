package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_base.MatcherURL;
import de.uni_mannheim.informatik.dws.melt.matching_jena.OntologyCacheJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.DefaultExtensions.SSSOM;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.SSSOMSerializer;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

/**
 * A very simple SSSOM matcher.
 */
public class SSSOMMatcher extends MatcherURL {

    @Override
    public URL match(URL sourceURL, URL targetURL, URL inputAlignment) throws Exception {
        
        OntModel source = OntologyCacheJena.get(sourceURL, OntologyCacheJena.DEFAULT_JENA_ONT_MODEL_SPEC);
        OntModel target = OntologyCacheJena.get(targetURL, OntologyCacheJena.DEFAULT_JENA_ONT_MODEL_SPEC);
        
        Alignment alignment = new Alignment();
        matchResources(source.listClasses(), target.listClasses(), alignment);//match only classes
        
        File alignmentFile = File.createTempFile("alignment", ".rdf");
        
        alignment.addExtensionValue(SSSOM.MAPPING_SET_TITLE, "A simple SSSOM mapping.");
        
        SSSOMSerializer.serialize(alignment, alignmentFile);
        return alignmentFile.toURI().toURL();
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

