
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalextractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.LiteralExtractor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * This extractor is a composer and uses the given extractor in the given order as long as an extractor will yield an result.
 */
public class LiteralExtractorFallback implements LiteralExtractor{
    
    private final List<LiteralExtractor> extractors;

    public LiteralExtractorFallback(List<LiteralExtractor> extractors) {
        this.extractors = extractors;
    }
    
    public LiteralExtractorFallback(LiteralExtractor... extractors) {
        this.extractors = Arrays.asList(extractors);
    }
    
    public LiteralExtractorFallback(Property... properties) {
        this.extractors = LiteralExtractorByProperty.wrapExtractor(properties);
    }
    
    @Override
    public Set<Literal> extract(Resource r) {
        Set<Literal> values = new HashSet<>();
        for(LiteralExtractor extractor : this.extractors){
            values.addAll(extractor.extract(r));
            if(values.isEmpty() == false)
                break;//break if we have some values
        }
        return values;
    }
    

    public List<LiteralExtractor> getExtractors() {
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
        final LiteralExtractorFallback other = (LiteralExtractorFallback) obj;
        if (!Objects.equals(this.extractors, other.extractors)) {
            return false;
        }
        return true;
    }
}
