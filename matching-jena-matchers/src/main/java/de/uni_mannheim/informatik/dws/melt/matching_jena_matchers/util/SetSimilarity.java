package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Different methods for set comparison like Overlap coefficient, Jaccard or Sørensen–Dice_coefficient (DSC).
 */
public enum SetSimilarity {    
    /**
     * Returns 1 if intersection is not empty and 0 if it is empty.
     */
    BOOLEAN,
    /**
     * Returns absolute number of the intersection. Not a real comparison.
     */
    ABSOLUTE,
    /**
     * The intersection divided by the smaller of the size of the two sets.
     * Also known as <a href="https://en.wikipedia.org/wiki/Overlap_coefficient">Overlap coefficient</a>.
     */
    MIN,    
    /**
     * The intersection divided by the larger of the size of the two sets.
     */
    MAX,    
    /**
     * <a href="https://en.wikipedia.org/wiki/Jaccard_index">Jaccard index</a>: intersection divided by the size of the union.
     */
    JACCARD,    
    /**
     * <a href="https://en.wikipedia.org/wiki/Sorensen-Dice_coefficient">Sørensen-Dice coefficient (DSC)</a>:
     * twice the number of intersection set divided by the sum of the number of elements in each set.
     */
    DICE;
    
    /**
     * Compute the set comparison.
     * @param <E> Generic type of the sets
     * @param x set x
     * @param y set y
     * @return set comparison value
     */
    public <E>double compute(Set<E> x, Set<E> y){
        Set<E> intersection = new HashSet(x);
        intersection.retainAll(y);
        return compute(intersection.size(), x.size(), y.size());
    }
    
    /**
     * Compute the set comparison.
     * @param sizeIntersection the number of intersection of the two sets
     * @param sizeX size of set x
     * @param sizeY size of set y
     * @return set comparison value
     */
    public double compute(int sizeIntersection, int sizeX, int sizeY){
        switch(this){
            case BOOLEAN:
                return sizeIntersection > 0 ? 1.0d : 0.0d;
            case ABSOLUTE:
                return sizeIntersection;
            case MIN:
                int min = Math.min(sizeX, sizeY);
                if(min == 0)
                    return 0.0d;
                return ((double)sizeIntersection) / min;
            case MAX:
                int max = Math.max(sizeX, sizeY);
                if(max == 0)
                    return 0.0d;
                return ((double)sizeIntersection) / max;
            case JACCARD:
                int sizeUnion = (sizeX + sizeY) - sizeIntersection;
                if(sizeUnion == 0)
                    return 0.0d;
                return ((double)sizeIntersection) / sizeUnion;
            case DICE:
                int sizeXY = sizeX + sizeY;
                if(sizeXY == 0)
                    return 0.0d;
                return ((double) 2 * sizeIntersection) / (sizeX + sizeY);
            default:
                throw new UnsupportedOperationException("Computation for enum not implemented.");
        }
    }
}
