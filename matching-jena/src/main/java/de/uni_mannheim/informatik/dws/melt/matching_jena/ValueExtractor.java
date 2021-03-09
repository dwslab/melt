package de.uni_mannheim.informatik.dws.melt.matching_jena;

import java.util.Set;
import org.apache.jena.rdf.model.Resource;

/**
 * Given a Jena resource, a ValueExtractor can derive zero or more String representations.
 *
 * Developer remark:
 * If you implement a new extractor: For a good design, you may want to implement a {@link LiteralExtractor} and use
 * this interface to wrap it. Code for wrapping:
 * {@code extractor.extract(resource).stream().map(Literal::getLexicalForm).filter(x -> !x.trim().equals("")).collect(Collectors.toSet());}
 */
public interface ValueExtractor {


    Set<String> extract(Resource r);
}
