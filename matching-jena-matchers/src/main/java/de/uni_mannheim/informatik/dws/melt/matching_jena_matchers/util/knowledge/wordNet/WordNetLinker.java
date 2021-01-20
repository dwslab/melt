package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.wordNet;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.nGramTokenizers.LeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.nGramTokenizers.MaxGramLeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.stringOperations.StringOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.HashSet;

/**
 * This class is capable of linking words to concepts in WordNet.
 *
 * @author D060249
 */
public class WordNetLinker implements LabelToConceptLinker {

	private static Logger LOGGER = LoggerFactory.getLogger(WordNetLinker.class);

    public static void main(String[] args) {
        WordNetKnowledgeSource dictionary = new WordNetKnowledgeSource("/Users/janportisch/Documents/Data/Wordnet/dict");
        WordNetLinker linker = new WordNetLinker(dictionary);
        for (String s : linker.linkToPotentiallyMultipleConcepts("council of the european union")) {
            System.out.println(s);
        }
        dictionary.close();
    }


    /**
     * The WordNet dictionary instance that is to be used.
     */
    private WordNetKnowledgeSource dictionary;

    private String nameOfLinker = "WordNet Linker";

    /**
     * Mapping Buffer
     */
    private HashMap<String, String> singleConceptBuffer;

    public WordNetLinker(WordNetKnowledgeSource dictionary) {
        this.dictionary = dictionary;
        singleConceptBuffer = new HashMap<>();
    }

    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        String key = labelToBeLinked;
        if (singleConceptBuffer.containsKey(key)) {
            return singleConceptBuffer.get(key);
        }

        // try simple approach
        String lowerCaseLabel = normalizeForWordnetLookupWithOutTokenization(labelToBeLinked);

        if(lowerCaseLabel.equals("")) return null;

        try {
			if (dictionary.isInDictionary(lowerCaseLabel)) {
				singleConceptBuffer.put(key, lowerCaseLabel);
				return lowerCaseLabel;
			}
		} catch (IllegalArgumentException iae){
			LOGGER.error("IllegalArgumentException while linking label '" + lowerCaseLabel + "'", iae);
		}


        labelToBeLinked = normalizeForWordnetLookupWithTokenization(labelToBeLinked);
        if (dictionary.isInDictionary(labelToBeLinked)) {
            singleConceptBuffer.put(key, labelToBeLinked);
            return labelToBeLinked;
        }
        singleConceptBuffer.put(key, null);
        return null;
    }

    @Override
    public HashSet<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked) {
        if (labelToBeLinked == null) return null;
        HashSet<String> result = linkLabelToTokensLeftToRight(labelToBeLinked);
        int possibleConceptParts = StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(labelToBeLinked)).length;

        int actualConceptParts = 0;
        for (String s : result) {
            actualConceptParts = actualConceptParts + StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(s)).length;
        }

        // TODO: for now: only 100% results
        if (possibleConceptParts <= actualConceptParts) {
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
    private HashSet<String> linkLabelToTokensLeftToRight(String labelToBeLinked) {
        StringOperations.removeNonAlphanumericCharacters(StringOperations.removeEnglishGenitiveS(labelToBeLinked));
        LeftToRightTokenizer tokenizer;
        String[] tokens = StringOperations.tokenizeBestGuess(labelToBeLinked);
        tokenizer = new MaxGramLeftToRightTokenizer(tokens, "_");
        HashSet<String> result = new HashSet<>();
        String resultingConcept = "";
        String token = tokenizer.getInitialToken();
        while (token != null) {
            resultingConcept = linkToSingleConcept(token);
            if (resultingConcept == null || resultingConcept.length() == 0) {
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
        return this.nameOfLinker;
    }

    @Override
    public void setNameOfLinker(String nameOfLinker) {
        this.nameOfLinker = nameOfLinker;
    }

    /**
     * Normalize for WordNet lookup, i.e., lowercasing and camel-case resolution.
     * Dev-Remark: The WordNet library utilizes space-separation.
     *
     * @param lookupString The string that shall be normalized for lookup.
     * @return Space-separated lookup word.
     */
    public static String normalizeForWordnetLookupWithTokenization(String lookupString) {
        lookupString = lookupString.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", " "); // convert camelCase to under_score_case
        lookupString = lookupString.replace("_", " ");
        lookupString = lookupString.replaceAll("( ){1,}", " "); // make sure there are no double-spaces
        lookupString = lookupString.toLowerCase();
        return lookupString;
    }

    /**
     * Normalize for WordNet lookup, i.e., lowercasing. No camelCase resolution.
     * Dev-Remark: The WordNet library utilizes space-separation.
     *
     * @param lookupString The string that shall be normalized for lookup.
     * @return Space-separated lookup word.
     */
    public static String normalizeForWordnetLookupWithOutTokenization(String lookupString) {
        lookupString = lookupString.replace("_", " ");
        lookupString = lookupString.replaceAll("( ){1,}", " "); // make sure there are no double-spaces
        lookupString = lookupString.toLowerCase();
        lookupString = lookupString.trim();
        return lookupString;
    }

}