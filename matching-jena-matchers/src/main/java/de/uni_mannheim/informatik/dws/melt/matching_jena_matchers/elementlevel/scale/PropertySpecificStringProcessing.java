package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.apache.jena.rdf.model.Property;


public class PropertySpecificStringProcessing {

    private List<ValueExtractor> valueExtractors;
    private Function<String, Object> processing;
    private double confidence;
    
    private int maxLevenshteinDistance;
    private int minLengthForLevenshtein;

    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, List<ValueExtractor> valueExtractors, int maxLevenshteinDistance, int minLengthForLevenshtein) {
        this.valueExtractors = valueExtractors;
        this.processing = processing;
        this.confidence = confidence;
        this.maxLevenshteinDistance = maxLevenshteinDistance;
        this.minLengthForLevenshtein = minLengthForLevenshtein;
    }
    
    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, List<ValueExtractor> valueExtractors) {
        this(processing, confidence, valueExtractors, 0,0);
    }
    
    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, ValueExtractor... valueExtractors) {
        this(processing, confidence, Arrays.asList(valueExtractors));
    }
    
    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, Property... properties) {
        this(processing, confidence, ValueExtractorProperty.wrapExtractor(properties));
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

    public int getMaxLevenshteinDistance() {
        return maxLevenshteinDistance;
    }

    public int getMinLengthForLevenshtein() {
        return minLengthForLevenshtein;
    }
}
