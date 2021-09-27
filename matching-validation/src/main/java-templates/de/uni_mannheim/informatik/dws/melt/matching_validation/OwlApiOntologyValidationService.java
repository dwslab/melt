package de.uni_mannheim.informatik.dws.melt.matching_validation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
import org.semanticweb.owlapi.util.VersionInfo;

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
     * @param fileContent file content of ontology file.
     */
    public OwlApiOntologyValidationService(String fileContent){
        super(fileContent);
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
        // Required to avoid errors on older OWL API releases with tracks that reuse one ontology in multiple test
        // cases.
        @SuppressWarnings("deprecation")
        ArrayList<OWLOntology> ontologiesToBeDeleted = new ArrayList<>(man.getOntologies());
        for(OWLOntology ontology : ontologiesToBeDeleted){
            man.removeOntology(ontology);
        }
        return man.loadOntologyFromOntologyDocument(IRI.create(ontUri));
    }
    
    @Override
    protected OWLOntology parseOntology(String fileContent) throws Exception {
        // Required to avoid errors on older OWL API releases with tracks that reuse one ontology in multiple test
        // cases.
        @SuppressWarnings("deprecation")
        ArrayList<OWLOntology> ontologiesToBeDeleted = new ArrayList<>(man.getOntologies());
        for(OWLOntology ontology : ontologiesToBeDeleted){
            man.removeOntology(ontology);
        }
        return man.loadOntologyFromOntologyDocument(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)));
    }
    

    @Override
    @SuppressWarnings("deprecation")
    protected Set<String> retrieveClasses(OWLOntology ontology) {
        return getURIs(ontology.getClassesInSignature());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Set<String> retrieveDatatypeProperties(OWLOntology ontology) {
        return getURIs(ontology.getDataPropertiesInSignature());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Set<String> retrieveObjectProperties(OWLOntology ontology) {
        return getURIs(ontology.getObjectPropertiesInSignature());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Set<String> retrieveProperties(OWLOntology ontology) {
        return getURIs(ontology.getAnnotationPropertiesInSignature());
        //Set<String> union = retrieveDatatypeProperties(ontology);
        //union.addAll(retrieveObjectProperties(ontology));
        //return union;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Set<String> retrieveInstances(OWLOntology ontology) {
        return getURIs(ontology.getIndividualsInSignature());
    }

    @Override
    @SuppressWarnings("deprecation")
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
    protected String retriveLibName() {
        return "OwlApi";
    }
    
    @Override
    protected String retriveLibVersion() {
        String version = VersionInfo.getVersionInfo().getVersion();
        if(version == null || version.trim().isEmpty()){
            return getVersionFromJarFile(OWLOntology.class);
        }
        return version;
    }

    @Override
    public boolean isConceptDefined(String concept) {
        return this.ontology.containsEntityInSignature(IRI.create(concept));
    }
}
