package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Computes a transitive closure in RAM.
 * This class is generic and can hold arbitrary classes as elements for the transitive closure.
 * One can add elements which should belong to the same identity set via the add methods.
 * Afterward, the computed identity sets can be retrived by the {@link #getClosure()} call.
 * Remove methods are not implemented (just create new instances of TransitiveClosure).
 * Example:
 * <pre>{@code 
 * TransitiveClosure<String> tc = new TransitiveClosure<>();
 * tc.add("A", "B");
 * tc.add("B", "C", "D");
 * tc.add("E", "F");
 * 
 * tc.getClosure();
 * //returns [ {"A", "B", "C", "D"}, {"E", "F"} ]
 * }</pre>
 */
public class TransitiveClosure <T> {
    
    /**
     * Map from object to synset ID
     */
    private Map<T, Integer> objectToId;
    
    /**
     * Map from synset ID to actual closure (set of objects)
     */
    private Map<Integer, Set<T>> idToClosure;
    
    /**
     * id counter to be used when generating new synset ids.
     */
    private int idCounter;
    
    /**
     * Initialize an empty transitive closure.
     */
    public TransitiveClosure(){
        this.objectToId = new HashMap<>();
        this.idToClosure = new HashMap<>();
        this.idCounter = 0;
    }
    
    /**
     * Adds elements to this transitive closure.
     * All items in the elements parameter are assumed to be equal.
     * Usually these are two elements (like A - B)
     * @param elements iterable of items which are equal.
     */
    public void add(T... elements){
        add(Arrays.asList(elements));
    }
    
    /**
     * Adds another transitive closure to this object.
     * Only this object is modified. The parameter transitiveClosure is not modified.
     * @param transitiveClosure other transitive closure which is added to this object
     */
    public void add(TransitiveClosure<T> transitiveClosure){
        for(Set<T> identitySet : transitiveClosure.getClosure()){
            add(identitySet);
        }
    }
    
    /**
     * Adds elements to this transitive closure.
     * All items in the elements parameter are assumed to be equal.
     * Usually these are two elements (like A - B)
     * @param elements iterable of items which are equal.
     */
    public void add(Iterable<T> elements){
        Set<T> elementsNoClosureID = new HashSet<>();
        Set<Integer> foundClosureIDs = new HashSet<>();
        
        for(T element : elements){
            Integer closureID = this.objectToId.get(element);
            if(closureID == null){
                elementsNoClosureID.add(element);
            }else{
                foundClosureIDs.add(closureID);
            }
        }
        
        if(foundClosureIDs.isEmpty()){
            //create new closure id, add all elements with no closure id
            this.idToClosure.put(this.idCounter, elementsNoClosureID);
            for(T element : elementsNoClosureID){
                this.objectToId.put(element, this.idCounter);
            }
            this.idCounter++;
        }else{
            //get largest closure and merge all smaller ones into that
            Set<T> largestClosure = null;
            int largetClosureSize = 0;
            int largestClosureID = 0;
            Set<T> elementsToAdd = new HashSet<>();
            for(Integer closureID : foundClosureIDs){
                Set<T> closure = this.idToClosure.get(closureID);
                if(closure.size() > largetClosureSize){
                    if(largestClosure != null){
                        this.idToClosure.remove(largestClosureID);
                        elementsToAdd.addAll(largestClosure);                        
                    }
                    largestClosure = closure;
                    largetClosureSize = closure.size();
                    largestClosureID = closureID;                    
                }else{
                    this.idToClosure.remove(closureID);
                    elementsToAdd.addAll(closure);        
                }
            }
            
            for(T element : elementsToAdd){
                largestClosure.add(element);
                this.objectToId.put(element, largestClosureID);
            }
            
            for(T element : elementsNoClosureID){
                largestClosure.add(element);
                this.objectToId.put(element, largestClosureID);
            }
        }
    }
    
    /**
     * Returns the transitive closure.
     * This means a collections of identity sets auch that all elements in one set belong to one identity.
     * @return transitive closure
     */
    public Collection<Set<T>> getClosure(){
        return this.idToClosure.values();
    }
    
    /**
     * Checks if all given elements belong to the same identity set.
     * Returns true if this is the case , false otherwise.
     * @param elements all elements to check.
     * @return true if all given elements belong to the same identity set
     */
    public boolean belongToTheSameCluster(T... elements){
        return belongToTheSameCluster(Arrays.asList(elements));
    }
    
    /**
     * Checks if all given elements belong to the same identity set.
     * Returns true if this is the case , false otherwise.
     * @param elements all elements to check.
     * @return true if all given elements belong to the same identity set
     */
    public boolean belongToTheSameCluster(Iterable<T> elements){
        Set<Integer> foundClosureIDs = new HashSet<>();        
        for(T element : elements){
            Integer closureID = this.objectToId.get(element);
            if(closureID == null){
                return false;
            }else{
                foundClosureIDs.add(closureID);
            }
        }            
        return foundClosureIDs.size() <= 1;        
    }
    
    /**
     * Returns the identity set in which the given object is stored.
     * @param element the element to search the identiry set for
     * @return the identity set
     */
    public Set<T> getIdentitySetForElement(T element){
        Integer closureID = this.objectToId.get(element);
        if(closureID == null){
            return null;
        }else{
            return this.idToClosure.getOrDefault(closureID, null);
        }
    }
    
    /**
     * Returns the internal id which represents the identity set.
     * Only use if an arbitrary id is fine for the caller.
     * @param element the element to look for the internal id.
     * @return the internal id which represents the identity set.
     */
    public Integer getIdentityID(T element){
        return this.objectToId.get(element);
    }
    
    /**
     * Returns the number of all elements in this transitive closure (regardless of their identity set).
     * If it contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     * @return the number of all elements in this transitive closure.
     */
    public int countOfAllElements(){
        return this.objectToId.size();
    }
    
    /**
     * Returns the number of identity sets in this transitive closure.
     * If it contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     * @return the number of identity sets.
     */
    public int countOfIdentitySets(){
        return this.idToClosure.size();
    }
}
