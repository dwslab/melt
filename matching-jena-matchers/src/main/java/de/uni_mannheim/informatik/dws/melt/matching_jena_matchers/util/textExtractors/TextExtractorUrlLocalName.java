package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;

/**
 * Extracts the local name from the URI. This wraps the Jena method getLocalName of class Resource which maps itself to
 * {@code org.apache.jena.rdf.model.impl.Util#splitNamespaceXML(String)}.
 */
public class TextExtractorUrlLocalName implements TextExtractor {


    @Override
    public Set<String> extract(Resource r) {
        Set<String> values = new HashSet<>();
        String localName = r.getLocalName().trim().trim();
        if (!localName.isEmpty())
            values.add(localName);
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
