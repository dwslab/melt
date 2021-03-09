package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher;

import de.uni_mannheim.informatik.dws.melt.matching_jena.ValueExtractor;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A tools class containing static functionality for string-based matching.
 */
public class BackgroundMatcherTools {


    /**
     * Creates a map of the form {@code URI -> set<labels>}.
     *
     * @param iterator Iterator for ont resources.
     * @param valueExtractor The value extractor that is to be used to obtain Strings from resources.
     * @return URI label map
     */
    public static Map<String, Set<String>> getURIlabelMap(ExtendedIterator<? extends OntResource> iterator,
                                                          ValueExtractor valueExtractor) {
        Map<String, Set<String>> result = new HashMap<>();
        while (iterator.hasNext()) {
            OntResource r1 = iterator.next();
            Set<String> labels = valueExtractor.extract(r1);
            if (labels != null && labels.size() > 0) {
                result.put(r1.getURI(), labels);
            }
        }
        return result;
    }

}
