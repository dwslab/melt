package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.LiteralExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Extracts the fragment of the url e.g. part after last slash or hashtag.
 */
public class LiteralExtractorUrlFragment implements LiteralExtractor {


    @Override
    public Set<Literal> extract(Resource r) {
        Set<Literal> values = new HashSet<>();
        //if(r.isURIResource())
        String uri = r.getURI();
        if(uri == null){
            //it was a blank node
            return values;
        }
        String fragment = URIUtil.getUriFragment(uri).trim();
        if(!fragment.isEmpty())
            values.add(ResourceFactory.createStringLiteral(fragment));
        return values;
    }
    
    @Override
    public int hashCode() {
        return 5345366;
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
