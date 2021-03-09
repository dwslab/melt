package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import de.uni_mannheim.informatik.dws.melt.matching_jena.ValueExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.valueExtractors.ValueExtractorProperty;
import org.apache.jena.rdf.model.Property;


public class PropertySpecificStringProcessing extends PropertySpecificStringProcessingMultipleReturn {


    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, List<ValueExtractor> valueExtractors, int maxLevenshteinDistance, int minLengthForLevenshtein) {
        super(text-> Arrays.asList(processing.apply(text)), confidence, valueExtractors, maxLevenshteinDistance, minLengthForLevenshtein);
        //this.valueExtractors = valueExtractors;
        //this.processing = processing;
        //this.confidence = confidence;
        //this.maxLevenshteinDistance = maxLevenshteinDistance;
        //this.minLengthForLevenshtein = minLengthForLevenshtein;
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
}
