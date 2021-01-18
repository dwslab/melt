package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizeConcatUnderscoreModifierTest {

    @Test
    void modifyString() {
        TokenizeConcatUnderscoreModifier modifier = new TokenizeConcatUnderscoreModifier();
        assertEquals("Hello_World", modifier.modifyString("HelloWorld"));
    }
}