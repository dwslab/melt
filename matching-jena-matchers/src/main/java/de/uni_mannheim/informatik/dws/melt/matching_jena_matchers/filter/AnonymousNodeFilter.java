package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * This filter removes correspondences where the source or target is a blank node.
 */
public class AnonymousNodeFilter extends MatcherYAAAJena implements Filter {


    private static final Logger LOGGER = LoggerFactory.getLogger(AnonymousNodeFilter.class);

    /**
     * Constructor
     */
    public AnonymousNodeFilter(){
    }
    
    /**
     * Filters the alignment based on similar hosts.
     * @param source the source ontology
     * @param target the target ontology
     * @param inputAlignment the alignment to be filtered
     * @return the filtered alignment.
     */
    public static Alignment filter(OntModel source, OntModel target, Alignment inputAlignment){
        Alignment resultAlignment = new Alignment();
        for(Correspondence c : inputAlignment){
            if(source.getOntResource(c.getEntityOne()) == null){
                continue;
            }
            if(target.getOntResource(c.getEntityTwo()) == null){
                continue;
            }
            resultAlignment.add(c);
        }
        return resultAlignment;
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return filter(source, target, inputAlignment);
    }
}
