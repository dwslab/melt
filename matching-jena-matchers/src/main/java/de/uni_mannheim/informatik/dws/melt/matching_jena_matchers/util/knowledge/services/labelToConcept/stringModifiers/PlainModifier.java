package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.stringModifiers;

public class PlainModifier implements StringModifier {
    @Override
    public String modifyString(String stringToBeModified) {
        return stringToBeModified;
    }

    @Override
    public String getName() {
        return "Plain";
    }
}
