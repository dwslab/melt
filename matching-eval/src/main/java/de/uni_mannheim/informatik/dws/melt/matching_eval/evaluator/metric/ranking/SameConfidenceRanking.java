package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enum for different sorting options in case correspondences have same confidence.
 * @see <a href="https://arxiv.org/pdf/1911.03903.pdf">https://arxiv.org/pdf/1911.03903.pdf</a> 
 */
public enum SameConfidenceRanking {

    /**
     * Sorts correspondences with same confidence in alphabetical order of source and target
     */
    ALPHABETICALLY,
    /**
     * Sorts correspondences with same confidence randomly.
     */
    RANDOM, 
    /**
     * Sorts true positive correspondences at the top of each confidence class.
     */
    TOP,     
    /**
     * Sorts true positive correspondences at the bottom of each confidence class.
     */
    BOTTOM;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SameConfidenceRanking.class);
    
    public List<Correspondence> sortAlignment(Alignment systemAlignment, Alignment referenceAlignment){
        TreeMap<Double, List<Correspondence>> confidenceSortedAlignment = new TreeMap<>();
        for(Correspondence c : systemAlignment){
            confidenceSortedAlignment.computeIfAbsent(c.getConfidence(), k->new ArrayList<>()).add(c);
        }
        
        List<Correspondence> sortedAlignment = new ArrayList<>(systemAlignment.size());
        
        Iterator<Double> iterator = confidenceSortedAlignment.descendingKeySet().iterator();
        while(iterator.hasNext()) {
            Double confidence = iterator.next();
            List<Correspondence> correspondences = confidenceSortedAlignment.get(confidence);
            switch(this){
                case ALPHABETICALLY:
                    correspondences.sort(Comparator.comparing(Correspondence::getEntityOne).thenComparing(Correspondence::getEntityTwo));
                    sortedAlignment.addAll(correspondences);
                    break;
                case RANDOM:
                    Collections.shuffle(correspondences);
                    sortedAlignment.addAll(correspondences);
                    break;
                case TOP:
                    List<Correspondence> tail = new ArrayList<>();
                    for(Correspondence c : correspondences){
                        if(referenceAlignment.contains(c)){
                            sortedAlignment.add(c);
                        }else{
                            tail.add(c);
                        }
                    }
                    sortedAlignment.addAll(tail);
                    break;
                case BOTTOM:
                    List<Correspondence> tailList = new ArrayList<>();
                    for(Correspondence c : correspondences){
                        if(referenceAlignment.contains(c)){
                            tailList.add(c);
                        }else{
                            sortedAlignment.add(c);
                        }
                    }
                    sortedAlignment.addAll(tailList);
                    break;
                default:
                    LOGGER.error("SameConfidenceRanking enum is not implemented. Returning arbitrary order.");
                    sortedAlignment.addAll(correspondences);
            }
        }
        return sortedAlignment;
    }
}
