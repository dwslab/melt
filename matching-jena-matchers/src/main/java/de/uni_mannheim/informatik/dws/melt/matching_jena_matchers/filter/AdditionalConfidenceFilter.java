package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;

import java.util.Properties;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import org.apache.jena.ontology.OntModel;

/**
 * It filters based on the additional confidence. The key and threshold should be provided.
 */
public class AdditionalConfidenceFilter extends MatcherYAAAJena implements Filter {


    private double threshold;
    private String additionalConfidenceKey;

    public AdditionalConfidenceFilter(double threshold, String additionalConfidenceKey) {
        this.threshold = threshold;
        this.additionalConfidenceKey = additionalConfidenceKey;
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return filter(inputAlignment, this.additionalConfidenceKey, this.threshold);
    }
    
    /**
     * Filters the given alignment such that in the returned alignment, only correspondences appear where
     * the additional confidence (provided by the key) is greater or equal to the given threshold.
     * @param alignment the initial alignment.
     * @param additionalConfidenceKey the key of the additional alignment.
     * @param threshold the threshold to use.
     * @return the filtered alignment.
     */
    public static Alignment filter(Alignment alignment, String additionalConfidenceKey, double threshold) {
        Alignment result = new Alignment(alignment, false);
        for (Correspondence c : alignment) {
            if(c.getAdditionalConfidence(additionalConfidenceKey) >= threshold){
                result.add(c);
            }
        }
        return result;
    }
}
