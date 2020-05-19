
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.SetSimilarity;

/**
 * Basic filter for instances which compares sets like neighbours or properties.
 */
public abstract class BaseInstanceFilterWithSetComparison extends MatcherYAAAJena {
    /**
     * The threshold which should be larger or equal to be a valid match.
     * Computation is based on set similarity.
     */
    protected double threshold;
    
    /**
     * The set similarity to choose when computing similarity value between the two distinct property sets.
     */
    protected SetSimilarity setSimilatity;

    public BaseInstanceFilterWithSetComparison(double threshold, SetSimilarity setSimilatity) {
        this.threshold = threshold;
        this.setSimilatity = setSimilatity;
    }
    

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public SetSimilarity getSetSimilatity() {
        return setSimilatity;
    }

    public void setSetSimilatity(SetSimilarity setSimilatity) {
        this.setSimilatity = setSimilatity;
    }
    
    
    
    
}
