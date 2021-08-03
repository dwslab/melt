package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.addnegatives;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

/**
 * A helper class to randomly sample elements from an initial set.
 * @param <E> the type of element in the set
 */
public class RandomSampleSet <E> {


    private final Random rand;
    
    /**
     * To this list no elements are added and it is initialized by a set.
     * Thus we can make sure, that the list contains no two same elements.
     */
    private final List<E> list;
    private double listSize;
    
    public RandomSampleSet(Set<E> set){
        this.list = new ArrayList<>(set);
        this.rand = new Random();
        this.listSize = this.list.size();
    }
    
    public RandomSampleSet(Set<E> set, Random rand){
        this.list = new ArrayList<>(set);
        this.rand = rand;
        this.listSize = this.list.size();
    }
    
    public RandomSampleSet(Set<E> set, long seed){
        this(set, new Random(seed));
    }
    
    
    public E getRandomElement(){
        if(this.list.isEmpty()){
            throw new NoSuchElementException("The list is empty and thus no random element can be returned.");
        }
        return list.get(this.rand.nextInt(list.size()));
    }    
    
    public E getRandomElement(Set<E> exclude){
        if(this.list.isEmpty()){
            throw new NoSuchElementException("The list is empty and thus no random element can be returned.");
        }
        if((exclude.size() / this.listSize) > 0.9){ // if excludes makes 90% of the list
            Set<E> s = new LinkedHashSet<>(this.list);
            s.removeAll(exclude);
            if(s.isEmpty())
                throw new NoSuchElementException("The list is empty and thus no random element can be returned.");
            List<E> k = new ArrayList<>(s);
            return k.get(this.rand.nextInt(k.size()));
        }else{
            while(true){
                E element = list.get(this.rand.nextInt(list.size()));
                if(exclude.contains(element) == false){
                    return element;
                }
            }
        }
    }
    
    public List<E> getRandomElementsWithRepetition(int countOfElements){
        //check that we have enough values
        if(this.list.isEmpty()){
            throw new NoSuchElementException("The list is empty and thus no random element can be returned.");
        }
        List<E> generated = new ArrayList<>();
        for(int i=0; i < countOfElements; i++){
            generated.add(list.get(this.rand.nextInt(list.size())));
        }
        return generated;
    }
    
    public List<E> getRandomElementsWithRepetition(int countOfElements, Set<E> exclude){
        //check that we have enough values
        if(this.list.isEmpty()){
            throw new NoSuchElementException("The list is empty and thus no random element can be returned.");
        }
        
        //we assume here, that all values in exclude also appears in the set/list given in the constructor
        //but even when this is not the case, the algorithm just takes longer
        
        if((exclude.size() / this.listSize) > 0.9){ // if excludes makes 90% of the list
            Set<E> s = new LinkedHashSet<>(this.list);
            s.removeAll(exclude);
            if(s.isEmpty())
                throw new NoSuchElementException("The list is empty and thus no random element can be returned.");
            List<E> k = new ArrayList<>(s);
            List<E> generated = new ArrayList<>();
            for(int i=0; i < countOfElements; i++){
                generated.add(k.get(this.rand.nextInt(k.size())));
            }
            return generated;
        }else{
            List<E> generated = new ArrayList<>();
            while(generated.size() < countOfElements){
                E element = list.get(this.rand.nextInt(list.size()));
                if(exclude.contains(element)){
                    continue;
                }
                generated.add(element);
            }
            return generated;
        }
    }
    
    public List<E> getRandomElementsWithoutRepetition(int countOfElements){
        if(countOfElements <= 0)
            throw new IllegalArgumentException("countOfElements to return should be greater than zero");
        //check that we have enough values
        if(this.list.size() < countOfElements){
            throw new NoSuchElementException("There are not enough elements in the list to return random elements.");
        }
        //decide between the sampling methods
        if((countOfElements / this.listSize) > 0.1){ // more than 10 percent of the whole list
            List<E> l = new ArrayList<>(this.list);
            Collections.shuffle(l, this.rand);
            return new ArrayList<>(l.subList(0, countOfElements));
        }else{
            Set<E> generated = new LinkedHashSet<>(); // LinkedHashSet to maintain insertion order
            while (generated.size() < countOfElements){
                E element = list.get(this.rand.nextInt(list.size()));
                generated.add(element);
            }
            return new ArrayList<>(generated);
        }
    }
    
    public List<E> getRandomElementsWithoutRepetition(int countOfElements, Set<E> exclude){
        //check that we have enough values
        if(this.list.isEmpty()){
            throw new NoSuchElementException("The list is empty and thus no random element can be returned.");
        }
        
        if((exclude.size() / this.listSize) > 0.9 || // if excludes makes 90% of the list
            this.list.size() == exclude.size() || // to prevent zero division below
            countOfElements / (double) (this.list.size() - exclude.size())  > 0.1 ){ //or if the remaining elements are too less to be randomly drawn 
            
            Set<E> s = new LinkedHashSet<>(this.list);
            s.removeAll(exclude);
            if(s.size() < countOfElements)
                throw new NoSuchElementException("There are not enough elements in the list to return random elements.");
            List<E> k = new ArrayList<>(s);
            Collections.shuffle(k, this.rand);
            return new ArrayList<>(k.subList(0, countOfElements));
        }else{
            Set<E> generated = new LinkedHashSet<>(); // LinkedHashSet to maintain insertion order
            while (generated.size() < countOfElements){
                E element = list.get(this.rand.nextInt(list.size()));
                if(exclude.contains(element))
                    continue;
                generated.add(element);
            }
            return new ArrayList<>(generated);
        }        
    }

}
