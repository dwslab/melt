package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalextractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.LiteralExtractor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * Extracts all values from a specific property as long as it is a literal.
 */
public class LiteralExtractorByProperty implements LiteralExtractor{

    private final Property property;

    public LiteralExtractorByProperty(Property property) {
        this.property = property;
    }
    
    @Override
    public Set<Literal> extract(Resource r) {
        Set<Literal> values = new HashSet<>();
        StmtIterator i = r.listProperties(this.property);
        while(i.hasNext()){
            RDFNode n = i.next().getObject();
            if(n.isLiteral()){
                values.add((Literal)n);
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
        final LiteralExtractorByProperty other = (LiteralExtractorByProperty) obj;
        if (!Objects.equals(this.property, other.property)) {
            return false;
        }
        return true;
    }
    
    public static List<LiteralExtractor> wrapExtractor(Property... properties){
        return wrapExtractor(Arrays.asList(properties));
    }
    
    public static List<LiteralExtractor> wrapExtractor(Collection<Property> properties){
        List<LiteralExtractor> extractors = new ArrayList<>(properties.size());
        for(Property p : properties){
            extractors.add(new LiteralExtractorByProperty(p));
        }
        return extractors;
    }
}
