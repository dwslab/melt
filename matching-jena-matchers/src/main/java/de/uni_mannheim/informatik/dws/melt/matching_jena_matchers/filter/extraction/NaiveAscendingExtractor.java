
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceConfidenceComparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;

/**
 * Naive ascending extraction as shown in "Analyzing Mapping Extraction Approaches" (C. Meilicke, H. Stuckenschmidt).
 * It iterates over the sorted (ascending) correspondences and and uses the correspondence with the highest confidence.
 * Afterwards removes every other correspondence with the same source or target.
 * @see <a href="http://ceur-ws.org/Vol-304/paper3.pdf">Analyzing Mapping Extraction Approaches (C. Meilicke, H. Stuckenschmidt)</a>
 */
public class NaiveAscendingExtractor extends MatcherYAAAJena implements Filter {
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties p) throws Exception {
        return filter(inputAlignment);        
    }
    
    public static Alignment filter(Alignment inputAlignment){
        List<Correspondence> sortedAlignment = new ArrayList<>(inputAlignment);
        sortedAlignment.sort(new CorrespondenceConfidenceComparator()
                .thenComparing(Correspondence::getEntityOne)
                .thenComparing(Correspondence::getEntityTwo));
        
        Alignment tmpAlignment = new Alignment(true, true, false, false);
        tmpAlignment.addAll(inputAlignment);
        
        for(Correspondence c : sortedAlignment){
            tmpAlignment.remove(c);
            
            if(tmpAlignment.getDistinctSourcesAsSet().contains(c.getEntityOne()) ||
               tmpAlignment.getDistinctTargetsAsSet().contains(c.getEntityTwo())){
                inputAlignment.remove(c);
            }
        }
        return inputAlignment;
    }
}
