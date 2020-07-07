package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Counter is for counting arbitrary objects.
 * The number per object is integer, thus the maximum frequency of an entity should not be higher than 2,147,483,647.
 */
public class Counter<T> {
    protected Map<T, MutableInt> counts;
    protected long overallCount;

    public Counter(){
        this.counts = new HashMap<>();
        this.overallCount = 0;
    }
    
    /**
     * Add a collection of elements to the counter.
     * @param iterable the iterable to be added.
     */
    public void addAll(Iterable<T> iterable) {
        for(T element : iterable){
            this.add(element);
        }
    }
    
    /**
     * Add one element to the counter
     * @param t the element to add
     */
    public void add(T t) {
        this.overallCount++;
        MutableInt c = this.counts.get(t);
        if(c == null){
            this.counts.put(t, new MutableInt());
        }else{
            c.increment();
        }
    }

    /**
     * Get the count for a specific element.
     * @param t the element for which the count should be returned
     * @return how often this element occured.
     */
    public int getCount(T t) {
        MutableInt i = this.counts.get(t);
        if(i == null)
            return 0;
        return i.get();
    }
    
    /**
     * Get the number of how many elements which were added to this counter (this includes duplicates).
     * @return how many elements are added to this list
     */
    public long getCount() {
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
                .map(e -> new SimpleEntry<>(e.getKey(), e.getValue().get()))
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
                .map(e -> new SimpleEntry<>(e.getKey(), e.getValue().get()))
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toList());
    }
    
    /**
     * Return a list of the elements with a given percentage and their frequency (higher or same frequency).
     * @param percentage between zero and one. 0.95 means 95 percent.
     * @return list of entries with frequency
     */
    public List<Entry<T, Double>> mostCommonByPercentage(double percentage) {
        if (percentage < 0.0 || percentage > 1.0)
            throw new IndexOutOfBoundsException("Percentage: " + percentage);
        
        List<Entry<T, Double>> list = new ArrayList();
        for(Entry<T, MutableInt> count : this.counts.entrySet()){
            double frequency = (double)count.getValue().get() / (double) this.overallCount;
            if(frequency >= percentage){
                list.add(new SimpleEntry(count.getKey(), frequency));
            }
        }
        return list.stream()
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .collect(Collectors.toList());
    }
    
    /**
     * Return a list of the elements with a given percentage.
     * @param percentage between zero and one. 0.95 menas 95 percent.
     * @return list of entries
     */
    public List<T> mostCommonElementsByPercentage(double percentage) {
        return mostCommonByPercentage(percentage).stream()
                .map(e -> e.getKey())
                .collect(Collectors.toList());
    }
    
    /**
     * Return a list of the n most common elements.
     * Ordered from the most common to the least.
     * @param n number of common elements to return
     * @return list of elements
     */
    public List<T> mostCommonElements(int n) {
        return counts.entrySet().stream()
                .map(e -> new SimpleEntry<>(e.getKey(), e.getValue().get()))
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(n)
                .map(e -> e.getKey())
                .collect(Collectors.toList());
    }
    
    /**
     * Return the most common element.
     * @return most common element or null if counter is empty.
     */
    public T mostCommonElement() {
        return counts.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().get(), e1.getValue().get()))
                .map(e -> e.getKey())
                .findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return this.mostCommon().toString();
    }
    
    class MutableInt {
        private int value = 1;
        public void increment() { ++value; }
        public void increment(int increment) { value+=increment; }
        public int get() { return value; }
    }
}
