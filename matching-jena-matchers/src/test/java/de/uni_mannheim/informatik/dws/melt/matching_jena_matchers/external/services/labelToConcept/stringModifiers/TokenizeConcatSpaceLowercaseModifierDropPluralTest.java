package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizeConcatSpaceLowercaseModifierDropPluralTest {


    @Test
    void modifyString() {
        TokenizeConcatSpaceLowercaseModifierDropPlural modifier = new TokenizeConcatSpaceLowercaseModifierDropPlural();
        assertEquals("european union", modifier.modifyString("European_Unions"));
        assertEquals("european union", modifier.modifyString("European Unions"));
        assertEquals("european union", modifier.modifyString("EuropeanUnion"));
        assertEquals("eu", modifier.modifyString("EU"));
        assertEquals("european union", modifier.modifyString("europeanUnions"));
        assertEquals("option", modifier.modifyString("Options"));
        assertEquals("security", modifier.modifyString("Securities"));
    }
}