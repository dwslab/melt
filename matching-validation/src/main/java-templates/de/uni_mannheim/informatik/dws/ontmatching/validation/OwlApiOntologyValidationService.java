package de.uni_mannheim.informatik.dws.ontmatching.validation;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRestriction;

/**
 * OWL API implementation of OntologyValidationService.
 */
public class OwlApiOntologyValidationService extends OntologyValidationService<OWLOntology> {
    
    private static OWLOntologyManager man = OWLManager.createOWLOntologyManager();

    /**
     * Constructor
     * @param ontologyFile File reference to the ontology to be validated.
     */
    public OwlApiOntologyValidationService(File ontologyFile){
        super(ontologyFile);
    }

    /**
     * Constructor
     * @param ontologyUri URI reference to the ontology to be validated.
     */
    public OwlApiOntologyValidationService(URI ontologyUri){
        super(ontologyUri);
    }


    protected Set<String> getURIs(Set<? extends OWLNamedObject> set){
        Set<String> result = new HashSet<>();
        for(OWLNamedObject c : set){
            result.add(c.getIRI().toString());
        }
        return result;
    }
    
    @Override
    protected OWLOntology parseOntology(URI ontUri) throws Exception {
        ArrayList<OWLOntology> ontologiesToBeDeleted = new ArrayList<>(man.getOntologies());
        for(OWLOntology ontology : ontologiesToBeDeleted){
            man.removeOntology(ontology);
        }
        return man.loadOntologyFromOntologyDocument(IRI.create(ontUri));
    }

    @Override
    protected Set<String> retrieveClasses(OWLOntology ontology) {
        return getURIs(ontology.getClassesInSignature());
    }

    @Override
    protected Set<String> retrieveDatatypeProperties(OWLOntology ontology) {
        return getURIs(ontology.getDataPropertiesInSignature());
    }

    @Override
    protected Set<String> retrieveObjectProperties(OWLOntology ontology) {
        return getURIs(ontology.getObjectPropertiesInSignature());
    }

    @Override
    protected Set<String> retrieveProperties(OWLOntology ontology) {
        Set<String> union = retrieveDatatypeProperties(ontology);
        union.addAll(retrieveObjectProperties(ontology));
        return union;
    }

    @Override
    protected Set<String> retrieveInstances(OWLOntology ontology) {
        return getURIs(ontology.getIndividualsInSignature());
    }

    @Override
    protected int computeNumberOfRestrictions(OWLOntology ontology) {
        int restrictions = 0;
        for(OWLAxiom a : ontology.getAxioms()){
            if(a instanceof OWLRestriction)
                restrictions++;
        }
        return restrictions;
    }

    @Override
    protected int computeNumberOfStatements(OWLOntology ontology) {
        return ontology.getAxiomCount();
    }

    @Override
    protected boolean computeHasOntologyDefinition(OWLOntology ontology) {
        //ontology.getOntologyID().getOntologyIRI().toURI()
        OWLOntologyID ontId = ontology.getOntologyID();
        if(ontId == null)
            return false;
        IRI iri = ontId.getOntologyIRI()${owlapiAdd};
        if(iri == null)
            return false;
        URI uri = iri.toURI();
        if(uri == null)
            return false;
        return true;
    }

    @Override
    protected Class getClassForVersionSpecification() {
        return OWLOntology.class;
    }

    @Override
    public boolean isConceptDefined(String concept) {
        return this.ontology.containsEntityInSignature(IRI.create(concept));
    }
}
