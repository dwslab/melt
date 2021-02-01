package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;

public class TokenizeConcatSpaceLowercaseModifierDropPlural implements StringModifier {


    /**
     * Constructor
     */
    public TokenizeConcatSpaceLowercaseModifierDropPlural(){
        previousModifier = new TokenizeConcatSpaceLowercaseModifier();
    }

    private final TokenizeConcatSpaceLowercaseModifier previousModifier;

    @Override
    public String modifyString(String stringToBeModified) {
        return StringOperations.removeEnglishPlural(previousModifier.modifyString(previousModifier.modifyString(stringToBeModified)).toLowerCase());
    }

    @Override
    public String getName() {
        return "TokenizeConcatUnderscoreCapitalizeModifier";
    }

}
