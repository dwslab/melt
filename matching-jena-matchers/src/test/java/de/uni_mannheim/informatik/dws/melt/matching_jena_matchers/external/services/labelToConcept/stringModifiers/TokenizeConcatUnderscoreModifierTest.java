package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizeConcatUnderscoreModifierTest {


    @Test
    void modifyString() {
        TokenizeConcatUnderscoreModifier modifier = new TokenizeConcatUnderscoreModifier();
        assertEquals("Hello_World", modifier.modifyString("HelloWorld"));
        assertEquals("Hello_World", modifier.modifyString("Hello_World"));
        assertEquals("Hello_World", modifier.modifyString("Hello World"));
    }

    @Test
    void getName(){
        TokenizeConcatUnderscoreModifier modifier = new TokenizeConcatUnderscoreModifier();
        assertNotNull(modifier.getName());
    }
}