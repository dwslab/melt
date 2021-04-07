package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

public class TokenizeConcatUnderscoreCapitalizeFirstLetterModifier implements StringModifier {


    /**
     * Constructor
     */
    public TokenizeConcatUnderscoreCapitalizeFirstLetterModifier(){
        tokenizeModifier = new TokenizeConcatUnderscoreModifier();
    }

    private TokenizeConcatUnderscoreModifier tokenizeModifier;

    @Override
    public String modifyString(String stringToBeModified) {
        String result = tokenizeModifier.modifyString(stringToBeModified);
        char upperCased = Character.toUpperCase(result.toCharArray()[0]);
        return upperCased + result.substring(1);
    }

    @Override
    public String getName() {
        return "TokenizeConcatUnderscoreCapitalizeFirstLetterModifier";
    }
}
