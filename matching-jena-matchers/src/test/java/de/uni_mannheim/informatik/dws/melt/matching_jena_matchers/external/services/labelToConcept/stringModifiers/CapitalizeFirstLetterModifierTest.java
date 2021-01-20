package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CapitalizeFirstLetterModifierTest {

    @Test
    void modifyString() {
        CapitalizeFirstLetterModifier modifier = new CapitalizeFirstLetterModifier();
        assertEquals("HelloWorld", modifier.modifyString("helloWorld"));
    }
}