
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;

/**
 * This extractor is a composer and uses the given extractor in the given order as long as an extractor will yield an result.
 */
public class TextExtractorFallback implements TextExtractor {


    private final List<TextExtractor> extractors;

    public TextExtractorFallback(List<TextExtractor> extractors) {
        this.extractors = extractors;
    }
    
    public TextExtractorFallback(TextExtractor... extractors) {
        this.extractors = Arrays.asList(extractors);
    }
    
    public TextExtractorFallback(Property... properties) {
        this.extractors = TextExtractorProperty.wrapExtractor(properties);
    }
    
    @Override
    public Set<String> extract(Resource r) {
        Set<String> values = new HashSet<>();
        for(TextExtractor extractor : this.extractors){
            values.addAll(extractor.extract(r));
            if(values.isEmpty() == false)
                break;//break if we have some values
        }
        return values;
    }
    

    public List<TextExtractor> getExtractors() {
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
        final TextExtractorFallback other = (TextExtractorFallback) obj;
        if (!Objects.equals(this.extractors, other.extractors)) {
            return false;
        }
        return true;
    }
}
