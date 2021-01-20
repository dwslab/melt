package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizeConcatUnderscoreModifierOldTest {

    @Test
    void modifyString() {
        TokenizeConcatUnderscoreModifierOld modifier = new TokenizeConcatUnderscoreModifierOld();
        assertEquals("Hello_World", modifier.modifyString("HelloWorld"));
    }
}