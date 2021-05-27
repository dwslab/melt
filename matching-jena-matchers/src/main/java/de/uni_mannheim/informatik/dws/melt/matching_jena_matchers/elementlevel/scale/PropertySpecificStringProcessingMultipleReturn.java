package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorProperty;
import org.apache.jena.rdf.model.Property;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;

public class PropertySpecificStringProcessingMultipleReturn {


    private List<TextExtractor> valueExtractors;
    private Function<String, Iterable<Object>> processing;
    private double confidence;
    
    private int maxLevenshteinDistance;
    private int minLengthForLevenshtein;
    
    
    public PropertySpecificStringProcessingMultipleReturn(Function<String, Iterable<Object>> processing, double confidence, List<TextExtractor> valueExtractors, int maxLevenshteinDistance, int minLengthForLevenshtein) {
        this.valueExtractors = valueExtractors;
        this.processing = processing;
        this.confidence = confidence;
        this.maxLevenshteinDistance = maxLevenshteinDistance;
        this.minLengthForLevenshtein = minLengthForLevenshtein;
    }
    
    public PropertySpecificStringProcessingMultipleReturn(Function<String, Iterable<Object>> processing, double confidence, List<TextExtractor> valueExtractors) {
        this(processing, confidence, valueExtractors, 0,0);
    }
    
    public PropertySpecificStringProcessingMultipleReturn(Function<String, Iterable<Object>> processing, double confidence, TextExtractor... valueExtractors) {
        this(processing, confidence, Arrays.asList(valueExtractors));
    }
    
    public PropertySpecificStringProcessingMultipleReturn(Function<String, Iterable<Object>> processing, double confidence, Property... properties) {
        this(processing, confidence, TextExtractorProperty.wrapExtractor(properties));
    }
    
    
    public List<TextExtractor> getValueExtractors() {
        return valueExtractors;
    }

    public Function<String, Iterable<Object>> getProcessing() {
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
