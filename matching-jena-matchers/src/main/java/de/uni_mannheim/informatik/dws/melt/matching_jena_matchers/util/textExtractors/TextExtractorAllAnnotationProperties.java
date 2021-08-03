package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalExtractors.LiteralExtractorAllAnnotationProperties;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;


import java.util.Set;
import java.util.stream.Collectors;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;

/**
 * All annotation properties are followed (recursively).
 */
public class TextExtractorAllAnnotationProperties implements TextExtractor {


    private static final LiteralExtractorAllAnnotationProperties extractor = new LiteralExtractorAllAnnotationProperties();

    @Override
    @NotNull
    public Set<String> extract(Resource resource) {
        return extractor.extract(resource).stream().map(Literal::getLexicalForm).filter(x -> !x.trim().equals("")).collect(Collectors.toSet());
    }
    
    @Override
    public int hashCode() {
        return 46546518;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return getClass() == obj.getClass();
    }
}
