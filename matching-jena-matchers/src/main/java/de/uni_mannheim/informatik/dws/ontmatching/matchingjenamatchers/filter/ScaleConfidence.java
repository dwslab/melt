package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.filter;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;

/**
 *
 * @author shertlin
 */
public class ScaleConfidence extends MatcherYAAAJena{

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return scale(inputAlignment);
    }
    
    public static Alignment scale(Alignment alignment){
        return scale(alignment, 0.0, 1.0);
    }
    
    public static Alignment scale(Alignment alignment, double newMin, double newMax){
        //https://stackoverflow.com/questions/5294955/how-to-scale-down-a-range-of-numbers-with-a-known-min-and-max-value
        
        //find min and max:
        double min = 1.0;
        double max = 0.0;
        for(Correspondence c : alignment){
            if(c.getConfidence() > max){
                max = c.getConfidence();
            }
            if(c.getConfidence() < min){
                min = c.getConfidence();
            }
        }
        
        //scale:
        double range = max - min;
        double scale = newMax - newMin;
        if(range == 0.0){
            for(Correspondence c : alignment){
                c.setConfidence((scale * c.getConfidence()) + newMin);//TODO: check
            }
        }else{
            for(Correspondence c : alignment){
                c.setConfidence(((scale * (c.getConfidence() - min))/ range) + newMin);
            }
        }
        
        return alignment;        
    }    
}
