package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;

/**
 * This modifier tokenizes the String to be modified and separates the tokens with spaces. 
 * In addition, the final label is transformed into lower case. 
 *
 */
public class TokenizeSpaceSeparateLowercaseModifier implements StringModifier{

    @Override
    public String modifyString(String stringToBeModified) {
        String splitResource[] = StringOperations.tokenizeBestGuess(stringToBeModified, StringOperations.AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION);
        String splitResourceConcatenatedName = "";
        for (String token : splitResource) {
            splitResourceConcatenatedName = splitResourceConcatenatedName + " " + token.toLowerCase();
        }
        splitResourceConcatenatedName = splitResourceConcatenatedName.trim();
        return splitResourceConcatenatedName;
    }

    @Override
    public String getName() {
        return "TokenizeSpaceSeparateLowercase";
    }
}
