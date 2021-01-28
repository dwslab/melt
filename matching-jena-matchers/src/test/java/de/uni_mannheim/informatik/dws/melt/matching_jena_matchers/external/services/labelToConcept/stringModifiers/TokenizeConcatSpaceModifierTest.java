package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizeConcatSpaceModifierTest {


    @Test
    void modifyString() {
        TokenizeConcatSpaceModifier modifier = new TokenizeConcatSpaceModifier();
        assertEquals("european union", modifier.modifyString("european_union"));
        assertEquals("European Union", modifier.modifyString("European_Union"));
        assertEquals("european union", modifier.modifyString("european union"));
        assertEquals("european Union", modifier.modifyString("europeanUnion"));
        assertEquals("EU", modifier.modifyString("EU"));
        assertEquals("european Union", modifier.modifyString("europeanUnion"));
    }

}