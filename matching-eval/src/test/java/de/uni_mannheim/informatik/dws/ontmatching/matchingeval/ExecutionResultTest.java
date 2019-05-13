package de.uni_mannheim.informatik.dws.ontmatching.matchingeval;

import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

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
}