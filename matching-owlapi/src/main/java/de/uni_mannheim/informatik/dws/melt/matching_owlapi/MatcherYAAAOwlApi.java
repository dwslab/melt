package de.uni_mannheim.informatik.dws.melt.matching_owlapi;

import de.uni_mannheim.informatik.dws.melt.matching_yaaa.MatcherYAAA;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.net.URL;
import java.util.Properties;

import org.semanticweb.owlapi.model.OWLOntology;


public abstract class MatcherYAAAOwlApi extends MatcherYAAA {
    
    /**
     * Default implementation to load an ontology from an url with the owlapi.
     * It can be changed by subclasses.
     * @param url a location where an ontology can be found
     * @return the loaded ontology as an OWLOntology object
     */
    protected OWLOntology readOntology(URL url){
        return OntologyCacheOwlApi.get(url);
    }
    
    @Override
    public Alignment match(URL source, URL target, Alignment inputAlignment, Properties properties) throws Exception {
        OWLOntology owlapiSource = readOntology(source);
        OWLOntology owlapiTarget = readOntology(target);
        
        //inputAlignment.setOnto1(new OntoInfo(jena_source.getNsPrefixURI(""), source.toString()));
        //inputAlignment.setOnto2(new OntoInfo(jena_target.getNsPrefixURI(""), target.toString()));
        
        return this.match(owlapiSource, owlapiTarget, inputAlignment, properties);
    }

    public abstract Alignment match(OWLOntology source, OWLOntology target, Alignment inputAlignment, Properties p) throws Exception ;
}
