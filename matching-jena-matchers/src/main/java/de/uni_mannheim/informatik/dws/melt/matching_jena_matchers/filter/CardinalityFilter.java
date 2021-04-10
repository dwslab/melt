package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.NaiveDescendingExtractor;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Properties;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import org.apache.jena.ontology.OntModel;

/**
 * This filter returns only the alignments with the highest confidence if there are n-to-m matched elements.
 * This might not be the best solution.
 * @deprecated use {@link NaiveDescendingExtractor}.
 */
public class CardinalityFilter extends MatcherYAAAJena implements Filter {


   @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties p) throws Exception {
        return filter(inputAlignment);        
    }
    
    public static Alignment filter(Alignment inputAlignment){
        return NaiveDescendingExtractor.filter(inputAlignment);
    }
}
