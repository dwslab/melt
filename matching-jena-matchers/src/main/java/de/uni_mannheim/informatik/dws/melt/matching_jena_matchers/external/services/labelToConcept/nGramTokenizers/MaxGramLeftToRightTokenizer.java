package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * This tokenizer is able to assist in linking labels that consist of multiple concepts to the most specific concept
 * possible.
 *
 * To understand the full capability of the program you can also have a look at the extensive unit test.
 *
 */
public class MaxGramLeftToRightTokenizer implements LeftToRightTokenizer, OneToManyLinkingStrategy {


    private static final Logger LOGGER = LoggerFactory.getLogger(MaxGramLeftToRightTokenizer.class);

    private String[] arrayToLink;
    private int endIndexExclusive;
    private int startIndex = 0;
    private boolean terminated;

    /**
     * Used to concatenate single tokens. The classic Classic data set, for instance, requires a space between
     * labels which are made up of more than one word.
     */
    private String delimiter;

    /**
     * List of terms that could not be linked.
     */
    private ArrayList<String> notLinked;

    /**
     * Constructor
     * @param arrayToLink The array that shall be linked
     * @param delimiter The delimiter.
     */
    public MaxGramLeftToRightTokenizer(String[] arrayToLink, String delimiter){
        setDelimiter(delimiter); // must be set because another class uses it even though arrayToLink might be null
        notLinked = new ArrayList<>();
        if(arrayToLink != null) {
            setArrayToLink(arrayToLink);
            endIndexExclusive = arrayToLink.length;
        } else {
            terminated = true;
        }
    }

    /**
     * Get a new token based on the information that the last string tested was not successful.
     * @return String representation for the next test.
     */
    public String getNextTokenNotSuccessful(){
        if(isTerminated()){
            return null;
        }

        if(endIndexExclusive == startIndex + 1) {
            //LOG.info("Single Term not found: " + arrayToLink[startIndex]);
            notLinked.add(arrayToLink[startIndex]);
            startIndex++;
            endIndexExclusive = arrayToLink.length;
            if(startIndex == arrayToLink.length){
                terminated = true;
                return null;
            }
            return processArrayForLookup(arrayToLink, startIndex, endIndexExclusive);
        } else{
            endIndexExclusive--;
            return processArrayForLookup(arrayToLink, startIndex, endIndexExclusive);
        }
    }

    /**
     * Get a new token based on the information that the last string tested was successful.
     * @return String representation for next trial.
     */
    public String getNextTokenSuccessful(){
        if(isTerminated()){
            return null;
        }

        // check whether other termination criteria fulfilled
        if(
            // case 1: start index approached end of string
                (startIndex == arrayToLink.length -1)
                        ||
                        // case 2: initial string was successful
                        (endIndexExclusive == arrayToLink.length)
                ){
            terminated = true;
            return null;
        }
        startIndex = endIndexExclusive;
        endIndexExclusive = arrayToLink.length;

        if(startIndex == arrayToLink.length -1){
            terminated = true;
        }
        return processArrayForLookup(arrayToLink, startIndex, endIndexExclusive);
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
        return processArrayForLookup(arrayToLink,0, arrayToLink.length);
    }

    /**
     * Cuts the given array as specified and concatenates the components as defined by the delimiter.
     * @param arrayToConvert The array to be cut.
     * @param start Start index of cut.
     * @param end End index of cut.
     * @return Single String of space-separated components.
     */
    String processArrayForLookup(String[] arrayToConvert, int start, int end){
        String result = "";
        String[] resultArray = Arrays.copyOfRange(arrayToConvert, start, Math.min(end, arrayToConvert.length));
        for (String s : resultArray) {
            result = result + s + delimiter;
        }
        try {
            // removing last delimiter
            return result.substring(0, result.length() - delimiter.length());
        } catch (NullPointerException npe){
            StringBuilder builder = new StringBuilder();
            builder.append("Result\n")
                    .append(result)
                    .append("\n")
                    .append("Delimiter\n")
                    .append(delimiter)
                    .append("\n")
                    .append("Array to convert:\n");
            Arrays.stream(arrayToConvert).forEach(x -> builder.append(x));
            LOGGER.error(builder.toString());
            return result.substring(0, result.length() - delimiter.length());
        }
    }

    //------------------------------
    // Only getters and setters
    //------------------------------

    /**
     * Get the token sequence that is to be linked.
     * @return Token sequence that is to be linked.
     */
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

    public ArrayList<String> getNotLinked() {
        return notLinked;
    }
}
