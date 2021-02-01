package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizeConcatSpaceOnlyCapitalizeFirstLetterModifierDropPluralTest {


    @Test
    void modifyString() {
        TokenizeConcatSpaceOnlyCapitalizeFirstLetterModifierDropPlural modifier = new TokenizeConcatSpaceOnlyCapitalizeFirstLetterModifierDropPlural();
        assertEquals("European union", modifier.modifyString("european_unions"));
        assertEquals("European union", modifier.modifyString("European_Unions"));
        assertEquals("European union", modifier.modifyString("european unions"));
        assertEquals("European union", modifier.modifyString("europeanUnions"));
        assertEquals("European union", modifier.modifyString("europeanUnions"));
    }
}