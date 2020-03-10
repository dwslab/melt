package de.uni_mannheim.informatik.dws.melt.matching_eval.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Counter is for counting arbitrary objects.
 */
public class Counter<T> {
    protected Map<T, Integer> counts;
    protected int overallCount;

    public Counter(){
        this.counts = new HashMap<>();
        this.overallCount = 0;
    }
    
    /**
     * Add a collection of elements to the counter.
     * @param collection the collection to be added.
     */
    public void add(Collection<T> collection) {
        for(T element : collection){
            this.add(element);
        }
    }
    
    /**
     * Add one element to the counter
     * @param t the element to add
     */
    public void add(T t) {
        this.overallCount++;
        counts.merge(t, 1, Integer::sum);
    }
    
    /**
     * Add the element (first parameter) multiple times (amount is specified in the second parameter).
     * @param it the element to add
     * @param v how often does this element should be added
     */
    public void add(T it, int v) {
        this.overallCount += v;
        counts.merge(it, v, Integer::sum);
    }

    /**
     * Get the count for a specific element.
     * @param t the element for which the count should be returned
     * @return how often this element occured.
     */
    public int getCount(T t) {
        return counts.getOrDefault(t, 0);
    }
    
    /**
     * Get the number of how many elements which were added to this counter (this includes duplicates).
     * @return how many elements are added to this list
     */
    public int getCount() {
        return overallCount;
    }
    
    /**
     * Returns the set of distinct elements.
     * @return the set of distinct elements
     */
    public Set<T> getDistinctElements() {
        return counts.keySet();
    }
    
    /**
     * Returns the amount of distinct elements.
     * @return amount of distinct elements
     */
    public int getAmountOfDistinctElements() {
        return counts.size();
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
