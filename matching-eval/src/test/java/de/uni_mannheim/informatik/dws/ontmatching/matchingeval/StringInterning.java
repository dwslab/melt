package de.uni_mannheim.informatik.dws.ontmatching.matchingeval;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test is not testing a particular class but basic axioms required for the implemented memory-optimized
 * Alignment structure.
 */
public class StringInterning {

    @Test
    public void interningTest(){

        String s1 = "hello world";
        String s2 = "hello world";
        assertTrue(s1 == s2);

        String s3 = new String("hello world");
        String s4 = new String("hello world");

        // reference tests
        assertFalse(s1 == s3);
        assertFalse(s1 == s4);
        assertFalse(s2 == s3);
        assertFalse(s2 == s4);
        assertFalse(s3 == s4);
        assertTrue(s1 == s2);

        // equality tests
        assertTrue(s1.equals(s2));
        assertTrue(s1.equals(s3));
        assertTrue(s1.equals(s4));

        // no change because result of intern() is not assigned
        s3.intern();
        s4.intern();
        assertFalse(s1 == s3);
        assertFalse(s1 == s4);
        assertFalse(s2 == s3);
        assertFalse(s2 == s4);
        assertFalse(s3 == s4);

        // no referentially, s1 to s4 should be the same
        s3 = s3.intern();
        s4 = s4.intern();
        assertTrue(s1 == s3);
        assertTrue(s1 == s4);
        assertTrue(s2 == s3);
        assertTrue(s2 == s4);
        assertTrue(s3 == s4);
    }
}
