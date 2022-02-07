package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_owlapi.MatcherYAAAOwlApi;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Stream;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * A simple string matcher using String equivalence as matching criterion.
 */
public class SimpleStringMatcher extends MatcherYAAAOwlApi { //implements IMatcher<OWLOntology,Alignment,Properties> {


    @Override
    public Alignment match(OWLOntology source, OWLOntology target, Alignment inputAlignment, Properties p) throws Exception {
        Alignment alignment = new Alignment();
        
        matchResources(source, source.classesInSignature().map(o->o.getIRI()), 
                target, target.classesInSignature().map(o->o.getIRI()), alignment); 
        
        matchResources(source, source.objectPropertiesInSignature().map(o->o.getIRI()), 
                target, target.objectPropertiesInSignature().map(o->o.getIRI()), alignment); 
        
        matchResources(source, source.dataPropertiesInSignature().map(o->o.getIRI()), 
                target, target.dataPropertiesInSignature().map(o->o.getIRI()), alignment); 
        
        matchResources(source, source.individualsInSignature().map(o->o.getIRI()), 
                target, target.individualsInSignature().map(o->o.getIRI()), alignment); 
        
        return alignment;
    }
    
    
    private void matchResources(OWLOntology source, Stream<IRI> sourceResources, OWLOntology target, Stream<IRI> targetResources, Alignment alignment) {
        HashMap<String, String> text2URI = new HashMap<>();
        sourceResources.forEach(iri -> text2URI.put(getStringRepresentation(source, iri), iri.toString()));
        
        targetResources.forEach(iri -> {
            String sourceURI = text2URI.get(getStringRepresentation(target, iri));
            if(sourceURI != null){
                alignment.add(sourceURI, iri.toString());
            }
        });
    }
    
    private String getStringRepresentation(OWLOntology ontology, IRI iri) {
        for(OWLAnnotationAssertionAxiom annotation : ontology.getAnnotationAssertionAxioms(iri)){
            if(annotation.getProperty().isLabel()){
                if(annotation.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    return val.getLiteral().toLowerCase();
                }
            }
        }
        return getUriFragment(iri.toString()).replace('_', ' ').toLowerCase();
    }
    
    private static String getUriFragment(String uri){
        int lastIndex = uri.lastIndexOf("#");
        if(lastIndex >= 0){
            return uri.substring(lastIndex + 1);
        }
        lastIndex = uri.lastIndexOf("/");
        if(lastIndex >= 0){
            return uri.substring(lastIndex + 1);
        }
        return uri;
    }
}
