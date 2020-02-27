package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Computes a transitive closure in RAM.
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
     * id counter to be used when generating new synste ids.
     */
    private int idCounter;
    
    public TransitiveClosure(){
        this.objectToId = new HashMap<>();
        this.idToClosure = new HashMap<>();
        this.idCounter = 0;
    }
    
    public void add(T... elements){
        add(Arrays.asList(elements));
    }
    
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
     * @return transitive closure
     */
    public Collection<Set<T>> getClosure(){
        return this.idToClosure.values();
    }
}
