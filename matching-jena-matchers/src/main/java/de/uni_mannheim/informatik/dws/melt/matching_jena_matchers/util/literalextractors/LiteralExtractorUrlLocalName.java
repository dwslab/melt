package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalextractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.LiteralExtractor;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Extracts the local name from the URI. This wrapps the Jena method getLocalName of class Resource which maps itself to org.apache.jena.rdf.model.impl.Util.splitNamespaceXML.
 */
public class LiteralExtractorUrlLocalName implements LiteralExtractor {
    
    @Override
    public Set<Literal> extract(Resource r) {
        Set<Literal> values = new HashSet<>();
        String localName = r.getLocalName().trim().trim();
        if(!localName.isEmpty())
            values.add(ResourceFactory.createStringLiteral(localName));
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
