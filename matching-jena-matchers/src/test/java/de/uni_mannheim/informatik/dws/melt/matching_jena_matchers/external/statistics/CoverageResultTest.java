package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.statistics;


import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CoverageResultTest {

    @Test
    void fullTest(){
        Map<String, Set<String>> linked = new HashMap<>();
        // data
        Set<String> links1 = new HashSet<>();
        links1.add("LINK 1");
        Set<String> links2 = new HashSet<>();
        links2.add("LINK 2a");
        links2.add("LINK 2b");
        linked.put("LINKED 1", links1);
        linked.put("LINKED 2", links2);
        Set<String> nonLinked = new HashSet<>();
        nonLinked.add("NOT LINKED 1");
        nonLinked.add("NOT LINKED 2");
        nonLinked.add("NOT LINKED 3");

        CoverageResult result = new CoverageResult(0.5d, linked, nonLinked);
        assertEquals(0.5f, result.getCoverageScore());
        assertEquals(2, result.getLinkedConcepts().size());
        assertEquals(3, result.getNonLinkedConcepts().size());

        // check toString
        String resultString = result.toString();

        for (String s : links1){
            assertTrue(resultString.contains(s));
        }

        for(String s : nonLinked){
            assertTrue(resultString.contains(s));
        }

        assertNotNull(resultString);

        String shortResultString = result.toShortString();
        assertNotNull(shortResultString);

        //System.out.println(resultString);
        //System.out.println(shortResultString);
    }

}