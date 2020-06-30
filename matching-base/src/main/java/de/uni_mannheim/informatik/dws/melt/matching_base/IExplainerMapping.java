package de.uni_mannheim.informatik.dws.melt.matching_base;

import java.util.List;
import java.util.Map;

/**
 * Interface for classes that are able to generate explanatory statements about individual mappings.
 * @author Sven Hertling, Jan Portisch
 */
public interface IExplainerMapping {
    
    /**
     * Gets information (features) about a mapping (represent through the parameters) from e.g. a matcher.
     * The result is a map of feature name and corresponding value.
     * @param uriOne the uri of the first matched entity
     * @param uriTwo the uri of the second matched entity
     * @param relation the relation from the mapping
     * @param confidence the confidence of teh mapping
     * @return a map of feature name and corresponding value.
     */
    public Map<String, String> getMappingFeatures(String uriOne, String uriTwo, String relation, double confidence);


    /**
     * Get the names of the mapping features which will also appear in the result of
     * {@link IExplainerMapping#getMappingFeatures(String, String, String, double)}.
     * @return A list of the feature names.
     */
    public List<String> getMappingFeatureNames();
}
