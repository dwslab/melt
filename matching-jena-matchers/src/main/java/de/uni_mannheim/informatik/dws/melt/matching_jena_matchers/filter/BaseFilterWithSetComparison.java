
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.SetSimilarity;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;

/**
 * Basic filter for instances which compares sets like neighbours or properties.
 */
public abstract class BaseFilterWithSetComparison extends MatcherYAAAJena implements Filter {
    /**
     * The threshold which should be larger or equal to be a valid match.
     * Computation is based on set similarity.
     */
    protected double threshold;
    
    /**
     * The set similarity to choose when computing similarity value between the two distinct property sets.
     */
    protected SetSimilarity setSimilarity;

    public BaseFilterWithSetComparison(double threshold, SetSimilarity setSimilarity) {
        this.threshold = threshold;
        this.setSimilarity = setSimilarity;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public SetSimilarity getSetSimilarity() {
        return setSimilarity;
    }

    public void setSetSimilarity(SetSimilarity setSimilarity) {
        this.setSimilarity = setSimilarity;
    }
}
