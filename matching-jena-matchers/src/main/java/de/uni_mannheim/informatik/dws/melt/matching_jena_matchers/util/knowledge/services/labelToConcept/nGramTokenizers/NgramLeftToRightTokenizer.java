package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.nGramTokenizers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * DEV REMARK: Be aware that refactoring the name leads to hardcoded String changes in the LabelToConcept Linker package.
 */
public class NgramLeftToRightTokenizer implements LeftToRightTokenizer, OneToManyLinkingStrategy  {

    private static Logger LOG = LoggerFactory.getLogger(NgramLeftToRightTokenizer.class);
    private String[] arrayToLink;
    private int endIndexExclusive;
    private int startIndex = 0;
    private boolean terminated;
    private int cutNgramPosition; // indicates whether the current nGram was cut currently because too long
    private int nGramsize;
    private String delimiter;

    /**
     * Constructor
     * @param arrayToLink The array that shall be linked.
     * @param delimiter Delimiter
     * @param nGramsize n-gram size
     */
    public NgramLeftToRightTokenizer(String[] arrayToLink, String delimiter, int nGramsize){
        setArrayToLink(arrayToLink);
        setDelimiter(delimiter);
        setnGramsize(nGramsize);
        endIndexExclusive = nGramsize;
        cutNgramPosition = nGramsize;
    }


    @Override
    public String getNextTokenNotSuccessful() {
        if(isTerminated()){
            return null;
        }

        // shorten current ngram if not found
        if(cutNgramPosition != 1){

            if(startIndex == arrayToLink.length -1){
                // last token not found
                LOG.info("Term " + arrayToLink[startIndex] +" not found.");
                terminated = true;
                return null;
            }

            cutNgramPosition = Math.min(arrayToLink.length - startIndex - 1,cutNgramPosition -1);
            endIndexExclusive--;

            return processArrayForLookup(arrayToLink,startIndex,endIndexExclusive);
        } else if (cutNgramPosition == 1){
           LOG.info("Term " + arrayToLink[startIndex] +" not found.");
        }

        // move starting point and reset ngram window
        startIndex++;
        endIndexExclusive = Math.min(startIndex + nGramsize, arrayToLink.length);
        cutNgramPosition = nGramsize;

        // special case: last token
        if(startIndex == arrayToLink.length -1){
            terminated = true;
        }

        return processArrayForLookup(arrayToLink,startIndex,endIndexExclusive);


        //return null;
    }

    @Override
    public String getNextTokenSuccessful() {
        if(isTerminated()){
            return null;
        }

        // check whether termination criterion fulfilled
        if(startIndex == arrayToLink.length - 1){
            terminated = true;
            return null;
        }

        startIndex = endIndexExclusive;
        endIndexExclusive = Math.min(startIndex + nGramsize, arrayToLink.length);
        cutNgramPosition = nGramsize;

        if(startIndex >= arrayToLink.length){
            terminated = true;
            return null;
        }

        return processArrayForLookup(arrayToLink,startIndex,endIndexExclusive);

    }

    /**
     * Getting the very first string formation.
     * This method can only be called as long as the process is not terminated.
     * @return String representation for next test.
     */
    public String getInitialToken(){
        if(isTerminated()){
            return null;
        }
        cutNgramPosition = nGramsize;
        return processArrayForLookup(arrayToLink,0, Math.min(nGramsize,arrayToLink.length));
    }


    /**
     * Cuts the given array as specified and concatenates the components in a space-separated way.
     * @param arrayToConvert The array to be cut.
     * @param start Start index of cut.
     * @param end End index of cut.
     * @return Single String of space-separated components.
     */
    String processArrayForLookup(String[] arrayToConvert, int start, int end){
    	if(arrayToConvert[0].equals("geniculate")) {
    		System.out.println("HERER");
    	}
        String result = "";
        String[] resultArray = Arrays.copyOfRange(arrayToConvert, start, Math.min(end,arrayToConvert.length));
        for(String s : resultArray){
            result = result + s + delimiter;
        }
        return result.substring(0, result.length()-delimiter.length());
    }



    //------------------------------
    // Only getters and setters
    //------------------------------

    public String[] getArrayToLink() {
        return arrayToLink;
    }

    public void setArrayToLink(String[] arrayToLink) {
        this.arrayToLink = arrayToLink;
    }

    public boolean isTerminated(){
        return this.terminated;
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
