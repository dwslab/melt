package de.uni_mannheim.informatik.dws.ontmatching.validation;

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

public abstract class OntologyValidationService<T> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyValidationService.class);
    
    private Set<String> classes = new HashSet<>();
    private Set<String> datatypeProperties = new HashSet<>();
    private Set<String> objectProperties = new HashSet<>();
    private Set<String> properties = new HashSet<>();
    private Set<String> instances = new HashSet<>();
    private int numberOfRestrictions = 0;
    private int numberOfStatements = 0;
    private boolean ontologyDefined = false;
    private boolean ontParseable = false;
    private String libName = "";
    private String libVersion = "";
    protected T ontology = null;
    private URI ontUri = null;
    

    public void loadOntology(File ontFile){
        loadOntology(ontFile.toURI());        
    }
    public void loadOntology(URI ontUri){
        this.ontUri = ontUri;
        try {
            this.ontology = parseOntology(ontUri);
            if(this.ontology == null)
                throw new Exception("Ontology is null");
            this.ontParseable = true;
            setVersion(getClassForVersionSpecification());
            LOGGER.info("Run OntologyValidationService with \"{}\", version: \"{}\"", this.libName, this.libVersion);
            computeStatistics(this.ontology);            
        } catch (Exception ex) {
            this.ontology = null;
            this.ontParseable = false;
            LOGGER.warn("Ontology not parsable", ex);
        }
    }
    
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
    
    protected abstract Set<String> retrieveClasses(T ontology);
    protected abstract Set<String> retrieveDatatypeProperties(T ontology);
    protected abstract Set<String> retrieveObjectProperties(T ontology);
    protected abstract Set<String> retrieveProperties(T ontology);    
    protected abstract Set<String> retrieveInstances(T ontology);
    protected abstract int computeNumberOfRestrictions(T ontology);
    protected abstract int computeNumberOfStatements(T ontology);
    protected abstract boolean computeHasOntologyDefinition(T ontology);    
    protected abstract Class getClassForVersionSpecification();
    
    public abstract boolean isConceptDefined(String concept);
    
    protected void setVersion(Class clazz){
        String classPath = clazz.getResource(clazz.getSimpleName() + ".class").toString(); 
        String libPath = classPath.substring(0, classPath.lastIndexOf("!")); 
        String libFileName = libPath.substring(libPath.lastIndexOf("/") + 1, libPath.lastIndexOf("."));
        String filePath = libPath + "!/META-INF/MANIFEST.MF"; 
        try {
            Manifest manifest = new Manifest(new URL(filePath).openStream());
            Attributes attr = manifest.getMainAttributes();
            this.libName = attr.getValue("Implementation-Title");
            this.libVersion = attr.getValue("Implementation-Version");
        } catch (IOException ex) {
            LOGGER.info("Could not create manifest for version extraction of parsing library.", ex);
        }
        
        if(this.libName == null || this.libName.length() == 0){
            this.libName = libFileName;
        }
        if(this.libVersion == null || this.libVersion.length() == 0){
            this.libVersion = libFileName;
        }        
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

    public boolean isOntParseable() {
        return ontParseable;
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

    public URI getOntUri() {
        return ontUri;
    }

    @Override
    public String toString() {
        return "OntologyValidationService{" + "numberOfStatements=" + numberOfStatements + ", ontologyDefined=" + ontologyDefined + ", ontParseable=" + ontParseable + ", libName=" + libName + ", libVersion=" + libVersion + ", ontUri=" + ontUri + '}';
    }
}
