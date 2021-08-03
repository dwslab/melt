
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import org.apache.jena.rdf.model.Statement;

/**
 * Extracts all values from specific properties as long as it is a literal.
 */
public class TextExtractorMultipleProperties implements TextExtractor {

    private final Set<Property> properties;

    public TextExtractorMultipleProperties(Property... property) {
        this(new HashSet<>(Arrays.asList(property)));
    }
    
    public TextExtractorMultipleProperties(Set<Property> properties) {
        this.properties = properties;
    }
    
    @Override
    public Set<String> extract(Resource r) {
        Set<String> values = new HashSet<>();
        StmtIterator i = r.listProperties();
        while(i.hasNext()){
            Statement stmt = i.next();
            if(properties.contains(stmt.getPredicate())){
                RDFNode n = stmt.getObject();
                if(n.isLiteral()){
                    String text = n.asLiteral().getLexicalForm().trim();
                    if(!text.isEmpty())
                        values.add(text);
                }
            }
        }
        return values;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.properties);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TextExtractorMultipleProperties other = (TextExtractorMultipleProperties) obj;
        if (!Objects.equals(this.properties, other.properties)) {
            return false;
        }
        return true;
    }

    
    
    
    public Set<Property> getProperties() {
        return properties;
    }

   
}
