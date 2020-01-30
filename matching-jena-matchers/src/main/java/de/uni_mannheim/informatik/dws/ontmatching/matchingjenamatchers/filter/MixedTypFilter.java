package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.filter;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MixedTypFilter extends MatcherYAAAJena{
    private static final Logger LOGGER = LoggerFactory.getLogger(MixedTypFilter.class);
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        Set<Correspondence> tobeDeleted = new HashSet<>();
        for(Correspondence c : inputAlignment){            
            ConceptType sourceType = ConceptType.analyze(source, c.getEntityOne());
            ConceptType targetType = ConceptType.analyze(target, c.getEntityTwo());            
            if(sourceType != targetType){
                tobeDeleted.add(c);
            }
        }
        //LOGGER.info("remove: " + tobeDeleted);
        inputAlignment.removeAll(tobeDeleted);
        return inputAlignment;
    }
}
