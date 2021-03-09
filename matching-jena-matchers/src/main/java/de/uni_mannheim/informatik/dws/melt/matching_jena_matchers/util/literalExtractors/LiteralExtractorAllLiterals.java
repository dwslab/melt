package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.LiteralExtractor;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * This extractor uses all literals of the resource.
 */
public class LiteralExtractorAllLiterals implements LiteralExtractor {


    @Override
    public Set<Literal> extract(Resource r) {
        Set<Literal> values = new HashSet<>();
        StmtIterator i = r.listProperties();
        while(i.hasNext()){
            RDFNode n = i.next().getObject();
            if(n.isLiteral()){
                values.add((Literal)n);
            }
        }
        return values;
    }
    
    @Override
    public int hashCode() {
        return 1584754;
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
