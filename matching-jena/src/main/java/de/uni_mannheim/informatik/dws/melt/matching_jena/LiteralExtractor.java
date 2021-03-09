package de.uni_mannheim.informatik.dws.melt.matching_jena;

import java.util.Set;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;

/**
 * Given a resource (from Jena - which has the model behind - and thus allows to traverse the whole graph),
 * this interface extracts the literals which are usually helpful for matching.
 * There are many classes which implement this interface.
 */
public interface LiteralExtractor {


    /**
     * Given a jena resource <code>r</code>, it extracts the literals which are usually helpful for matching.
     * It does not do any tokenization nor transformation.
     * @param r the jena resource to extract the literals (which describe this resource).
     * @return a set of literals which describes the resource.
     */
    Set<Literal> extract(Resource r);
}