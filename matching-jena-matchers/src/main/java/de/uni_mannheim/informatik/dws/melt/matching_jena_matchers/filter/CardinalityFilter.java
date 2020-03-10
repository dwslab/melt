package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
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
 *
 */
public class CardinalityFilter extends MatcherYAAAJena{

   @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties p) throws Exception {
        return filter(inputAlignment);        
    }
    
    public static Alignment filter(Alignment inputAlignment){
        List<Correspondence> sortedAlignment = new ArrayList<>(inputAlignment);
        sortedAlignment.sort(new CorrespondenceConfidenceComparator().reversed()
                .thenComparing(Correspondence::getEntityOne)
                .thenComparing(Correspondence::getEntityTwo));
        Set<String> sourceMatches = new HashSet<>();
        Set<String> targetMatches = new HashSet<>();
        for(Correspondence c : sortedAlignment){
            if(sourceMatches.contains(c.getEntityOne()) || targetMatches.contains(c.getEntityTwo())){
                inputAlignment.remove(c);
            } else {
                sourceMatches.add(c.getEntityOne());
                targetMatches.add(c.getEntityTwo());
            }
        }
        return inputAlignment;                
    }
}
