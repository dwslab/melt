package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;

public class TokenizeConcatSpaceOnlyCapitalizeFirstLetterModifierDropPlural implements StringModifier {


    /**
     * Constructor
     */
    public TokenizeConcatSpaceOnlyCapitalizeFirstLetterModifierDropPlural(){
        tokenizeModifier = new TokenizeConcatSpaceCapitalizeFirstLetterLowercaseRestModifier();
    }

    private TokenizeConcatSpaceCapitalizeFirstLetterLowercaseRestModifier tokenizeModifier;

    @Override
    public String modifyString(String stringToBeModified) {
        return StringOperations.removeEnglishPlural(tokenizeModifier.modifyString(tokenizeModifier.modifyString(stringToBeModified)));

    }

    @Override
    public String getName() {
        return "TokenizeConcatSpaceOnlyCapitalizeFirstLetterModifier";
    }

}
