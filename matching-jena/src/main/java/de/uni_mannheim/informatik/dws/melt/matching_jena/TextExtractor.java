package de.uni_mannheim.informatik.dws.melt.matching_jena;

import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;

/**
 * Given a Jena resource, a ValueExtractor can derive zero or more String representations.
 *
 * Developer remark:
 * If you implement a new extractor: For a good design, you may want to implement a {@link LiteralExtractor} and use
 * this interface to wrap it. Code for wrapping:
 * {@code extractor.extract(resource).stream().map(Literal::getLexicalForm).filter(x -> !x.trim().equals("")).collect(Collectors.toSet());}
 */
public interface TextExtractor {

    /**
     * Given a Jena resource this method extracts textual/string representations from it.
     * @param r the jena resource which also allows to traverse the whole rdf graph
     * @return a set of textual representations of the given resource.
     */
    Set<String> extract(Resource r);
    
    public static TextExtractor wrapLiteralExtractor(LiteralExtractor e){
        return (Resource r) -> e.extract(r).stream().map(Literal::getLexicalForm).filter(x -> !x.trim().equals("")).collect(Collectors.toSet());
    }
}
