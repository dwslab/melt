package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

public class TokenizeConcatSpaceModifier implements StringModifier {

    /**
     * Tokenizes using best guess and AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION.
     * Concatenate using lower scores ("_")
     * @param stringToBeModified The String that shall be modified.
     * @return Modified String.
     */
    @Override
    public String modifyString(String stringToBeModified) {
        stringToBeModified = stringToBeModified.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", " "); // convert camelCase to under_score_case
        stringToBeModified = stringToBeModified.replace("_", " "); // replace _
        stringToBeModified = stringToBeModified.replaceAll("( ){1,}", " "); // make sure there are no double-spaces
        return stringToBeModified;
    }

    @Override
    public String getName() {
        return "TokenizeConcatSpace";
    }
}
