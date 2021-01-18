package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.stringModifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CapitalizeFirstLettersModifierTest {

    @Test
    void modifyString() {
        CapitalizeFirstLettersModifier modifier = new CapitalizeFirstLettersModifier("_");
        assertEquals("European_Union", modifier.modifyString("european_union"));

        modifier = new CapitalizeFirstLettersModifier(" ");
        assertEquals("European Union", modifier.modifyString("european union"));
    }
}