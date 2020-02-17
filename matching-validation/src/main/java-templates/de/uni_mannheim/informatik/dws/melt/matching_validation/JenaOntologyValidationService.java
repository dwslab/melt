package de.uni_mannheim.informatik.dws.melt.matching_validation;

import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator; // keep even though some IDEs might flag this as not necessary

import ${jenaPrefix}ontology.OntModel;
import ${jenaPrefix}ontology.OntModelSpec;
import ${jenaPrefix}ontology.OntResource;
import ${jenaPrefix}rdf.model.ModelFactory;
import ${jenaPrefix}vocabulary.OWL;
import ${jenaPrefix}vocabulary.RDF;

/**
 * Jena implementation of OntologyValidationService.
 */
public class JenaOntologyValidationService extends OntologyValidationService<OntModel> {

    /**
     * Constructor
     * @param ontologyFile File reference to the ontology to be validated.
     */
    public JenaOntologyValidationService(File ontologyFile){
        super(ontologyFile);
    }
    
    /**
     * Constructor
     * @param fileContent file content of ontology file.
     */
    public JenaOntologyValidationService(String fileContent){
        super(fileContent);
    }

    /**
     * Constructor
     * @param ontologyUri URI reference to the ontology to be validated.
     */
    public JenaOntologyValidationService(URI ontologyUri){
        super(ontologyUri);
    }

    @Override
    protected OntModel parseOntology(URI ontUri) throws Exception {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);    
        model.read(ontUri.toString(), "");
        return model;
    }
    
    @Override
    protected OntModel parseOntology(String fileContent) throws Exception {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);    
        model.read(new StringReader(fileContent), "");
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
        return getURIs(ontology.listAllOntProperties());
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