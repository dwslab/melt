package de.uni_mannheim.informatik.dws.melt.matching_eval;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutorSealsTest {

    @Test
    void testIsDirectoryRunnableInSeals() {
        assertTrue(ExecutorSeals.isDirectoryRunnableInSeals("./src/test/resources/sealsCompliantDirectory"));
        assertFalse(ExecutorSeals.isDirectoryRunnableInSeals("./src/test/resources/sealsNonCompliantDirectory"));
    }
    
    @Test
    void testGetMatcherNameFromSealsDescriptor() {
        assertEquals("DemoMatcher", ExecutorSeals.getMatcherNameFromSealsDescriptor("./src/test/resources/sealsCompliantDirectory"));
        assertEquals("DemoMatcher", ExecutorSeals.getMatcherNameFromSealsDescriptor("./src/test/resources/sealsCompliantDirectory/descriptor.xml"));
        assertEquals("", ExecutorSeals.getMatcherNameFromSealsDescriptor("./src/test/resources/sealsCompliantDirectory/notexistent.xml"));
    }
}