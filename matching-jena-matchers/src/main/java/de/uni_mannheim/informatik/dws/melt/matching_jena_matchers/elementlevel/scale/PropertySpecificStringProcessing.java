package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.apache.jena.rdf.model.Property;


public class PropertySpecificStringProcessing {

    private List<ValueExtractor> valueExtractors;
    private Function<String, Object> processing;
    private double confidence;
    /**
     * The index name, this Processing belongs to.
     * Same index name means that all objects are search in this index.
     */
    private String indexName;
    
    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, String indexName, List<ValueExtractor> valueExtractors) {
        this.valueExtractors = valueExtractors;
        this.processing = processing;
        this.confidence = confidence;
        this.indexName = indexName;
    }
    
    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, String indexName, ValueExtractor... valueExtractors) {
        this(processing, confidence, indexName, Arrays.asList(valueExtractors));
    }
    
    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, String indexName, Property... properties) {
        this(processing, confidence, indexName, ValueExtractorProperty.wrapExtractor(properties));
    }

    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, ValueExtractor... valueExtractors) {
        this(processing, confidence, UUID.randomUUID().toString(), valueExtractors);
    }
    
    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, Property... properties) {
        this(processing, confidence, UUID.randomUUID().toString(), properties);
    }
    
    public List<ValueExtractor> getValueExtractors() {
        return valueExtractors;
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
