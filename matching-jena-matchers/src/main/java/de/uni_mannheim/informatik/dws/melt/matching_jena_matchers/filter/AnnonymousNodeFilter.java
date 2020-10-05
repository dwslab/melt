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
 * This filter removes correspondences where the source or target has not the same host of the OntModels.
 * E.g. it removes rdf:type=rdf:type or foaf:knows=foaf:knows
 */
public class AnnonymousNodeFilter extends MatcherYAAAJena implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnonymousNodeFilter.class);

    /**
     * Constructor
     */
    public AnnonymousNodeFilter(){
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
