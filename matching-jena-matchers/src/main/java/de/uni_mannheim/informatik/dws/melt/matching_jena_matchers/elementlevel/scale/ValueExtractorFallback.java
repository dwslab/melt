
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * This extractor is a composer and uses the given extractor in the given order as long as an extractor will yield an result.
 */
public class ValueExtractorFallback implements ValueExtractor{
    
    private List<ValueExtractor> extractors;

    public ValueExtractorFallback(List<ValueExtractor> extractors) {
        this.extractors = extractors;
    }
    
    public ValueExtractorFallback(ValueExtractor... extractors) {
        this.extractors = Arrays.asList(extractors);
    }
    
    public ValueExtractorFallback(Property... properties) {
        this.extractors = ValueExtractorProperty.wrapExtractor(properties);
    }
    
    @Override
    public Set<String> extract(Resource r) {
        Set<String> values = new HashSet();
        for(ValueExtractor extractor : this.extractors){
            values.addAll(extractor.extract(r));
            if(values.isEmpty() == false)
                break;//break if we have some values
        }
        return values;
    }
    

    public List<ValueExtractor> getExtractors() {
        return extractors;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.extractors);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ValueExtractorFallback other = (ValueExtractorFallback) obj;
        if (!Objects.equals(this.extractors, other.extractors)) {
            return false;
        }
        return true;
    }

    
    
    
    
    
    
}
