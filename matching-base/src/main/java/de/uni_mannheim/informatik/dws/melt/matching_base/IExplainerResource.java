package de.uni_mannheim.informatik.dws.melt.matching_base;

import java.util.List;
import java.util.Map;

/**
 * Interface for classes that are able to generate explanatory statements about individual resources.
 * @author Sven Hertling, Jan Portisch
 */
public interface IExplainerResource {
    
    /**
     * Gets information (features) about a resource (represent through the uri parameter) from e.g. a matcher.
     * The result is a map of feature name and corresponding value.
     * @param uri the resource uri.
     * @return a map of feature name and corresponding value.
     */
    Map<String, String> getResourceFeatures(String uri);

    /**
     * Get the names of the resource features which will also appear in the result of
     * {@link IExplainerResource#getResourceFeatures(String)}.
     * @return A list of the resource feature names.
     */
    List<String> getResourceFeatureNames();

}
