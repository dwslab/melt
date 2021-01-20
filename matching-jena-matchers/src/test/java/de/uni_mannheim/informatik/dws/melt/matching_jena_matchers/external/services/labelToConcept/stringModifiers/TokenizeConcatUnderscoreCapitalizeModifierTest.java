package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizeConcatUnderscoreCapitalizeModifierTest {

    @Test
    void modifyString() {
        TokenizeConcatUnderscoreCapitalizeModifier modifier = new TokenizeConcatUnderscoreCapitalizeModifier();
        assertEquals("European_Union", modifier.modifyString("European_Union"));
        assertEquals("European_Union", modifier.modifyString("European Union"));
        assertEquals("European_Union", modifier.modifyString("EuropeanUnion"));
        assertEquals("EU", modifier.modifyString("EU"));
        assertEquals("European_Union", modifier.modifyString("europeanUnion"));
    }
}