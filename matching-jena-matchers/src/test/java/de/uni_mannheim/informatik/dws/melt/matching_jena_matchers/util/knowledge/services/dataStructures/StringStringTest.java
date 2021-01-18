package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.dataStructures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringStringTest {

    @Test
    void equalsTest() {
        StringString s1 = new StringString("A", "B");
        StringString s2 = new StringString("B", "A");
        StringString s3 = new StringString("C", "D");

        assertTrue(s1.equals(s2));
        assertFalse(s1.equals(s3));
        assertFalse(s2.equals(s3));
        assertFalse(s3.equals("CD"));
    }

    @Test
    void hashCodeTest() {
        StringString s1 = new StringString("A", "B");
        StringString s2 = new StringString("B", "A");
        StringString s3 = new StringString("C", "D");

        assertTrue(s1.hashCode() == s2.hashCode());
        assertFalse(s2.hashCode() == s3.hashCode());
        assertFalse(s1.hashCode() == s3.hashCode());
        assertFalse(s1.hashCode() == "AB".hashCode());

        // ---------------------------------------------------------------
        // Minor modification: adding a threshold to the string
        // ---------------------------------------------------------------
        StringString s102 = new StringString("A" + 0.2, "B" + 0.2);
        StringString s202 = new StringString("B" + 0.2, "A" + 0.2);
        StringString s302 = new StringString("C" + 0.2, "D" + 0.2);

        assertTrue(s102.hashCode() == s202.hashCode());
        assertFalse(s202.hashCode() == s302.hashCode());
        assertFalse(s102.hashCode() == s302.hashCode());
        assertFalse(s102.hashCode() == ("AB" + 0.2).hashCode());
        assertFalse(s202.hashCode() == s2.hashCode());
        assertFalse(s102.hashCode() == s1.hashCode());

    }

}