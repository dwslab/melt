package de.uni_mannheim.informatik.dws.melt.matching_validation;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An OntologyValidationService allows to validate a single ontology, i.e., make sure that the ontology is parseable.
 * In addition the service calculates statistics about the ontology.
 * @param <T> The class which represents the ontology such as OWLOntology in case of the OWL API.
 */
public abstract class OntologyValidationService<T> {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyValidationService.class);
    
    private Set<String> classes = new HashSet<>();
    private Set<String> datatypeProperties = new HashSet<>();
    private Set<String> objectProperties = new HashSet<>();
    private Set<String> properties = new HashSet<>();
    private Set<String> instances = new HashSet<>();
    private int numberOfRestrictions = 0;
    private int numberOfStatements = 0;
    private boolean ontologyDefined = false;
    private boolean ontologyParseable = false;
    private String libName = "";
    private String libVersion = "";
    protected T ontology = null;
    private URI ontologyUri = null;


    /**
     * Constructor
     * @param ontologyUri URI of the ontology to be validated.
     */
    public OntologyValidationService(URI ontologyUri){
        this.ontologyUri = ontologyUri;
        try {
            this.ontology = parseOntology(ontologyUri);
            if(this.ontology == null)
                throw new Exception("Ontology is null");
            this.ontologyParseable = true;
            this.libName = retriveLibName();
            this.libVersion = retriveLibVersion();
            computeStatistics(this.ontology);            
        } catch (Exception ex) {
            this.ontology = null;
            this.ontologyParseable = false;
            LOGGER.warn("Ontology not parsable", ex);
        }
    }
    
    /**
     * Constructor
     * @param fileContent file content of ontology file.
     */
    public OntologyValidationService(String fileContent){
        this.ontologyUri = null;
        try {
            this.ontology = parseOntology(fileContent);
            if(this.ontology == null)
                throw new Exception("Ontology is null");
            this.ontologyParseable = true;
            this.libName = retriveLibName();
            this.libVersion = retriveLibVersion();
            computeStatistics(this.ontology);            
        } catch (Exception ex) {
            this.ontology = null;
            this.ontologyParseable = false;
            LOGGER.warn("Ontology not parsable", ex);
        }
    }


    /**
     * Constructor
     * @param ontologyFile Ontology File to be validated.
     */
    public OntologyValidationService(File ontologyFile){
        this(ontologyFile.toURI());
    }


    /**
     * Set local statistics variables.
     * @param ontology The ontologies for which the statistics shall be computed and set.
     */
    protected void computeStatistics(T ontology){
        this.classes = retrieveClasses(ontology);
        this.datatypeProperties = retrieveDatatypeProperties(ontology);
        this.objectProperties = retrieveObjectProperties(ontology);
        this.properties = retrieveProperties(ontology);
        this.instances = retrieveInstances(ontology);
        this.numberOfRestrictions = computeNumberOfRestrictions(ontology);
        this.numberOfStatements = computeNumberOfStatements(ontology);
        this.ontologyDefined = computeHasOntologyDefinition(ontology);
    }
    
    protected abstract T parseOntology(URI ontUri) throws Exception; 
    protected abstract T parseOntology(String fileContent) throws Exception; 
    
    protected abstract Set<String> retrieveClasses(T ontology);
    protected abstract Set<String> retrieveDatatypeProperties(T ontology);
    protected abstract Set<String> retrieveObjectProperties(T ontology);
    protected abstract Set<String> retrieveProperties(T ontology);    
    protected abstract Set<String> retrieveInstances(T ontology);
    protected abstract int computeNumberOfRestrictions(T ontology);
    protected abstract int computeNumberOfStatements(T ontology);
    protected abstract boolean computeHasOntologyDefinition(T ontology);
    protected abstract String retriveLibName();
    protected abstract String retriveLibVersion();
    
    public abstract boolean isConceptDefined(String concept);
    
    protected String getVersionFromJarFile(Class clazz){
        String classPath = clazz.getResource(clazz.getSimpleName() + ".class").toString(); 
        String libPath = classPath.substring(0, classPath.lastIndexOf("!")); 
        String libFileName = libPath.substring(libPath.lastIndexOf("/") + 1, libPath.lastIndexOf("."));
        String filePath = libPath + "!/META-INF/MANIFEST.MF"; 
        try {
            Manifest manifest = new Manifest(new URL(filePath).openStream());
            Attributes attr = manifest.getMainAttributes();
            return attr.getValue("Implementation-Version");
        } catch (IOException ex) {
            LOGGER.info("Could not create manifest for version extraction of parsing library.", ex);
            return libFileName;
        }   
    }

    /**
     * Returns all class-, property- and instance-URIs.
     * @return A large set of all resources.
     */
    public Set<String> getAllResources() {
        Set<String> result = new HashSet<>();
        if(classes != null) result.addAll(classes);
        if(instances != null) result.addAll(instances);
        if(properties != null) result.addAll(properties);
        if(datatypeProperties != null) result.addAll(datatypeProperties);        
        if(objectProperties != null) result.addAll(objectProperties);        
        return result;
    }

    public Set<String> getClasses() {
        return classes;
    }
    public int getNumberOfClasses() {
        return classes.size();
    }

    public Set<String> getDatatypeProperties() {
        return datatypeProperties;
    }
    public int getNumberOfDatatypeProperties() {
        return datatypeProperties.size();
    }

    public Set<String> getObjectProperties() {
        return objectProperties;
    }
    public int getNumberOfObjectProperties() {
        return objectProperties.size();
    }

    public Set<String> getProperties() {
        return properties;
    }
    public int getNumberOfProperties() {
        return properties.size();
    }

    public Set<String> getInstances() {
        return instances;
    }
    public int getNumberOfInstances() {
        return instances.size();
    }

    public int getNumberOfRestrictions() {
        return numberOfRestrictions;
    }

    public int getNumberOfStatements() {
        return numberOfStatements;
    }

    public boolean isOntologyDefined() {
        return ontologyDefined;
    }

    public boolean isOntologyParseable() {
        return ontologyParseable;
    }

    public String getLibName() {
        return libName;
    }

    public String getLibVersion() {
        return libVersion;
    }

    public T getOntology() {
        return ontology;
    }

    public URI getOntologyUri() {
        return ontologyUri;
    }

    @Override
    public String toString() {
        return "OntologyValidationService{" + 
                "statements=" + this.getNumberOfStatements() + 
                ", classes=" + this.getNumberOfClasses() + 
                ", instances=" + this.getNumberOfInstances() + 
                ", properties=" + this.getNumberOfProperties() + 
                ", datatypeProperties=" + this.getNumberOfDatatypeProperties() + 
                ", objectProperties=" + this.getNumberOfObjectProperties() +                 
                ", restrictions=" + this.getNumberOfRestrictions() + 
                ", ontologyDefined=" + ontologyDefined + 
                ", ontologyParseable=" + ontologyParseable + 
                ", libName=" + libName + 
                ", libVersion=" + libVersion + 
                ", ontologyUri=" + ontologyUri + '}';
    }
    
}
