package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizeConcatLowerscoreUppercaseAfterScoreModifierTest {

    @Test
    void modifyString() {
        TokenizeConcatLowerscoreUppercaseAfterScoreModifier modifier = new TokenizeConcatLowerscoreUppercaseAfterScoreModifier();
        modifier.modifyString("HelloWorld");
        assertEquals(modifier.modifyString("HelloWorld"), "Hello_World");
    }
}