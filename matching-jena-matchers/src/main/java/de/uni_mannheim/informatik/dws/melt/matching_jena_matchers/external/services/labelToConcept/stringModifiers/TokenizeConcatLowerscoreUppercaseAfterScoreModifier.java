package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import org.apache.commons.text.WordUtils;

public class TokenizeConcatLowerscoreUppercaseAfterScoreModifier implements StringModifier {

        /**
         * Tokenizes using best guess and AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION.
         * Concatenate using lower scores ("_")
         * @param stringToBeModified The String that shall be modified.
         * @return The modified String.
         */
        @Override
        public String modifyString(String stringToBeModified) {
            String splitResource[] = StringOperations.tokenizeBestGuess(stringToBeModified, StringOperations.AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION);
            String splitResourceConcatenatedName = "";

            if(splitResource.length > 1) {
                for (int i = 0; i < splitResource.length; i++) {
                    if (i > 0) {
                        splitResourceConcatenatedName = WordUtils.capitalize(splitResourceConcatenatedName) + "_" + WordUtils.capitalize(splitResource[i]);
                    } else {
                        splitResourceConcatenatedName = WordUtils.capitalize(splitResource[i]);
                    }
                }
            } else {
                // nothing to concatenate, return string as it is
                return stringToBeModified;
            }
            return splitResourceConcatenatedName;
        }

    @Override
    public String getName() {
        return "TokenizeConcatLowerscoreUppercaseAfterScore";
    }
}
