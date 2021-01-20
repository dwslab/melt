package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

public class TokenizeConcatSpaceCapitalizeModifier implements StringModifier {

    /**
     * Constructor
     */
    public TokenizeConcatSpaceCapitalizeModifier(){
        tokenizeModifier = new TokenizeConcatSpaceModifier();
        capitalizeModifier = new CapitalizeFirstLettersModifier(" ");
    }

    private TokenizeConcatSpaceModifier tokenizeModifier;
    private CapitalizeFirstLettersModifier capitalizeModifier;


    @Override
    public String modifyString(String stringToBeModified) {
        return capitalizeModifier.modifyString(tokenizeModifier.modifyString(stringToBeModified));
    }

    @Override
    public String getName() {
        return "TokenizeConcatUnderscoreCapitalizeModifier";
    }
}
