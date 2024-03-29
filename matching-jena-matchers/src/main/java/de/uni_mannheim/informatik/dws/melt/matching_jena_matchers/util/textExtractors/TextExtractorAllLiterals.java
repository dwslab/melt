package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;

/**
 * This extractor uses all literals of the resource.
 */
public class TextExtractorAllLiterals implements TextExtractor {


    @Override
    public Set<String> extract(Resource r) {
        Set<String> values = new HashSet<>();
        StmtIterator i = r.listProperties();
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
