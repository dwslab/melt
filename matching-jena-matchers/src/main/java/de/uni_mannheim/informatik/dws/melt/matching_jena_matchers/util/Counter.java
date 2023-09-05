package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Counter is for counting arbitrary objects.
 * The number per object is integer, thus the maximum frequency of an entity should not be higher than 2,147,483,647.
 * @param <T> the datatype of the objects to count.
 */
public class Counter<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Counter.class);
    
    private Map<T, MutableInt> counts;
    private long overallCount;
    private Comparator<Entry<T, ? extends Comparable>> mapComparator;

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
     * Add a collection of elements to the counter.
     * @param iterator the iterator to be added.
     */
    public void addAll(Iterator<T> iterator) {
        while(iterator.hasNext()){
            this.add(iterator.next());
        }
    }
    
    /**
     * Adds another counter to this counter.
     * Only this counter is modified.
     * @param counter the other counter to add to this object. 
     */
    public void addAll(Counter<T> counter) {
        for(Entry<T, MutableInt> c : counter.counts.entrySet()){
            this.add(c.getKey(), c.getValue().value);
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
     * Removes one count of the given element from this counter (the element is still in the counter if it was added multiple times).
     * It will remove the element only if it is present (optional operation).
     * Returns true if this counter contained the specified element t (or
     * equivalently, if this collection changed as a result of the call).
     * In case you want to remove all counts of an element, then use {@link #removeAll(java.lang.Object) }.
     * @param t the element to remove
     * @return true if an element was removed
     */
    public boolean remove(T t){
        MutableInt c = this.counts.get(t);
        if(c == null){
            return false;
        }else{
            this.overallCount--;
            c.decrement();
            if(c.value <=0){
                this.counts.remove(t);
            }
            return true;
        }
    }
    
    /**
     * Removes the specified number of occurances of the given element from this counter.
     * It will remove the element only if it is present (optional operation).
     * Returns true if this counter contained the specified element t (or
     * equivalently, if this collection changed as a result of the call).
     * If the element should be removed more times, than it is added, the corresponding element is removed from this counter.
     * In case you want to remove all counts of an element, then use {@link #removeAll(java.lang.Object) }.
     * @param t the element to remove
     * @param howOften how often this element should be removed.
     * @return true if an element was removed
     */
    public boolean remove(T t, int howOften){
        MutableInt c = this.counts.get(t);
        if(c == null){
            return false;
        }else{
            if(c.value > howOften){
                c.decrement(howOften);
                this.overallCount-=howOften;
            }else if(c.value == howOften) {
                this.overallCount-=howOften;
                this.counts.remove(t);
            }else{
                this.overallCount-=c.value;
                this.counts.remove(t);
            }
            return true;
        }
    }
    
    /**
     * Removes the given element from this counter (even if it appears multiple times), if it is present (optional operation).
     * Returns true if this counter contained the specified element t (or
     * equivalently, if this collection changed as a result of the call).
     * @param t the element to remove
     * @return true if an element was removed
     */
    public boolean removeAll(T t){
        MutableInt c = this.counts.get(t);
        if(c == null){
            return false;
        }else{
            this.overallCount -= c.value;
            this.counts.remove(t);
            return true;
        }
    }
    
    /**
     * Removes the given element from this counter (even if it appears multiple times), if it is present (optional operation).
     * Returns true if this counter contained the specified element t (or
     * equivalently, if this collection changed as a result of the call).
     * @param elements the element to remove
     * @return true if the counter was changed/modified
     */
    public boolean removeAll(Collection<T> elements){
        boolean modified = false;
        for(T element : elements){
            if(this.removeAll(element)){
                modified = true;
            }
        }
        return modified;
    }
    
    /**
     * It will substract the other counter from this counter.
     * This counter will be modified and the counts are reduced.
     * @param other the other counter with values to be removed from this counter.
     * @return true if this counter was changed/modified
     */
    public boolean substract(Counter<T> other){
        boolean modified = false;
        for(Entry<T, MutableInt> entry : other.counts.entrySet()){
            if(this.remove(entry.getKey(), entry.getValue().value)){
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Get the count for a specific element.
     * If the element does not appear in this counter, 0 is returned.
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
    
    public boolean isEmpty(){
        return this.overallCount == 0;
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
     * Return a sorted map of the most common elements and their counts.
     * Ordered from the most common to the least.
     * @return sorted map
     */
    public LinkedHashMap<T, Integer> getSortedMap() {
        LinkedHashMap<T, Integer> lhm = new LinkedHashMap<>();
        counts.entrySet().stream()
                .sorted(this.mapComparator)
                .forEach(e -> lhm.put(e.getKey(), e.getValue().get()));
        return lhm;
    }
    
    /**
     * Return a sorted map of the most common elements (limit by n) and their counts.
     * Ordered from the most common to the least.
     * @param n the number of elements to put in the sorted map
     * @return sorted map
     */
    public LinkedHashMap<T, Integer> getSortedMap(int n) {
        LinkedHashMap<T, Integer> lhm = new LinkedHashMap<>();
        counts.entrySet().stream()
                .sorted(this.mapComparator)
                .limit(n)
                .forEach(e -> lhm.put(e.getKey(), e.getValue().get()));
        return lhm;
    }
    
    /**
     * Return a list of the most common elements with the highest count.
     * Usually this is only one element but if there are multiple ones with the same count,
     * then this function will return them all.
     * E.g. (a, a, a, b, b, b, c) results in a list of ((a,3), (b, 3)).
     * @return list of elements with their counts
     */
    public List<Entry<T, Integer>> mostCommonWithHighestCount() {
        List<Entry<T, Integer>> mostCommon = new ArrayList<>();
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
        
        List<Entry<T, Double>> list = new ArrayList<>();
        for(Entry<T, MutableInt> count : this.counts.entrySet()){
            double frequency = (double)count.getValue().get() / (double) this.overallCount;
            if(frequency >= percentage){
                list.add(new SimpleEntry<>(count.getKey(), frequency));
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
     * (greater or equal to min and less or equal to max).
     * Ordered from the most common to the least.
     * @param min the min frequency (inclusive)
     * @param max the max frequency (inclusive)
     * @return list of elements with their frequency
     */
    public List<Entry<T, Double>> betweenFrequency(double min, double max) {
        return betweenFrequencyRelativeToTotal(min, max, this.overallCount);
    }
    
    /**
     * Return a set of elements where the frequency is between the given min and max arguments
     * (greater or equal to min and less or equal to max).
     * Ordered from the most common to the least.
     * @param min the min frequency (inclusive)
     * @param max the max frequency (inclusive)
     * @return set of elements
     */
    public Set<T> betweenFrequencyReturningElements(double min, double max) {
        return betweenFrequencyRelativeToTotalReturningElements(min, max, this.overallCount);
    }
    
    /**
     * Return a list of elements with their frequency relative to the given total number.
     * Only elements are returned where the frequency is between the given min and max arguments
     * (greater or equal to min and less or equal to max).
     * Ordered from the most common to the least.
     * @param min the min frequency (inclusive)
     * @param max the max frequency (inclusive)
     * @param total the total number to compare with
     * @return list of elements with their frequency
     */
    public List<Entry<T, Double>> betweenFrequencyRelativeToTotal(double min, double max, long total) {
        if (min < 0.0 || min > 1.0)
            throw new IllegalArgumentException("min argument not between zero and one: " + min);
        if (max < 0.0 || max > 1.0)
            throw new IllegalArgumentException("max argument not between zero and one: " + min);
                
        List<Entry<T, Double>> list = new ArrayList<>();
        for(Entry<T, MutableInt> count : this.counts.entrySet()){
            double frequency = (double)count.getValue().get() / (double) total;
            if(min <= frequency && frequency <= max){
                list.add(new SimpleEntry<>(count.getKey(), frequency));
            }
        }
        return list.stream()
            .sorted(this.mapComparator)
            .collect(Collectors.toList());
    }
    
    /**
     * Return a set of elements where the frequency is between the given min and max arguments
     * (greater or equal to min and less or equal to max). It calculates the frequency compared to the parameter total.
     * Ordered from the most common to the least.
     * @param min the min frequency (inclusive)
     * @param max the max frequency (inclusive)
     * @param total the total number to compare with
     * @return set of elements
     */
    public Set<T> betweenFrequencyRelativeToTotalReturningElements(double min, double max, long total) {
        if (min < 0.0 || min > 1.0)
            throw new IllegalArgumentException("min argument not between zero and one: " + min);
        if (max < 0.0 || max > 1.0)
            throw new IllegalArgumentException("max argument not between zero and one: " + min);
                
        Set<T> set = new HashSet<>();
        for(Entry<T, MutableInt> count : this.counts.entrySet()){
            double frequency = (double)count.getValue().get() / (double) total;
            if(min <= frequency && frequency <= max){
                set.add(count.getKey());
            }
        }
        return set;
    }

    @Override
    public String toString() {
        return this.mostCommon().toString();
    }
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    
    
    
    /**
     * ToString method which returns the given entries well formatted in multiple lines.
     * @param entries the values which should be nicely formatted
     * @return a string which contains the entries information in multiple lines.
     */      
    public String toStringMultiline(Collection<Entry<T, Double>> entries){
        LinkedHashMap<T, Double> lhm = new LinkedHashMap<>();
        entries.stream()
                .sorted(this.mapComparator)
                .forEach(e -> lhm.put(e.getKey(), e.getValue()));
        try{
            return JSON_MAPPER.writeValueAsString(lhm);
        } catch (IOException ex) {
            LOGGER.error("Could not serialize Counter to string", ex);
            return "";
        }
    }    
    
    /**
     * ToString method which returns the counter well formatted in multiple lines to have a better overview.
     * @return a string which contains the counter information in multiple lines.
     */
    public String toStringMultiline(){
        return toJson();
    }
    
    /**
     * ToString method which returns return values from functions like {@link #mostCommon(int) } well formatted in multiple lines to have a better overview.
     * @param n the number of elements to print as multiline
     * @return a string which contains the counter information in multiple lines.
     */
    public String toStringMultiline(int n){
        try{
            return JSON_MAPPER.writeValueAsString(this.getSortedMap(n));
        } catch (IOException ex) {
            LOGGER.error("Could not serialize object to string", ex);
            return "";
        }
    }
    
    /**
     * Returns a json representation of this counter.
     * @return a json representation
     */
    public String toJson(){
        try{
            return JSON_MAPPER.writeValueAsString(this.getSortedMap());
        } catch (IOException ex) {
            LOGGER.error("Could not serialize Counter to string", ex);
            return "";
        }
    }
    
    /**
     * Write the counter to a file (content is json).
     * @param file the file to use for writing the JSON content.
     */
    public void toJson(File file){
        try{
            JSON_MAPPER.writeValue(file, this.getSortedMap());
        } catch (IOException ex) {
            LOGGER.error("Could not write Counter to file", ex);
        }
    }
    
    /**
     * Given a json representation of a counter, create a new counter.
     * The comparator is not loaded/serialized.
     * @param jsonRepresentation the json representation of a counter.
     * @return the new counter
     */
    public static Counter<String> loadFromJsonString(String jsonRepresentation){
        try(JsonParser jParser = new JsonFactory().createParser(jsonRepresentation)){
            return loadFromJsonParser(jParser);
        } catch (IOException ex) {
            LOGGER.warn("Could not parse JSON from string correctly.", ex);
            return new Counter<>();
        }
    }
    
    /**
     * Load a counter instance from a json file.
     * The comparator is not loaded/serialized.
     * @param jsonFile the json file.
     * @return the new instance
     */
    public static Counter<String> loadFromJsonFile(File jsonFile){
        try(JsonParser jParser = new JsonFactory().createParser(jsonFile)){
            return loadFromJsonParser(jParser);
        } catch (IOException ex) {
            LOGGER.warn("Could not parse JSON from file correctly.", ex);
            return new Counter<>();
        }
    }
    
    private static Counter<String> loadFromJsonParser(JsonParser parser) throws IOException{
        Counter<String> c = new Counter<>();        
        // Check the first token
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected content to be an object");
        }
        int i = 0;
        // Iterate over the tokens until the end of the object
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            //read one key value pair
            i++;
            if(i % 500000 == 0)
                LOGGER.info("Parse counter entry {}", i);            
            String key = parser.getCurrentName();
            parser.nextToken();
            int value = parser.getIntValue();
            c.add(key, value);
        }
        return c;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.counts);
        hash = 53 * hash + (int) (this.overallCount ^ (this.overallCount >>> 32));
        hash = 53 * hash + Objects.hashCode(this.mapComparator);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Counter<?> other = (Counter<?>) obj;
        if (this.overallCount != other.overallCount) {
            return false;
        }
        if (!Objects.equals(this.counts, other.counts)) {
            return false;
        }
        if (!Objects.equals(this.mapComparator, other.mapComparator)) {
            return false;
        }
        return true;
    }
    
    class MutableInt implements Comparable<MutableInt>{
        private int value;
        
        public MutableInt(){this.value = 1;}        
        public MutableInt(int initial){this.value = initial;}
        
        public void decrement() { --value; }
        public void decrement(int decrement) { value-=decrement; }
        
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

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + this.value;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MutableInt other = (MutableInt) obj;
            if (this.value != other.value) {
                return false;
            }
            return true;
        }
    }
}
