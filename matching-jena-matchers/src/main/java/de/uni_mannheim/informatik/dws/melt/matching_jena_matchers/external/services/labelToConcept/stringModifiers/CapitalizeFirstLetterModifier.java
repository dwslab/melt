package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import org.apache.commons.text.WordUtils;

/**
 *
 * Example:
 * Input: "hello"
 * Output: "Hello"
 *
 */
public class CapitalizeFirstLetterModifier implements StringModifier{
    @Override
    public String modifyString(String stringToBeModified) {
        return WordUtils.capitalize(stringToBeModified);
    }

    @Override
    public String getName() {
        return "CapitalizeFirstLetter";
    }
}
