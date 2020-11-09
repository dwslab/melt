package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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
     * Sorts correspondences with same confidence randomly but a seed is set and thus multiple runs, returns same results.
     */
    RANDOM_WITH_SEED, 
    /**
     * Sorts true positive correspondences at the top of each confidence class.
     */
    TOP,     
    /**
     * Sorts true positive correspondences at the bottom of each confidence class.
     */
    BOTTOM;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SameConfidenceRanking.class);
    
    
    /**
     * Sorts the system alignment and returns a list of correspondences.
     * The important part is when multiple correspondences have the same confidence.
     * In this situation the ranking is determined by the SameConfidenceRanking.
     * The reference alignment (second parameter) can be null, in case TOP and BOTTOM is not used.
     * @param systemAlignment the alignment to be sorted.
     * @param referenceAlignment the reference alignment which is only used for TOP and BOTTOM.
     * @return a sorted alignment
     */
    public List<Correspondence> sortAlignment(Iterable<Correspondence> systemAlignment, Collection<Correspondence> referenceAlignment){
        TreeMap<Double, List<Correspondence>> confidenceSortedAlignment = new TreeMap<>();
        int alignmentSize = 0;
        for(Correspondence c : systemAlignment){
            confidenceSortedAlignment.computeIfAbsent(c.getConfidence(), k->new ArrayList<>()).add(c);
            alignmentSize++;
        }
        
        List<Correspondence> sortedAlignment = new ArrayList<>(alignmentSize);
        
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
                case RANDOM_WITH_SEED:
                    Collections.shuffle(correspondences, RND);
                    sortedAlignment.addAll(correspondences);
                    break;
                case TOP:
                    if(referenceAlignment == null){
                        LOGGER.error("Chosen TOP as SameConfidenceRanking but provided no reference alignment. Returning arbitrary order.");
                        sortedAlignment.addAll(correspondences);
                    }else{
                        List<Correspondence> tail = new ArrayList<>();
                        for(Correspondence c : correspondences){
                            if(referenceAlignment.contains(c)){
                                sortedAlignment.add(c);
                            }else{
                                tail.add(c);
                            }
                        }
                        sortedAlignment.addAll(tail);
                    }                    
                    break;
                case BOTTOM:
                    if(referenceAlignment == null){
                        LOGGER.error("Chosen BOTTOM as SameConfidenceRanking but provided no reference alignment. Returning arbitrary order.");
                        sortedAlignment.addAll(correspondences);
                    }else{
                        List<Correspondence> tailList = new ArrayList<>();
                        for(Correspondence c : correspondences){
                            if(referenceAlignment.contains(c)){
                                tailList.add(c);
                            }else{
                                sortedAlignment.add(c);
                            }
                        }
                        sortedAlignment.addAll(tailList);
                    }
                    break;
                default:
                    LOGGER.error("SameConfidenceRanking enum is not implemented. Returning arbitrary order.");
                    sortedAlignment.addAll(correspondences);
            }
        }
        return sortedAlignment;
    }
    
    /**
     * Sorts the system alignment and returns a list of correspondences.
     * The important part is when multiple correspondences have the same confidence.
     * In this situation the ranking is determined by the SameConfidenceRanking.
     * Due to the fact that reference alignment is not given,  TOP and BOTTOM should be not used.
     * @param systemAlignment the alignment to be sorted.
     * @return a sorted alignment
     */
    public List<Correspondence> sortAlignment(Iterable<Correspondence> systemAlignment){
        return sortAlignment(systemAlignment, null);
    }
    
    
    /**
     * Sorts the system alignment and returns a list of correspondences.
     * The important part is when multiple correspondences have the same confidence.
     * In this situation the ranking is determined by the SameConfidenceRanking.
     * The reference alignment (second parameter) can be null, in case TOP and BOTTOM is not used.
     * @param systemAlignment the alignment to be sorted.
     * @param referenceAlignment the reference alignment which is only used for TOP and BOTTOM.
     * @return a sorted alignment
     */
    public List<Correspondence> sortAlignment(Alignment systemAlignment, Alignment referenceAlignment){
        return sortAlignment((Iterable<Correspondence>)systemAlignment, (Collection<Correspondence>)referenceAlignment);
    }
    
    
    private static Random RND = new Random(13246);
    
    /**
     * Sets the seed value for the RANDOM_WITH_SEED enum.
     * @param seed the seed value to use for generating the randomness.
     */
    public static void setSeed(long seed){
        RND = new Random(seed);
    }
}
