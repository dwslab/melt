package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.Resource;

/**
 * Extracts the local name from the URI. This wrapps the Jena method getLocalName of class Resource which maps itself to org.apache.jena.rdf.model.impl.Util.splitNamespaceXML.
 */
public class ValueExtractorUrlLocalName implements ValueExtractor {
    
    @Override
    public Set<String> extract(Resource r) {
        Set<String> values = new HashSet();
        values.add(r.getLocalName().trim());
        return values;
    }
    
    @Override
    public int hashCode() {
        return 536789345;
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
