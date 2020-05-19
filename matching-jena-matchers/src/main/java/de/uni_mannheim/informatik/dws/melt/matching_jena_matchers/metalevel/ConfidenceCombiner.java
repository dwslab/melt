
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combines the additional confidences and set the overall correspondence confidence to be the mean of the selected confidences.
 * Can also be used to set the 
 */
public class ConfidenceCombiner extends MatcherYAAAJena {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfidenceCombiner.class);
    
    private Set<String> additionalConfidenceNames;
    
    public ConfidenceCombiner(Set<String> additionalConfidenceNames){
        this.additionalConfidenceNames = additionalConfidenceNames;
    }
    
    public ConfidenceCombiner(Class... additionalConfidenceClasses){
        this.additionalConfidenceNames = new HashSet<>();
        for(Class c : additionalConfidenceClasses){
            this.additionalConfidenceNames.add(c.getSimpleName());
        }
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        List<Correspondence> modifiedCorrespondences = new ArrayList<>(inputAlignment.size());
        for(Correspondence c : inputAlignment){            
            Collection<Double> confidences = getConfidences(c);
            c.setConfidence(mean(confidences));
            modifiedCorrespondences.add(c);
            //inputAlignment.addOrModify(c);//to update the indices
        }
        //updateing the indices:
        inputAlignment.clear();
        inputAlignment.addAll(modifiedCorrespondences);
        return inputAlignment;
    }
    
    private Collection<Double> getConfidences(Correspondence c){
        if(additionalConfidenceNames.isEmpty()){
            return c.getAdditionalConfidences().values();
        }
        List<Double> confidences = new ArrayList(additionalConfidenceNames.size());
        for(String confidenceName : additionalConfidenceNames){
            Double value = c.getAdditionalConfidence(confidenceName);
            if(value == null){
                LOGGER.warn("Additional confidence \"{}\" not found in correspodence.", confidenceName);
                continue;
            }
            confidences.add(value);
        }
        return confidences;
    }
    
    private static double mean(Collection<Double> collection) {
        double sum = 0;
        for (Double d : collection) {
            sum += d;
        }
        return sum / collection.size();
    }
    
}
