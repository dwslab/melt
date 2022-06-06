package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filters the alignment by computing the inflection point of the sorted
 * confidences. To make it more stable a smoothing (spline interpolation) can be
 * used. Furthermore also the elbow point of the confidences can be used to
 * filter them.
 */
public class ConfidenceCurvatureFilter extends MatcherYAAAJena implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfidenceCurvatureFilter.class);

    private final MeltCurvature curvature;
    private final String additionalConfidenceKey;

    /**
     * Initializes the object with all possible attributes.
     *
     * @param curvature curvature method to use
     * @param additionalConfidenceKey confidence key to use
     */
    public ConfidenceCurvatureFilter(MeltCurvature curvature, String additionalConfidenceKey) {
        this.curvature = curvature;
        this.additionalConfidenceKey = additionalConfidenceKey;
    }
    
    public ConfidenceCurvatureFilter(MeltCurvature curvature) {
        this.curvature = curvature;
        this.additionalConfidenceKey = null;
    }
    

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties p) throws Exception {
        if(StringUtils.isBlank(this.additionalConfidenceKey)){
            return filter(inputAlignment, this.curvature);
        }else{
            return filter(inputAlignment, this.curvature, this.additionalConfidenceKey);
        }
    }

    public static Alignment filter(Alignment inputAlignment, MeltCurvature curvature) {
        double threshold = getThreshold(inputAlignment, curvature);
        LOGGER.info("Compute threshold of alignment based on curvature which is: {}", threshold);
        return inputAlignment.cut(threshold);
    }
    
    public static Alignment filter(Alignment inputAlignment, MeltCurvature curvature, String additionalConfidenceKey) {
        double threshold = getThreshold(inputAlignment, curvature, additionalConfidenceKey);
        LOGGER.info("Compute threshold of alignment based on curvature which is: {}", threshold);
        return inputAlignment.cut(threshold);
    }
    
    public static double getThreshold(Alignment inputAlignment, MeltCurvature curvature, String additionalConfidenceKey) {
        if(StringUtils.isBlank(additionalConfidenceKey)){
            throw new IllegalArgumentException("additionalConfidenceKey is null or empty");
        }
        double[] confidences = getSortedConfidences(inputAlignment, additionalConfidenceKey);
        return curvature.computeCurvature(confidences);
    }
    
    public static double getThreshold(Alignment inputAlignment, MeltCurvature curvature) {
        double[] confidences = getSortedConfidences(inputAlignment);
        return curvature.computeCurvature(confidences);
    }
    
    
    private static double[] getSortedConfidences(Alignment alignment) {
        List<Double> confidences = new ArrayList<>(alignment.size());
        for (Correspondence c : alignment) {
            confidences.add(c.getConfidence());
        }
        Collections.sort(confidences);
        return confidences.stream().mapToDouble(d -> d).toArray();
    }
    
    private static double[] getSortedConfidences(Alignment alignment, String additionalConfidenceKey) {
        List<Double> confidences = new ArrayList<>(alignment.size());
        for (Correspondence c : alignment) {
            confidences.add(c.getAdditionalConfidence(additionalConfidenceKey));
        }
        Collections.sort(confidences);
        return confidences.stream().mapToDouble(d -> d).toArray();
    }
    
}