package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;


public class PropertySpecificStringProcessing {
    //specific properties:
    public static Property URL_FRAGMENT = ModelFactory.createDefaultModel().createProperty("http://example.com/fragment");
    public static Property ALL_STRING_LITERALS = ModelFactory.createDefaultModel().createProperty("http://example.com/all_string_literals");
    public static Property ALL_LITERALS = ModelFactory.createDefaultModel().createProperty("http://example.com/all_literals");
    
    private Set<Property> properties;
    private Function<String, Object> processing;
    private double confidence;
    /**
     * The index name, this Processing belongs to.
     * Same index name means that all objects are search in this index.
     */
    private String indexName;
    
    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, String indexName, Set<Property> properties) {
        this.properties = properties;
        this.processing = processing;
        this.confidence = confidence;
        this.indexName = indexName;
    }

    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, String indexName, Property... property) {
        this(processing, confidence, indexName, new HashSet<>(Arrays.asList(property)));
    }
    
    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, Property... property) {
        this(processing, confidence, UUID.randomUUID().toString(), new HashSet<>(Arrays.asList(property)));
    }

    
    public Set<Property> getProperties() {
        return properties;
    }

    public Function<String, Object> getProcessing() {
        return processing;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getIndexName() {
        return indexName;
    }
}
