package de.uni_mannheim.informatik.dws.melt.matching_jena;

import java.util.Set;
import org.apache.jena.rdf.model.Resource;

/**
 * Given a Jena resource, a ValueExtractor can derive zero or more String representations.
 *
 * @deprecated This interface is deprecated, rather use {@link LiteralExtractor}.
 */
@Deprecated
public interface ValueExtractor {

    Set<String> extract(Resource r);
}
