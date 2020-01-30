package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.elementlevel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaselineStringMatcherTest {

    @Test
    void normalizeString() {
        assertEquals(BaselineStringMatcher.normalize("HelloWorld"), BaselineStringMatcher.normalize("hello_world"));
        assertEquals(BaselineStringMatcher.normalize("Hello World"), BaselineStringMatcher.normalize("hello_world"));
        assertEquals(BaselineStringMatcher.normalize("HelloWorld"), BaselineStringMatcher.normalize("hello_world"));
    }
}