package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.resultset.ResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A filter which removes correspondences where source or target is matched to more than one entity.
 * All such correspondences will be removed.
 * As an example: if alignment looks like
 * <ul>
 * <li>A, B</li>
 * <li>C, D</li>
 * <li>C, E</li>
 * <li>F, D</li>
 * </ul>
 * then the last three are removed because C and D are matched multiple times.
 */
public class NtoMCorrespondenceFilter extends MatcherYAAAJena implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NtoMCorrespondenceFilter.class);
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return filter(inputAlignment);
    }
    
    public static Alignment filter(Alignment alignment){
        Set<String> sourceSeenOnce = new HashSet<>();
        Set<String> targetSeenOnce = new HashSet<>();
        
        Set<String> sourceSeenMultipleTimes = new HashSet<>();
        Set<String> targetSeenMultipleTimes = new HashSet<>();
        LOGGER.debug("Iterate over alignment with {} correspondences", alignment.size());
        for(Correspondence c : alignment){
            if(sourceSeenOnce.contains(c.getEntityOne())){
                sourceSeenMultipleTimes.add(c.getEntityOne());
            }else{
                sourceSeenOnce.add(c.getEntityOne());                
            }
            
            if(targetSeenOnce.contains(c.getEntityTwo())){
                targetSeenMultipleTimes.add(c.getEntityTwo());
            }else{
                targetSeenOnce.add(c.getEntityTwo());                
            }
        }
        LOGGER.debug("{} source and {} target URIs seen multiple times.Retrive now correspondences to remove.", 
                sourceSeenMultipleTimes.size(), targetSeenMultipleTimes.size());
        ResultSet<Correspondence> result = alignment.retrieve(
                QueryFactory.or(
                    QueryFactory.in(Correspondence.SOURCE, sourceSeenMultipleTimes),
                    QueryFactory.in(Correspondence.TARGET, targetSeenMultipleTimes)
                )
        );
        LOGGER.debug("Will remove {} correspondences", result.size());
        for(Correspondence c : result){
            alignment.remove(c);
        }
        LOGGER.debug("Finished removing correspondes");
        return alignment;
    }
}
