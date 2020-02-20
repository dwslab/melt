package de.uni_mannheim.informatik.dws.melt.matching_eval.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * A Counter is for counting arbitrary objects.
 */
public class Counter<T> {
    protected Map<T, Integer> counts;

    public Counter(){
        this.counts = new HashMap<>();
    }
    
    public void add(Collection<T> collection) {
        for(T element : collection){
            this.add(element);
        }
    }
    
    public void add(T t) {
        counts.merge(t, 1, Integer::sum);
    }
    
    public void add(T it, int v) {
        counts.merge(it, v, Integer::sum);
    }

    public int getCount(T t) {
        return counts.getOrDefault(t, 0);
    }
    
    /**
     * Return a list of the n most common elements and their counts.
     * Ordered from the most common to the least.
     * @param n number of common elements to return
     * @return list of entries
     */
    public List<Entry<T, Integer>> mostCommon(int n) {
        return counts.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(n)
                .collect(Collectors.toList());
    }
    
    /**
     * Return a list of the most common elements and their counts.
     * Ordered from the most common to the least.
     * @return list of entries
     */
    public List<Entry<T, Integer>> mostCommon() {
        return counts.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toList());
    }
    
}
