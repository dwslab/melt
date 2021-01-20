package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class TestOperationsTest {

    @Test
    void setContainsSameContent(){
        HashSet<String> s1 = new HashSet<>();
        HashSet<String> s2 = new HashSet<>();
        s1.add("hello");
        s2.add("hello");
        s1.add("world");

        assertFalse(TestOperations.setContainsSameContent(s1,s2));

        s2.add("world");
        assertTrue(TestOperations.setContainsSameContent(s1, s2));
    }

    @Test
    void isSameStringArray(){
        String[] s1 = {"hello", "world"};
        String[] s2 = {"hello", "world"};
        String[] s3 = {"hello", "World"};
        String[] s4 = {"hello"};

        assertTrue(TestOperations.isSameStringArray(s1, s2));
        assertFalse(TestOperations.isSameStringArray(s1, s3));
        assertFalse(TestOperations.isSameStringArray(s1, s4));
    }

    @Test
    void isSameArrayContent(){
        String[] s1 = {"hello", "world"};
        String[] s2 = {"world", "hello"};
        String[] s3 = {"hello"};
        String[] s4 = {"hello", "peace", "world"};
        String[] s5 = {"hello", "world", "peace"};

        assertTrue(TestOperations.isSameArrayContent(s1,s2));
        assertTrue(TestOperations.isSameArrayContent(s2,s1));

        assertFalse(TestOperations.isSameArrayContent(s1,s3));
        assertFalse(TestOperations.isSameArrayContent(s3,s1));

        assertTrue(TestOperations.isSameArrayContent(s4,s5));
        assertTrue(TestOperations.isSameArrayContent(s5,s4));
    }

    @Test
    void getStringKeyFromResourceBundle(){
        String result = TestOperations.getStringKeyFromResourceBundle("file_for_test", "testVariable");
        assertEquals("Hello World", result);
        result = TestOperations.getStringKeyFromResourceBundle("file_for_test.properties", "testVariable");
        assertEquals("Hello World", result);
        result = TestOperations.getStringKeyFromResourceBundle("file_for_test_does_not_exist", "testVariable");
        assertNull(result);
    }

}