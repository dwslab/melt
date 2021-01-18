package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizeConcatUnderscoreLowercaseModifierTest {

    @Test
    void modifyString() {
        TokenizeConcatUnderscoreLowercaseModifier modifier = new TokenizeConcatUnderscoreLowercaseModifier();
        assertEquals("european_union", modifier.modifyString("European_Union"));
        assertEquals("european_union", modifier.modifyString("European Union"));
        assertEquals("european_union", modifier.modifyString("EuropeanUnion"));
        assertEquals("eu", modifier.modifyString("EU"));
        assertEquals("european_union", modifier.modifyString("europeanUnion"));
    }
}