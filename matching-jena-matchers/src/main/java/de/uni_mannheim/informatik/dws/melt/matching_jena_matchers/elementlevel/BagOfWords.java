package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel;

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
     * @param tokensToBeAdded Tokens that shall be added to the BOW.
     */
    public BagOfWords(String[] tokensToBeAdded){
        for(String token : tokensToBeAdded){
            this.add(token);
        }
    }

    /**
     * Two BagOfWords qualify as the same object if they contain the same objects.
     * The order of elements does not matter.
     * @param o Object for comparison.
     * @return True if equals, else false.
     */
    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(o.getClass() != this.getClass()) return false;
        return o.hashCode() == this.hashCode();
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

    @Override
    public String toString(){
        String result = "|";
        for(String s : this){
            result = result + s + "|";
        }
        return result;
    }
}
