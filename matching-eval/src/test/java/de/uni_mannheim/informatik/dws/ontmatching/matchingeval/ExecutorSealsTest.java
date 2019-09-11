package de.uni_mannheim.informatik.dws.ontmatching.matchingeval;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutorSealsTest {

    @Test
    void surroundWithDoubleQuotes() {
        ExecutorSeals s = new ExecutorSeals("./seals.jar", "/home/seals/");
        assertEquals("\"test\"", s.surroundWithDoubleQuotes("test"));
    }

    @Test
    void testIsDirectoryRunnableInSeals() {
        assertTrue(ExecutorSeals.isDirectoryRunnableInSeals("./src/test/resources/sealsCompliantDirectory"));
        assertFalse(ExecutorSeals.isDirectoryRunnableInSeals("./src/test/resources/sealsNonCompliantDirectory"));
    }
}