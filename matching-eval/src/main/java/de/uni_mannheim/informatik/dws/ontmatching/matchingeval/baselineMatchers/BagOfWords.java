package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.baselineMatchers;

import java.util.ArrayList;

/**
 * Data structure keeps the notion of the original ordering but the equals method will ignore the ordering
 */
public class BagOfWords extends ArrayList<String> {

    /**
     * Cached hash code.
     * IMPORTANT: MAKE VOLATILE IFF USED IN MULTI THREAD ENVIRONMENT.
     */
    private int hashCode = -1;


    /**
     * Constructor
     * @param tokensToBeAdded
     */
    public BagOfWords(String[] tokensToBeAdded){
        for(String token : tokensToBeAdded){
            this.add(token);
        }
    }


    /**
     * Two BagOfWords qualify as the same object if they contain the same objects.
     * The order of elements does not matter.
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o){
        return o.hashCode() == this.hashCode();
        /*
        if(this.getClass() != o.getClass()){
            return false;
        }
        // cast
        BagOfWords other = (BagOfWords) o;

        // bags must be of same size
        if(other.size() != this.size()){
            return false;
        }

        // element comparison
        for(String thisStringElement : this){
            if(!other.contains(thisStringElement)){
                return false;
            }
        }
        return true;
        */
    }


    @Override
    public int hashCode(){
        if(hashCode != -1){
            return hashCode;
        }
        int hashCode = 1;
        for(String element : this){
            hashCode = hashCode + element.hashCode();
        }
        return hashCode;
    }

}
