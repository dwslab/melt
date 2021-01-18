package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenizeConcatSpaceCapitalizeModifierTest {

    @Test
    void modifyString() {
        TokenizeConcatSpaceCapitalizeModifier modifier = new TokenizeConcatSpaceCapitalizeModifier();
        assertEquals("European Union", modifier.modifyString("European_Union"));
        assertEquals("European Union", modifier.modifyString("European Union"));
        assertEquals("European Union", modifier.modifyString("EuropeanUnion"));
        assertEquals("EU", modifier.modifyString("EU"));
        assertEquals("European Union", modifier.modifyString("europeanUnion"));
    }
}