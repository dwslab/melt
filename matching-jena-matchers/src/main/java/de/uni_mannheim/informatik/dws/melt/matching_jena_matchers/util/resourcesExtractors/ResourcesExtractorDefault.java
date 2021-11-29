package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.resourcesExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.ResourcesExtractor;
import java.util.Arrays;
import java.util.List;

/**
 * Class for listing default extractors.
 */
public class ResourcesExtractorDefault {
    
    /**
     * Returns a list of default extractors like: class, all types of properties, and instances.
     * They only return values if they shouldl be matched.
     * @return the default extractors
     */
    public static List<ResourcesExtractor> getDefaultExtractors(){
        return Arrays.asList(
                new ResourcesExtractorClasses(),
                new ResourcesExtractorDatatypeProperties(),
                new ResourcesExtractorObjectProperties(),
                new ResourcesExtractorRDFProperties(),
                new ResourcesExtractorInstances());
    }
}
