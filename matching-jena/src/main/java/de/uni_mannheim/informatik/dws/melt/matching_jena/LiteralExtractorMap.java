package de.uni_mannheim.informatik.dws.melt.matching_jena;

import java.util.Map;
import java.util.Set;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;

/**
 * Given a resource (from Jena - which has the model behind - and thus allows to traverse the whole graph),
 * this interface extracts the literals which are usually helpful for matching.
 * The returned value is a map between the key (e.g. where it is extracted from like label or fragment etc) and the corresponding texts.
 */
public interface LiteralExtractorMap {


    /**
     * Given a jena resource <code>r</code>, it extracts the literals which are usually helpful for matching.
     * It does not do any tokenization nor transformation.
     * @param r the jena resource to extract the literals (which describe this resource).
     * @return a a map between the key (e.g. where it is extracted from like label or fragment etc) and the corresponding texts.
     */
    Map<String, Set<Literal>> extract(Resource r);
}