package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BagOfWordsTest {

    @Test
    void equalsTest() {
        BagOfWords bag1 = new BagOfWords(new String[]{"hello", "world"});
        BagOfWords bag2 = new BagOfWords(new String[]{"hello", "world"});
        BagOfWords bag3 = new BagOfWords(new String[]{"world", "hello"});
        BagOfWords bag4 = new BagOfWords(new String[]{"World", "hello"});
        BagOfWords bag5 = new BagOfWords(new String[]{"world", "hello", "peace"});
        BagOfWords bag6 = new BagOfWords(new String[]{"hello"});
        BagOfWords bag7 = new BagOfWords(new String[]{"hello", "world", "peace", "humanity", "equality", "freedom", "one"});
        String randomObject1 = new String();
        String randomObject2 = "hello world";
        String randomObject3 = "hello";

        assertTrue(bag1.equals(bag2));
        assertTrue(bag1.equals(bag3));
        assertTrue(bag2.equals(bag3));
        assertFalse(bag3.equals(bag4));
        assertFalse(bag1.equals(bag5));
        assertFalse(bag2.equals(bag5));
        assertFalse(bag3.equals(bag5));
        assertFalse(bag5.equals(randomObject3));
        assertFalse(bag1.equals(randomObject1));
        assertFalse(bag1.equals(randomObject2));
        assertFalse(bag6.equals(randomObject3));
        assertFalse(bag7.equals(bag6));
    }

    @Test
    void hashCodeTest() {
        BagOfWords bag1 = new BagOfWords(new String[]{"hello", "world"});
        BagOfWords bag2 = new BagOfWords(new String[]{"hello", "world"});
        BagOfWords bag3 = new BagOfWords(new String[]{"world", "hello"});
        BagOfWords bag4 = new BagOfWords(new String[]{"World", "hello"});
        BagOfWords bag5 = new BagOfWords(new String[]{"world", "hello", "peace"});
        BagOfWords bag6 = new BagOfWords(new String[]{"hello"});
        String randomObject1 = new String();
        String randomObject2 = "hello world";
        String randomObject3 = "hello";

        assertTrue(bag1.hashCode() == bag2.hashCode());
        assertTrue(bag1.hashCode() == bag3.hashCode());
        assertTrue(bag2.hashCode() == bag3.hashCode());
        assertFalse(bag1.hashCode() == randomObject1.hashCode());
        assertFalse(bag3.hashCode() == bag4.hashCode());
        assertFalse(bag1.hashCode() == bag5.hashCode());
        assertFalse(bag2.hashCode() == bag5.hashCode());
        assertFalse(bag3.hashCode() == bag5.hashCode());
        assertFalse(bag5.hashCode() == randomObject3.hashCode());
        assertFalse(bag1.hashCode() == randomObject1.hashCode());
        assertFalse(bag1.hashCode() == randomObject2.hashCode());
        assertFalse(bag6.hashCode() == randomObject3.hashCode());
    }
}