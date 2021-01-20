package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

public class TokenizeConcatUnderscoreLowercaseModifier implements StringModifier {

    /**
     * Constructor
     */
    public TokenizeConcatUnderscoreLowercaseModifier(){
        tokenizeModifier = new TokenizeConcatUnderscoreModifier();
    }

    private TokenizeConcatUnderscoreModifier tokenizeModifier;

    @Override
    public String modifyString(String stringToBeModified) {
        return tokenizeModifier.modifyString(tokenizeModifier.modifyString(stringToBeModified)).toLowerCase();
    }

    @Override
    public String getName() {
        return "TokenizeConcatUnderscoreCapitalizeModifier";
    }

}
