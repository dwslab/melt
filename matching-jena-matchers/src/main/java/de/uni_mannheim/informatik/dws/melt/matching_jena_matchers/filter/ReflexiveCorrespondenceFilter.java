package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;

/**
 * Removes all reflexive edges (which maps A to A) from an alignment.
 */
public class ReflexiveCorrespondenceFilter extends MatcherYAAAJena implements Filter{
     

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return removeReflexiveCorrespondences(inputAlignment);
    }
    
    /**
     * Removes all reflexive edges (which maps A to A) from an alignment.
     * The given alignment is used directly and is modified.
     * Thus the returned value is the same object.
     * @param alignment the alignment to be processed.
     * @return an alignment without reflexive edges.
     */
    public static Alignment removeReflexiveCorrespondences(Alignment alignment){
        for(Correspondence c : alignment){
            if(c.getEntityOne().equals(c.getEntityTwo()))
                alignment.remove(c);
        }
        return alignment;
    }
}
