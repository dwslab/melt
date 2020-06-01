
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * Extracts all values from a specific proeprty as long as it is a literal.
 */
public class ValueExtractorProperty implements ValueExtractor{

    private Property property;

    public ValueExtractorProperty(Property property) {
        this.property = property;
    }
    
    @Override
    public Set<String> extract(Resource r) {
        Set<String> values = new HashSet();
        StmtIterator i = r.listProperties(this.property);
        while(i.hasNext()){
            RDFNode n = i.next().getObject();
            if(n.isLiteral()){
                String text = n.asLiteral().getLexicalForm().trim();
                if(!text.isEmpty())
                    values.add(text);
            }
        }
        return values;
    }

    public Property getProperty() {
        return property;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.property);
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
        final ValueExtractorProperty other = (ValueExtractorProperty) obj;
        if (!Objects.equals(this.property, other.property)) {
            return false;
        }
        return true;
    }
    
    public static List<ValueExtractor> wrapExtractor(Property... properties){
        return wrapExtractor(Arrays.asList(properties));
    }
    
    public static List<ValueExtractor> wrapExtractor(Collection<Property> properties){
        List<ValueExtractor> extractors = new ArrayList(properties.size());
        for(Property p : properties){
            extractors.add(new ValueExtractorProperty(p));
        }
        return extractors;
    }
}
