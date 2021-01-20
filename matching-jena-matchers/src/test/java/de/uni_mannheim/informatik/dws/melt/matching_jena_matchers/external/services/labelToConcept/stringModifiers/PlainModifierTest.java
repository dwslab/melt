package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlainModifierTest {

    @Test
    public void modifyString(){
        PlainModifier modifier = new PlainModifier();
        String s = "Hello World!";
        assertEquals(s, modifier.modifyString(s));
    }
}