package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import java.util.Set;
import org.apache.jena.rdf.model.Resource;

/**
 *
 */
public interface ValueExtractor {
    public Set<String> extract(Resource r);    
}
