package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.filter;

import de.uni_mannheim.informatik.dws.ontmatching.matchingyaaa.MatcherYAAA;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.CorrespondenceConfidenceComparator;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * This filter returns only the alignments with the highest confidence if there are n-to-m matched elements.
 * This might not be the best solution.
 */
public class CardinalityFilter extends MatcherYAAA{

   @Override
    public Alignment match(URL source, URL target, Alignment inputAlignment, Properties p) throws Exception {
        return filter(inputAlignment);        
    }
    
    public static Alignment filter(Alignment inputAlignment){
        List<Correspondence> sortedAlignment = new ArrayList<>(inputAlignment);
        sortedAlignment.sort(new CorrespondenceConfidenceComparator().reversed());
        
        Set<String> sourceMatches = new HashSet<>();
        Set<String> targetMatches = new HashSet<>();
        for(Correspondence c : sortedAlignment){
            if(sourceMatches.contains(c.getEntityOne()) || targetMatches.contains(c.getEntityTwo())){
                inputAlignment.remove(c);
            }else{
                sourceMatches.add(c.getEntityOne());
                targetMatches.add(c.getEntityTwo());
            }
        }
        return inputAlignment;                
    }
}
