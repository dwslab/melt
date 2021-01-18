package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizeConcatSpaceLowercaseModifierTest {

    @Test
    void modifyString() {
        TokenizeConcatSpaceLowercaseModifier modifier = new TokenizeConcatSpaceLowercaseModifier();
        assertEquals("european union", modifier.modifyString("European_Union"));
        assertEquals("european union", modifier.modifyString("European Union"));
        assertEquals("european union", modifier.modifyString("EuropeanUnion"));
        assertEquals("eu", modifier.modifyString("EU"));
        assertEquals("european union", modifier.modifyString("europeanUnion"));
    }
}