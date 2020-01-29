package de.uni_mannheim.informatik.dws.ontmatching.matchingeval;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.MAC;

class ExecutionResultTest {

    @Test
    void getMatcherNameComparator() {
        ArrayList<ExecutionResult> list = new ArrayList<>();
        list.add(new ExecutionResult(null, "B-Matcher", new Alignment(), new Alignment()));
        list.add(new ExecutionResult(null, "A-Matcher", new Alignment(), new Alignment()));
        assertTrue(list.get(0).getMatcherName().equals("B-Matcher"));
        assertTrue(list.get(1).getMatcherName().equals("A-Matcher"));
        Collections.sort(list, ExecutionResult.getMatcherNameComparator());
        assertTrue(list.get(0).getMatcherName().equals("A-Matcher"));
        assertTrue(list.get(1).getMatcherName().equals("B-Matcher"));
    }

    @Test
    @EnabledOnOs({ MAC })
    void testNonParseableSystemAlignment() {
        try {
            ExecutionResult result = new ExecutionResult(
                    TrackRepository.Conference.V1.getTestCases().get(0),
                    "Failing Matcher",
                    new File("./src/test/resources/EmptyReferencealignmentForTest.rdf").toURI().toURL(),
                    10L,
                    null);
            assertNotNull(result.getSystemAlignment());
            assertNotNull(result.getOriginalSystemAlignment());
            assertEquals(new File("./src/test/resources/EmptyReferencealignmentForTest.rdf").toURI().toURL(), result.getOriginalSystemAlignment());
        } catch (MalformedURLException mue){
            mue.printStackTrace();
            fail("Test case ran into an exception.");
        }
    }

}