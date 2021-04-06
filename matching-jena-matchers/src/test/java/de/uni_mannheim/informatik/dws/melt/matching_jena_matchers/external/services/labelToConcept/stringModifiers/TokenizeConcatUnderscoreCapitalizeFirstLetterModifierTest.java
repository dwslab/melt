package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizeConcatUnderscoreCapitalizeFirstLetterModifierTest {


    @Test
    void modifyString() {
        TokenizeConcatUnderscoreCapitalizeFirstLetterModifier modifier =
                new TokenizeConcatUnderscoreCapitalizeFirstLetterModifier();
        assertEquals("European_union", modifier.modifyString("european_union"));
        assertEquals("European_Union", modifier.modifyString("European_Union"));
        assertEquals("European_union", modifier.modifyString("european union"));
        assertEquals("European_Union", modifier.modifyString("europeanUnion"));
        assertEquals("European_Union", modifier.modifyString("europeanUnion"));
    }

    @Test
    void getName() {
        TokenizeConcatUnderscoreCapitalizeFirstLetterModifier m =
                new TokenizeConcatUnderscoreCapitalizeFirstLetterModifier();
        assertNotNull(m.getName());
    }
}