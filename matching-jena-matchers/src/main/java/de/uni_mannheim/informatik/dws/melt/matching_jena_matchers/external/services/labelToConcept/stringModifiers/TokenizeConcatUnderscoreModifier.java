package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

public class TokenizeConcatUnderscoreModifier implements StringModifier {

    /**
     * Tokenizes using best guess and AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION.
     * Concatenate using lower scores ("_")
     * @param stringToBeModified The String that shall be modified.
     * @return The modified String.
     */
    @Override
    public String modifyString(String stringToBeModified) {
        stringToBeModified = stringToBeModified.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", "_"); // convert camelCase to under_score_case
        stringToBeModified = stringToBeModified.replace(" ", "_");
        stringToBeModified = stringToBeModified.replaceAll("(_){1,}", "_"); // make sure there are no double-spaces
        return stringToBeModified;
    }

    @Override
    public String getName() {
        return "TokenizeConcatUnderscore";
    }
}
