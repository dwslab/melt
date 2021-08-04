package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.addnegatives;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConceptType;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This helper class is used to randomly sample resources from an {@link OntModel}.
 * When created, it will retrieve all instances, classes and properties and keep them in memory.
 */
public class RandomSampleOntModel {


    private static final Logger LOGGER = LoggerFactory.getLogger(RandomSampleOntModel.class);
    
    private final RandomSampleSet<String> individuals;
    private final RandomSampleSet<String> allProperties;
    private final RandomSampleSet<String> datatypeProperties;
    private final RandomSampleSet<String> objectProperties;
    private final RandomSampleSet<String> annotationProperties;
    private final RandomSampleSet<String> classes;
    private final RandomSampleSet<String> all;
    
    
    public RandomSampleOntModel(OntModel m, Random rnd){
        
        Set<String> individualsString = getUriSet(m.listIndividuals());
        Set<String> allPropertiesString = getUriSet(m.listOntProperties());
        Set<String> datatypePropertiesString = getUriSet(m.listDatatypeProperties());
        Set<String> objectPropertiesString = getUriSet(m.listObjectProperties());
        Set<String> annotationPropertiesString = getUriSet(m.listAnnotationProperties());
        Set<String> classesString = getUriSet(m.listClasses());
        
        this.individuals = new RandomSampleSet<>(individualsString, rnd);
        this.allProperties = new RandomSampleSet<>(allPropertiesString, rnd);
        this.datatypeProperties = new RandomSampleSet<>(datatypePropertiesString, rnd);
        this.objectProperties = new RandomSampleSet<>(objectPropertiesString, rnd);
        this.annotationProperties = new RandomSampleSet<>(annotationPropertiesString, rnd);
        this.classes = new RandomSampleSet<>(classesString, rnd);
        
        Set<String> allString = new HashSet<>(individualsString.size() + allPropertiesString.size() +
                datatypePropertiesString.size() + objectPropertiesString.size() + 
                annotationPropertiesString.size() + classesString.size());
        allString.addAll(individualsString);
        allString.addAll(allPropertiesString);
        allString.addAll(datatypePropertiesString);
        allString.addAll(objectPropertiesString);
        allString.addAll(annotationPropertiesString);
        allString.addAll(classesString);
        
        this.all = new RandomSampleSet<>(allString);
    }
    
    public RandomSampleOntModel(OntModel m){
        this(m, new Random());
    }
    
    public RandomSampleOntModel(OntModel m, long seed){
        this(m, new Random(seed));
    }
    
    
    public String getRandomElement(){
        return this.all.getRandomElement();
    }
    
    public RandomSampleSet<String> getGlobalSampler(){
        return this.all;
    }
    
    public RandomSampleSet<String> getSampler(ConceptType type){
        switch(type){
            case INSTANCE:
                return this.individuals;
            case RDF_PROPERTY:
                return this.allProperties;
            case DATATYPE_PROPERTY:
                return this.datatypeProperties;
            case OBJECT_PROPERTY:
                return this.objectProperties;
            case ANNOTATION_PROPERTY:
                return this.annotationProperties;
            case CLASS:
                return this.classes;
            case UNKNOWN:
                return this.all;
            default:
                throw new IllegalArgumentException("ConceptType enum is not recognized.");
        }
    }
    
    private static Set<String> getUriSet(ExtendedIterator<? extends OntResource> iterator){
        Set<String> resourceURIs = new HashSet<>(); // just to make sure that we have unique elements
        while(iterator.hasNext()){
            OntResource r = iterator.next();
            if(r.isURIResource()){
                resourceURIs.add(r.getURI());
            }
        }
        return resourceURIs;
    }
}

