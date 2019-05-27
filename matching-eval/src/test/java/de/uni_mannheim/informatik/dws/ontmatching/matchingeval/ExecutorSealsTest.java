package de.uni_mannheim.informatik.dws.ontmatching.matchingeval;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutorSealsTest {

    @Test
    void surroundWithDoubleQuotes() {
        assertEquals("\"test\"", ExecutorSeals.surroundWithDoubleQuotes("test"));
    }
}