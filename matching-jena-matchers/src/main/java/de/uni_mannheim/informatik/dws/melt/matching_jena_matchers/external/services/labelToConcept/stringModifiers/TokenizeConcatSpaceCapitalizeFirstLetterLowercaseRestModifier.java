package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

public class TokenizeConcatSpaceCapitalizeFirstLetterLowercaseRestModifier implements StringModifier {


    /**
     * Constructor
     */
    public TokenizeConcatSpaceCapitalizeFirstLetterLowercaseRestModifier(){
        tokenizeModifier = new TokenizeConcatSpaceModifier();
    }

    private TokenizeConcatSpaceModifier tokenizeModifier;

    @Override
    public String modifyString(String stringToBeModified) {
        String result = tokenizeModifier.modifyString(tokenizeModifier.modifyString(stringToBeModified)).toLowerCase();
        char upperCased = Character.toUpperCase(result.toCharArray()[0]);
        return upperCased + result.substring(1);
    }

    @Override
    public String getName() {
        return "TokenizeConcatSpaceOnlyCapitalizeFirstLetterModifier";
    }

}
