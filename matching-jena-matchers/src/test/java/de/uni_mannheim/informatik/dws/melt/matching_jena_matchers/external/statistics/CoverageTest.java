package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.statistics;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.classic.WebIsAlodClassicLinker;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CoverageTest {


    @Test
    void getCoveragePartialLabel(){
        WebIsAlodClassicLinker linker = new WebIsAlodClassicLinker();
        Set<String> entities = new HashSet<>();
        entities.add("europe");
        entities.add("EU");
        entities.add("XKRZY335_NOT_EXISTING car");

        CoverageResult result = Coverage.getCoveragePartialLabel(linker, entities);
        assertEquals((float) 1, result.getCoverageScore());
        assertTrue(result.getLinkedConcepts().containsKey("europe"));
        assertTrue(result.getLinkedConcepts().containsKey("EU"));
        assertTrue(result.getLinkedConcepts().containsKey("XKRZY335_NOT_EXISTING car"));
        assertEquals(0, result.getNonLinkedConcepts().size());
    }

    @Test
    void getCoverageFullLabel() {
        WebIsAlodClassicLinker linker = new WebIsAlodClassicLinker();
        Set<String> entities = new HashSet<>();
        entities.add("europe");
        entities.add("EU");
        entities.add("XKRZY335_NOT_EXISTING");

        CoverageResult result = Coverage.getCoverageFullLabel(linker, entities);
        assertEquals((float) 2 / 3, result.getCoverageScore());
        assertTrue(result.getLinkedConcepts().containsKey("europe"));
        assertTrue(result.getLinkedConcepts().containsKey("EU"));
        assertFalse(result.getLinkedConcepts().containsKey("XKRZY335_NOT_EXISTING"));
        assertTrue(result.getNonLinkedConcepts().contains("XKRZY335_NOT_EXISTING"));
        assertFalse(result.getNonLinkedConcepts().contains("europe"));
        assertFalse(result.getNonLinkedConcepts().contains("EU"));
    }
}