package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.jena.rdf.model.Resource;

/**
 * A text textractor which extracts texts from a resource which can be used by transformer 
 * based matchers like TransformersFilter or TransformersFilterFineTuner.
 * It will extract the longest text literal which is attached to the resource or the fragment if no literal is available.
 */
public class TextExtractorUseLongestLiteralOrFragment implements TextExtractor {

    private final TextExtractorAllStringLiterals allStringLiterals = new TextExtractorAllStringLiterals();
    private final TextExtractorUrlFragment urlFragment = new TextExtractorUrlFragment();
    
    @Override
    public Set<String> extract(Resource r) {
        
        Set<String> s = allStringLiterals.extract(r);
        if(s.isEmpty()){
            return urlFragment.extract(r);
        }
        
        List<String> list = new ArrayList<>(s);
        list.sort(Comparator.comparingInt(String::length).reversed().thenComparing(x->x));
        return new HashSet<>(Arrays.asList(list.get(0)));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.allStringLiterals);
        hash = 97 * hash + Objects.hashCode(this.urlFragment);
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
        final TextExtractorUseLongestLiteralOrFragment other = (TextExtractorUseLongestLiteralOrFragment) obj;
        if (!Objects.equals(this.allStringLiterals, other.allStringLiterals)) {
            return false;
        }
        if (!Objects.equals(this.urlFragment, other.urlFragment)) {
            return false;
        }
        return true;
    }

    
}
