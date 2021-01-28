package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

public class TokenizeConcatSpaceLowercaseModifier implements StringModifier {


    /**
     * Constructor
     */
    public TokenizeConcatSpaceLowercaseModifier(){
        tokenizeModifier = new TokenizeConcatSpaceModifier();
    }

    private TokenizeConcatSpaceModifier tokenizeModifier;

    @Override
    public String modifyString(String stringToBeModified) {
        return tokenizeModifier.modifyString(tokenizeModifier.modifyString(stringToBeModified)).toLowerCase();
    }

    @Override
    public String getName() {
        return "TokenizeConcatUnderscoreCapitalizeModifier";
    }

}
