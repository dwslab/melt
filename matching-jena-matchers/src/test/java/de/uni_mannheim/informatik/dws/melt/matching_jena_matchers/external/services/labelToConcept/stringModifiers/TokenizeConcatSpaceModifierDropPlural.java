package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenizeConcatSpaceModifierDropPluralTest {


    @Test
    void modifyString() {
        TokenizeConcatSpaceModifierDropPlural modifier = new TokenizeConcatSpaceModifierDropPlural();
        assertEquals("european union", modifier.modifyString("european_unions"));
        assertEquals("European Union", modifier.modifyString("European_Unions"));
        assertEquals("european union", modifier.modifyString("european unions"));
        assertEquals("european Union", modifier.modifyString("europeanUnions"));
        assertEquals("EU", modifier.modifyString("EU"));
        assertEquals("european Union", modifier.modifyString("europeanUnions"));
        assertEquals("european Union", modifier.modifyString("europeanUnions"));
        assertEquals("Option", modifier.modifyString("Options"));
        assertEquals("Security", modifier.modifyString("Securities"));
        assertEquals("Contingent convertible bond", modifier.modifyString("Contingent convertible bonds"));
    }

}