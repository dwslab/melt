package de.uni_mannheim.informatik.dws.ontmatching.validation;

import java.net.URI;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import ${jenaPrefix}ontology.OntModel;
import ${jenaPrefix}ontology.OntModelSpec;
import ${jenaPrefix}ontology.OntResource;
import ${jenaPrefix}rdf.model.ModelFactory;
import ${jenaPrefix}vocabulary.OWL;
import ${jenaPrefix}vocabulary.RDF;

public class JenaOntologyValidationService extends OntologyValidationService<OntModel> {

    @Override
    protected OntModel parseOntology(URI ontUri) throws Exception {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);    
        model.read(ontUri.toString(), "");
        return model;
    }
    
    protected Set<String> getURIs(Iterator<? extends OntResource> it){
        Set<String> result = new HashSet<>();
        while (it.hasNext()){
            OntResource r = it.next();
            if(r.isURIResource()){
                result.add( r.getURI() );
            }
        }
        return result;
    }
    
    @Override
    protected Set<String> retrieveClasses(OntModel ontology) {   
        return getURIs(ontology.listClasses());
    }

    @Override
    protected Set<String> retrieveDatatypeProperties(OntModel ontology) {
        return getURIs(ontology.listDatatypeProperties());
    }

    @Override
    protected Set<String> retrieveObjectProperties(OntModel ontology) {
        return getURIs(ontology.listObjectProperties());
    }

    @Override
    protected Set<String> retrieveProperties(OntModel ontology) {
        return getURIs(ontology.listOntProperties());
    }

    @Override
    protected Set<String> retrieveInstances(OntModel ontology) {
        return getURIs(ontology.listIndividuals());
    }

    @Override
    protected int computeNumberOfRestrictions(OntModel ontology) {
        return ontology.listRestrictions().toSet().size();
    }

    @Override
    protected int computeNumberOfStatements(OntModel ontology) {
        return (int)ontology.size();
        //return ontology.listStatements().toList().size();
    }

    @Override
    protected boolean computeHasOntologyDefinition(OntModel ontology) {
        return ontology.contains(null, RDF.type, OWL.Ontology);
    }

    @Override
    protected Class getClassForVersionSpecification() {
        return OntModel.class;
    }

    @Override
    public boolean isConceptDefined(String uri) {
        return this.ontology.getOntResource(uri) != null;
    }
}