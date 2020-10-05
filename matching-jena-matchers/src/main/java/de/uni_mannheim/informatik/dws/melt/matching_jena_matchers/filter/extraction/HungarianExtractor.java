package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation uses the Hungarian algorithm to find a one to one mapping.
 * The runtime highly depends on the lower number of concepts (source or target) of the alignment as well as the number of correspondences.
 * If a better runtime is needed, use {@link MaxWeightBipartiteExtractor}.
 */
public class HungarianExtractor extends MatcherYAAAJena implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HungarianExtractor.class);
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return filter(inputAlignment);
    }
    
    public static Alignment filter(Alignment inputAlignment){
        if(inputAlignment.getDistinctConfidencesAsSet().size() == 1){
            //alignment api says that hungarian algorithmn runs in inifite loop when all correspondences hav same confidence
            LOGGER.warn("The input alignment has only one confidence. Defaulting to make a random one to one alignment.");
            return NaiveDescendingExtractor.filter(inputAlignment);
        }
    
        List<String> sources = new ArrayList<>();
        inputAlignment.getDistinctSources().forEach(sources::add);
        Collections.sort(sources);//make deterministic
        
        List<String> targets = new ArrayList<>();
        inputAlignment.getDistinctTargets().forEach(targets::add);
        Collections.sort(targets);//make deterministic
        
        boolean switchSourceTarget = sources.size() > targets.size();
        
        //generate array
        double[][] values = switchSourceTarget ? new double[targets.size()][sources.size()] : new double[sources.size()][targets.size()];
        Map<String, Integer> sourceMap = getPositionMap(sources);
        Map<String, Integer> targetMap = getPositionMap(targets);
        for(Correspondence c : inputAlignment.getCorrespondencesRelation(CorrespondenceRelation.EQUIVALENCE)){
            int sourcePostion = sourceMap.get(c.getEntityOne());
            int targetPosition = targetMap.get(c.getEntityTwo());
            if(switchSourceTarget)
                values[targetPosition][sourcePostion] = c.getConfidence();
            else
                values[sourcePostion][targetPosition] = c.getConfidence();
        }
        
        int[][] assignment = HungarianAlgorithm.hgAlgorithm(values, "max");
        
        Set<Correspondence> goodCoorespondes = new HashSet<>();        
        for (int i = 0; i < assignment.length; i++){
            if(switchSourceTarget){
                goodCoorespondes.add(inputAlignment.getCorrespondence(sources.get(assignment[i][1]), targets.get(assignment[i][0]), CorrespondenceRelation.EQUIVALENCE));          
            }else{
                goodCoorespondes.add(inputAlignment.getCorrespondence(sources.get(assignment[i][0]), targets.get(assignment[i][1]), CorrespondenceRelation.EQUIVALENCE));          
            }
        }
        
        inputAlignment.retainAll(goodCoorespondes);
        return inputAlignment;
    }
    
    private static Map<String, Integer> getPositionMap(List<String> list){
        Map<String, Integer> map = new HashMap<>();
        for(int i=0; i < list.size(); i++){
            map.put(list.get(i), i);
        }
        return map;
    }
}
