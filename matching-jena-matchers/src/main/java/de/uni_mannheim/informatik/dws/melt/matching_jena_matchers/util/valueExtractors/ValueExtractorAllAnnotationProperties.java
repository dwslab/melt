package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.valueExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.ValueExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalExtractors.LiteralExtractorAllAnnotationProperties;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;


import java.util.Set;
import java.util.stream.Collectors;

/**
 * All annotation properties are followed (recursively).
 */
public class ValueExtractorAllAnnotationProperties implements ValueExtractor {


    private static LiteralExtractorAllAnnotationProperties extractor = new LiteralExtractorAllAnnotationProperties();

    @Override
    @NotNull
    public Set<String> extract(Resource resource) {
        return extractor.extract(resource).stream().map(Literal::getLexicalForm).filter(x -> !x.trim().equals("")).collect(Collectors.toSet());
    }
}
