
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.util.ResizableDoubleArray;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combines the additional confidences and set the overall correspondence confidence to be the mean of the selected confidences.
 * Can also be used to set the 
 */
public class ConfidenceCombiner extends MatcherYAAAJena {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfidenceCombiner.class);
    
    private UnivariateStatistic statistic;
    private Set<String> additionalConfidenceNames;

    /**
     * Constructor
     * @param statistic choose any implementation from univariatestatistic listed 
     *      <a href="http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math4/stat/descriptive/UnivariateStatistic.html">here</a>.
     * @param additionalConfidenceNames 
     */
    public ConfidenceCombiner(UnivariateStatistic statistic, Set<String> additionalConfidenceNames) {
        this.statistic = statistic;
        this.additionalConfidenceNames = additionalConfidenceNames;
    }

    public ConfidenceCombiner(UnivariateStatistic statistic) {
        this(statistic, null);
    }
    
    /**
     * Compute the mean of the confidences.
     * @param additionalConfidenceNames confidence names to care about.
     */
    public ConfidenceCombiner(Set<String> additionalConfidenceNames){
        this(new Mean(), additionalConfidenceNames);
    }
    
    /**
     * Compute the mean of the confidences.
     * @param additionalConfidenceClasses classes which adds confidences to correspondence.
     */
    public ConfidenceCombiner(Class... additionalConfidenceClasses){
        this.statistic = new Mean();
        this.additionalConfidenceNames = new HashSet<>();
        for(Class c : additionalConfidenceClasses){
            this.additionalConfidenceNames.add(c.getSimpleName());
        }
    }
    
    /**
     * Calulates the mean of all additional confidences.
     */
    public ConfidenceCombiner(){
        this(new Mean(), null);
    }
    
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return combine(inputAlignment);
    }
    
    
    public Alignment combine(Alignment alignment){
        if(additionalConfidenceNames == null || additionalConfidenceNames.isEmpty())
            additionalConfidenceNames = alignment.getDistinctCorrespondenceConfidenceKeys();
        if(additionalConfidenceNames.isEmpty()){
            LOGGER.warn("No additional confidences are available. Return unmodified alignment.");
            return alignment;
        }
        
        Alignment newAlignment = new Alignment(alignment, false);
        for(Correspondence c : alignment){
            ResizableDoubleArray confidences = getConfidences(c);
            if(confidences != null){
                double computedStatistic = confidences.compute(statistic);
                c.setConfidence(computedStatistic);
            }            
            newAlignment.add(c);
        }
        return newAlignment;
    }
    
    
    private ResizableDoubleArray getConfidences(Correspondence c){
        ResizableDoubleArray confidences = new ResizableDoubleArray(additionalConfidenceNames.size());
        for(String confidenceName : additionalConfidenceNames){
            Double value = c.getAdditionalConfidence(confidenceName);
            if(value == null){
                LOGGER.debug("Additional confidence \"{}\" not found in correspodence {}. It will not be modified (no confidence will be set).", confidenceName, c);
                return null;
            }
            confidences.addElement(value);
        }
        return confidences;
    }
}
