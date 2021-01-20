package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers;

import java.util.HashSet;

/**
 * Creates regular n-grams
 */
public class NgramTokenizer implements OneToManyLinkingStrategy {

    private String delimiter;
    private int nGramsize;


    public NgramTokenizer(int nGramsize, String delimiter){
        setnGramsize(nGramsize);
        setDelimiter(delimiter);
    }


    /**
     * Creates ngrams out of the given array {@code tokens}.
     * @param tokens Array for which ngrams shall be created.
     * @return ngrams in a set.
     */
    public HashSet<String> getNgrams(String[] tokens){

        // avoiding NullPointerExceptions here:
        if(tokens == null){
            return null;
        }

        HashSet<String> result = new HashSet<>();

        // if the ngram size is larger than the actual data:
        if(tokens.length <= nGramsize){
            String resultString = "";
            for(String s : tokens){
                resultString = resultString + s + delimiter;
            }
            result.add(resultString.substring(0, resultString.length() - delimiter.length()));
            return result;
        }

        // standard use case:
        String ngram = "";
        for(int i = 0; i + nGramsize - 1 < tokens.length; i++){
            ngram = "";
            for(int j = 0; j < nGramsize; j++){
                ngram = ngram + tokens[i+j] + delimiter;
            }
            result.add(ngram.substring(0, ngram.length() - delimiter.length()));
        }
        return result;
    }


    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public int getnGramsize() {
        return nGramsize;
    }

    public void setnGramsize(int nGramsize) {
        this.nGramsize = Math.max(nGramsize, 1);
    }

}
