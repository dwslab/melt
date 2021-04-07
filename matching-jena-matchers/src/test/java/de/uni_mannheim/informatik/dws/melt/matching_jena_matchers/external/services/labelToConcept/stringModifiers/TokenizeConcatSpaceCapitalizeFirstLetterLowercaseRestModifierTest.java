package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizeConcatSpaceCapitalizeFirstLetterLowercaseRestModifierTest {


    @Test
    void modifyString() {
        TokenizeConcatSpaceCapitalizeFirstLetterLowercaseRestModifier modifier = new TokenizeConcatSpaceCapitalizeFirstLetterLowercaseRestModifier();
        assertEquals("European union", modifier.modifyString("european_union"));
        assertEquals("European union", modifier.modifyString("European_Union"));
        assertEquals("European union", modifier.modifyString("european union"));
        assertEquals("European union", modifier.modifyString("europeanUnion"));
        assertEquals("European union", modifier.modifyString("europeanUnion"));
    }

    @Test
    void getName(){
        TokenizeConcatSpaceCapitalizeFirstLetterLowercaseRestModifier modifier = new TokenizeConcatSpaceCapitalizeFirstLetterLowercaseRestModifier();
        assertNotNull(modifier.getName());
    }
}