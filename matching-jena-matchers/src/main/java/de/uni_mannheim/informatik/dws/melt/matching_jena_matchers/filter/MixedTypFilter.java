package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Asserts a homogenous alignment (i.e. only the same type is matched).
 * For instance, correspondences between instances and classes will be deleted.
 *
 * Not Allowed (examples):
 * - class, instance
 * - datatype property, object property
 * - rdf property, datatype property
 *
 * Allowed are only exact matches.
 */
public class MixedTypFilter extends MatcherYAAAJena implements Filter {
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
