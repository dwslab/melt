package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers;

/**
 * A left to right tokenizer runs over the array from left to right.
 * Tokens are formed in a way so that there is no overlap in n-grams (as opposed to regular n-gram tokenizers).
 * A feedback channel is established to tell the instance whether a token was successfully found.
 */
public interface LeftToRightTokenizer {
    String getNextTokenNotSuccessful();
    String getNextTokenSuccessful();
    String getInitialToken();
}
