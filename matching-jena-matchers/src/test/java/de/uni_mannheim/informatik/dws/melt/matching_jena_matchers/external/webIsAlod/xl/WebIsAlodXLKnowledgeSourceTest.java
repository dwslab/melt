package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.xl;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class WebIsAlodXLKnowledgeSourceTest {

    @Test
    void getLinker(){
        WebIsAlodXLKnowledgeSource xl = new WebIsAlodXLKnowledgeSource();
        assertNotNull(xl.getLinker());
    }

    @Test
    void isHypernym(){
        WebIsAlodXLKnowledgeSource xl = new WebIsAlodXLKnowledgeSource();
        WebIsAlodXLLinker linker = new WebIsAlodXLLinker();
        String optionContract = linker.linkToSingleConcept("option contract");
        String contract = linker.linkToSingleConcept("contract");
        String europa = linker.linkToSingleConcept("europa");
        assertTrue(xl.isHypernym(contract, optionContract));
        assertFalse(xl.isHypernym(europa, optionContract));
    }
}