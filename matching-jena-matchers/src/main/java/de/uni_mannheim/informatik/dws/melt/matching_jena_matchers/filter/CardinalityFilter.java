package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.NaiveDescendingExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_yaaa.MatcherYAAA;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceConfidenceComparator;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;

/**
 * This filter returns only the alignments with the highest confidence if there are n-to-m matched elements.
 * This might not be the best solution.
 * @deprecated use {@link NaiveDescendingExtractor}.
 */
public class CardinalityFilter extends MatcherYAAAJena{

   @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties p) throws Exception {
        return filter(inputAlignment);        
    }
    
    public static Alignment filter(Alignment inputAlignment){
        return NaiveDescendingExtractor.filter(inputAlignment);
    }
}
