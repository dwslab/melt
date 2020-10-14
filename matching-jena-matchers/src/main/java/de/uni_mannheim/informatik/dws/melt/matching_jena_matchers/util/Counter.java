package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
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
    protected Comparator<Entry<T, ? extends Comparable>> mapComparator;

    /**
     * Create a new counter object with a default initial capacity. 
     */
    public Counter(){
        this.counts = new HashMap<>();
        this.overallCount = 0;
        this.mapComparator = (c1, c2) -> c2.getValue().compareTo(c1.getValue());
    }
    
    /**
     * Create a new counter object with a default initial capacity.
     * @param elementComparator the comparator to use if the count is the same
     */
    public Counter(Comparator<T> elementComparator){
        this.counts = new HashMap<>();
        this.overallCount = 0;
        this.mapComparator = (c1, c2) -> {
            int res = c2.getValue().compareTo(c1.getValue()); //compare the counts in descending order
            return (res != 0) ? res : elementComparator.compare(c1.getKey(), c2.getKey()); //then comparing by 
        };
    }
    
    /**
     * Create a new counter object from the given iterable.
     * @param iterable any iterable of type T
     */
    public Counter(Iterable<T> iterable){
        this();
        addAll(iterable);
    }
    
    /**
     * Create a new counter object from the given iterable.
     * @param iterable any iterable of type T
     * @param elementComparator the comparator to use if the count is the same 
     */
    public Counter(Iterable<T> iterable, Comparator<T> elementComparator){
        this(elementComparator);
        addAll(iterable);
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
     * Add this element to the counter multiple times.
     * @param t the element to add
     * @param howOften  how often this element should be added.
     */
    public void add(T t, int howOften) {
        if(howOften < 1)
            throw new IllegalArgumentException("Argument howOften is smaller than 1.");
        this.overallCount += howOften;
        MutableInt c = this.counts.get(t);
        if(c == null){
            this.counts.put(t, new MutableInt(howOften));
        }else{
            c.increment(howOften);
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
                .sorted(this.mapComparator)
                .map(e -> new SimpleEntry<>(e.getKey(), e.getValue().get()))
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
                .sorted(this.mapComparator)
                .map(e -> new SimpleEntry<>(e.getKey(), e.getValue().get()))
                .collect(Collectors.toList());
    }
    
    /**
     * Return a list of the most common elements with the highest count.
     * Usually this is only one element but if there are multiple ones with the same count,
     * then this function will return them all.
     * E.g. (a, a, a, b, b, b, c) results in a list of ((a,3), (b, 3)).
     * @return list of elements with their counts
     */
    public List<Entry<T, Integer>> mostCommonWithHighestCount() {
        List<Entry<T, Integer>> mostCommon = new ArrayList();
        int highestCount = -1;
        for(Entry<T, Integer> entry : mostCommon()){
            if(highestCount < 0){
                mostCommon.add(entry);
                highestCount = entry.getValue();
            }else if(highestCount == entry.getValue()){
                mostCommon.add(entry);
            }else{
                break;
            }
        }
        return mostCommon;
    }
    
    /**
     * Return a list of the elements with a higher or equal frequency/percentage than the given one
     * together with their frequency.
     * @param percentage between zero and one. 0.95 means 95 percent.
     * @return list of entries with frequency
     */
    public List<Entry<T, Double>> mostCommonByPercentage(double percentage) {
        if (percentage < 0.0 || percentage > 1.0)
            throw new IllegalArgumentException("Percentage: " + percentage);
        
        List<Entry<T, Double>> list = new ArrayList();
        for(Entry<T, MutableInt> count : this.counts.entrySet()){
            double frequency = (double)count.getValue().get() / (double) this.overallCount;
            if(frequency >= percentage){
                list.add(new SimpleEntry(count.getKey(), frequency));
            }
        }
        return list.stream()
            .sorted(this.mapComparator)
            .collect(Collectors.toList());
    }
    
    /**
     * Return a list of the elements with a higher or equal frequency/percentage than the given one.
     * @param percentage between zero and one. 0.95 menas 95 percent.
     * @return list of entries
     */
    public List<T> mostCommonElementsByPercentage(double percentage) {
        return mostCommonByPercentage(percentage).stream()
                .map(e -> e.getKey())
                .collect(Collectors.toList());
    }
    
    /**
     * Return a list of elements which have a higher (or equal) frequency than the specified one but not more than the specified amount(parameter topN).
     * If one of those two parameters is zero, then it will not be taken into account. Especially if both are zero, then all elements will be returned.
     * @param percentage between zero and one. 0.95 means 95 percent.
     * @param topN  the maximum number of common elements to return
     * @return list of elements (element with highest count first)
     */
    public List<T> mostCommonElementsByPercentageOrTopN(double percentage, int topN) {
        if(topN == 0){
            return this.mostCommonElementsByPercentage(percentage);
        }else if(percentage == 0){
            if(topN == 0)
                return this.mostCommonElements(); // if both are zero then return all
            return this.mostCommonElements(topN);
        }else{
            return mostCommonByPercentage(percentage).stream()
                .limit(topN)
                .map(e -> e.getKey())
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Return a list of the n most common elements.
     * Ordered from the most common to the least.
     * @param n number of common elements to return
     * @return list of elements
     */
    public List<T> mostCommonElements(int n) {
        return counts.entrySet().stream()
                .sorted(this.mapComparator)
                .map(e -> e.getKey())
                .limit(n)
                .collect(Collectors.toList());
    }
    
    /**
     * Return a list ordered by their number of occurences.
     * Ordered from the most common to the least.
     * @return list of elements
     */
    public List<T> mostCommonElements() {
        return counts.entrySet().stream()
                .sorted(this.mapComparator)
                .map(e -> e.getKey())
                .collect(Collectors.toList());
    }
    
    
    /**
     * Return the most common element.
     * @return most common element or null if counter is empty.
     */
    public T mostCommonElement() {
        return counts.entrySet().stream()
                .sorted(this.mapComparator)
                .map(e -> e.getKey())
                .findFirst().orElse(null);
    }
    
    
    /**
     * Return a list of elements with their frequency.
     * Only elements are returned where the frequency is between the given min and max arguments
     * (greter or equal to min and less or equal to max).
     * Ordered from the most common to the least.
     * @param min the min frequency (inclusive)
     * @param max the max frequency (inclusive)
     * @return list of elements with their frequency
     */
    public List<Entry<T, Double>> betweenFrequency(double min, double max) {
        if (min < 0.0 || min > 1.0)
            throw new IllegalArgumentException("min argument not between zero and one: " + min);
        if (max < 0.0 || max > 1.0)
            throw new IllegalArgumentException("max argument not between zero and one: " + min);
                
        List<Entry<T, Double>> list = new ArrayList();
        for(Entry<T, MutableInt> count : this.counts.entrySet()){
            double frequency = (double)count.getValue().get() / (double) this.overallCount;
            if(min <= frequency && frequency <= max){
                list.add(new SimpleEntry(count.getKey(), frequency));
            }
        }
        return list.stream()
            .sorted(this.mapComparator)
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return this.mostCommon().toString();
    }
    
     private static final String NEWLINE = System.getProperty("line.separator");
     
    /**
     * ToString method which returns the counter well formatted in multiple lines to have a better overview.
     * @return a string which contains the counter information in multiple lines.
     */
    public String toStringMultiline(){
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(NEWLINE);
        for(Entry<T, Integer> e : this.mostCommon()){
            sb.append("    ").append(e.getKey()).append("=").append(e.getValue()).append(",").append(NEWLINE);
        }
        sb.append("]");
        return sb.toString();
    }
    
    class MutableInt implements Comparable<MutableInt>{
        private int value;
        
        public MutableInt(){this.value = 1;}        
        public MutableInt(int initial){this.value = initial;}
        
        public void increment() { ++value; }
        public void increment(int increment) { value+=increment; }
        public int get() { return value; }

        @Override
        public int compareTo(MutableInt o) {
            return Integer.compare(this.value, o.value);
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }
}
