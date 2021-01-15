package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.stringModifiers;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.stringOperations.StringOperations;

/**
 * This StringModifier removes all characters that are not a letter and then applies the TokenizeSpaceSeparateLowercaseModifier.
 * @see TokenizeSpaceSeparateLowercaseModifier
 *
 */
public class CharactersOnlyTokenizeSpaceSeparateLowercaseModifier implements StringModifier {
    @Override
    public String modifyString(String stringToBeModified) {

        String reducedString = StringOperations.reduceToLettersOnly(new TokenizeSpaceSeparateLowercaseModifier().modifyString(stringToBeModified));
        return reducedString;
    }

    @Override
    public String getName() {
        return "CharactersOnlyTokenizeSpaceSeparateLowercaseModifier";
    }
}
