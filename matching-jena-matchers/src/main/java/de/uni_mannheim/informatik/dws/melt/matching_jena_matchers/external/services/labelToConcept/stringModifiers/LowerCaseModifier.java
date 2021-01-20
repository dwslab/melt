package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

/**
 * Transforms to lower case.
 */
public class LowerCaseModifier implements StringModifier{
    @Override
    public String modifyString(String stringToBeModified) {
        return stringToBeModified.toLowerCase();
    }

    @Override
    public String getName() {
        return "LowerCase";
    }
}
