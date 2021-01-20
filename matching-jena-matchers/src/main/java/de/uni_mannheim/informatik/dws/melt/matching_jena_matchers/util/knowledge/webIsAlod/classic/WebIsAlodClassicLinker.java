package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.webIsAlod.classic;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.nGramTokenizers.LeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.nGramTokenizers.MaxGramLeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.stringOperations.StringOperations;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.webIsAlod.WebIsAlodSPARQLservice;

import java.util.HashSet;

/**
 * This linker can link strings to dictionary entries.
 */
public class WebIsAlodClassicLinker implements LabelToConceptLinker {

    /**
     * Identifying label of this linker.
     */
    private String nameOfLinker = "ALOD Classic Linker";

    /**
     * SPARQL Service instance for (buffered) queries.
     */
    private WebIsAlodSPARQLservice sparqlService = WebIsAlodSPARQLservice.getInstance(WebIsAlodSPARQLservice.WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT);

    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        String result;

        // lookup 1: plain
        result = sparqlService.getUriUsingLabel(cleanLabelForLabelLookup(labelToBeLinked));
        if(result != null) return result;

        // lookup 2: no tokenization
        result = sparqlService.getUriUsingLabel(normalizeForAlodClassicLookupWithoutTokenization(labelToBeLinked));
        if(result != null) return result;

        // lookup 3: no tokenization
        result = sparqlService.getUriUsingLabel(normalizeForAlodClassicLookupWithTokenization(labelToBeLinked));
        return result;
    }

    @Override
    public HashSet<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked) {
        HashSet<String> result = linkLabelToTokensLeftToRight(labelToBeLinked);
        int possibleConceptParts = StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(labelToBeLinked)).length;

        int actualConceptParts = 0;
        for(String s : result) {
            s = unstripUriClassic(s);
            actualConceptParts = actualConceptParts + StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(s)).length;
        }

        // TODO: for now: only 100% results
        if(possibleConceptParts <= actualConceptParts) {
            return result;
        }
        return null;
    }

    /**
     * Splits the labelToBeLinked in ngrams up to infinite size and tries to link components.
     * This corresponds to a MAXGRAM_LEFT_TO_RIGHT_TOKENIZER or NGRAM_LEFT_TO_RIGHT_TOKENIZER OneToManyLinkingStrategy.
     *
     * @param labelToBeLinked The label that shall be linked.
     * @return A set of concept URIs that were found.
     */
    private HashSet<String> linkLabelToTokensLeftToRight(String labelToBeLinked){
        LeftToRightTokenizer tokenizer;
        String[] tokens = StringOperations.tokenizeBestGuess(labelToBeLinked);

        tokenizer = new MaxGramLeftToRightTokenizer(tokens, " ");

        HashSet<String> result = new HashSet<>();
        String resultingConcept = "";
        String token = tokenizer.getInitialToken();
        while(token != null){
            resultingConcept = linkToSingleConcept(token);
            if(resultingConcept == null || resultingConcept.length() == 0){
                token = tokenizer.getNextTokenNotSuccessful();
            } else {
                result.add(resultingConcept);
                token = tokenizer.getNextTokenSuccessful();
            }
        }
        return result;
    }


    @Override
    public String getNameOfLinker() {
        return nameOfLinker;
    }

    @Override
    public void setNameOfLinker(String nameOfLinker) {
        this.nameOfLinker = nameOfLinker;
    }

    /**
     * When looking up a resource on WebIsALOD using a plain label, that label needs to be stripped off from
     * invalid characters. This method does the job. Note that this method is endpoint specific.
     *
     * @param labelToClean That label that is to be cleaned.
     * @return Cleaned label.
     */
    public static String cleanLabelForLabelLookup(String labelToClean) {
        String outputString = labelToClean;
        // irregular character replacement
        outputString = outputString.replace("\"", "");
        outputString = outputString.replace(":", "");
        outputString = outputString.replace("{", "");
        outputString = outputString.replace("}", "");
        outputString = outputString.replace("-", " ");
        outputString = outputString.replace("\\", "\\\\");
        outputString = outputString.replace("\n", " ");
        return outputString;
    }

    /**
     * Normalize for BabelNet lookup, i.e., lowercasing and camel-case resolution.
     * Dev-Remark: The BabelNet library utilizes space-separation.
     * @param lookupString The string that shall be normalized for lookup.
     * @return Space-separated lookup word.
     */
    public static String normalizeForAlodClassicLookupWithTokenization(String lookupString){
        lookupString = lookupString.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", " "); // convert camelCase to under_score_case
        lookupString = lookupString.replace("_", " ");
        lookupString = lookupString.replaceAll("( ){1,}", " "); // make sure there are no double-spaces
        lookupString = lookupString.toLowerCase();
        return cleanLabelForLabelLookup(lookupString);
    }

    /**
     * Normalize for BabelNet lookup, i.e., lowercasing and camel-case resolution.
     * Dev-Remark: The BabelNet library utilizes space-separation.
     * @param lookupString The string that shall be normalized for lookup.
     * @return Space-separated lookup word.
     */
    public static String normalizeForAlodClassicLookupWithoutTokenization(String lookupString){
        lookupString = lookupString.replace("_", " ");
        lookupString = lookupString.replaceAll("( ){1,}", " "); // make sure there are no double-spaces
        lookupString = lookupString.toLowerCase();
        return cleanLabelForLabelLookup(lookupString);
    }


    /**
     * This method will strip the URL part from the URI.
     * @param uri URI that shall be stripped.
     * @return unstripped URI
     */
    public static String unstripUriClassic(String uri){
        return uri.replaceAll("http://webisa.webdatacommons.org/concept/", "");
    }
}
