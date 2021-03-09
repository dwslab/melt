package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.valueExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.ValueExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.Resource;

/**
 * Extracts the fragment of the URL, e.g. part after last slash or hashtag.
 */
public class ValueExtractorUrlFragment implements ValueExtractor {


    @Override
    public Set<String> extract(Resource r) {
        Set<String> values = new HashSet();
        //if(r.isURIResource())
        String uri = r.getURI();
        if(uri == null){
            //it was a blank node
            return values;
        }
        String fragment = URIUtil.getUriFragment(uri).trim();
        if(!fragment.isEmpty())
            values.add(fragment);
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
