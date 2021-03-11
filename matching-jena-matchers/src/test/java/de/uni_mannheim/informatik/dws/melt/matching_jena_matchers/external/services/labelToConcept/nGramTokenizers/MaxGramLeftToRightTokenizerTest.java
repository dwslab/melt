package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers;

import org.junit.jupiter.api.Test;


import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MaxGramLeftToRightTokenizerTest {


    @Test
    void getNextTokenNotSuccessful() {
        String[] input = {"hello", "world", "peace"};
        MaxGramLeftToRightTokenizer tokenizer = new MaxGramLeftToRightTokenizer( input , " ");

        assertEquals("hello world peace",tokenizer.getInitialToken());
        assertEquals("hello world", tokenizer.getNextTokenNotSuccessful());
        assertEquals("hello", tokenizer.getNextTokenNotSuccessful());
        assertEquals("world peace", tokenizer.getNextTokenNotSuccessful());
        assertEquals("world", tokenizer.getNextTokenNotSuccessful());
        assertEquals("peace", tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenNotSuccessful());

        // test: call without initial
        tokenizer = new MaxGramLeftToRightTokenizer( input , " ");
        assertEquals("hello world", tokenizer.getNextTokenNotSuccessful());

        // test: no success at all
        String[] input3 = {"hello", "world", "peace", "european", "federation"};
        tokenizer = new MaxGramLeftToRightTokenizer( input3 , " ");
        assertEquals("hello world peace european",tokenizer.getNextTokenNotSuccessful());
        assertEquals("hello world peace",tokenizer.getNextTokenNotSuccessful());
        assertEquals("hello world",tokenizer.getNextTokenNotSuccessful());
        assertEquals("hello",tokenizer.getNextTokenNotSuccessful());
        assertEquals("world peace european federation",tokenizer.getNextTokenNotSuccessful());
        assertEquals("world peace european",tokenizer.getNextTokenNotSuccessful());
        assertEquals("world peace",tokenizer.getNextTokenNotSuccessful());
        assertEquals("world",tokenizer.getNextTokenNotSuccessful());
        assertEquals("peace european federation",tokenizer.getNextTokenNotSuccessful());
        assertEquals("peace european",tokenizer.getNextTokenNotSuccessful());
        assertEquals("peace",tokenizer.getNextTokenNotSuccessful());
        assertEquals("european federation",tokenizer.getNextTokenNotSuccessful());
        assertEquals("european",tokenizer.getNextTokenNotSuccessful());
        assertEquals("federation",tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenNotSuccessful());

        String[] input4 = {"hello", "world"};
        tokenizer = new MaxGramLeftToRightTokenizer( input4 , " ");
        assertEquals("hello",tokenizer.getNextTokenNotSuccessful());
        assertEquals("world",tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenNotSuccessful());
    }


    @Test
    void getNextTokenSuccessful() {
        String[] input = {"hello", "world"};
        MaxGramLeftToRightTokenizer tokenizer = new MaxGramLeftToRightTokenizer( input , " ");
        assertNull(tokenizer.getNextTokenSuccessful());
    }


    @Test
    void getNextTokenMixed() {
        String[] input = {"hello", "world", "peace"};
        MaxGramLeftToRightTokenizer tokenizer = new MaxGramLeftToRightTokenizer( input , " ");

        assertEquals("hello world peace",tokenizer.getInitialToken());
        assertEquals("hello world", tokenizer.getNextTokenNotSuccessful());
        assertEquals("peace", tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());

        // test with different delimiter
        tokenizer = new MaxGramLeftToRightTokenizer( input , " ");
        tokenizer.setDelimiter("_");
        assertEquals("hello_world_peace",tokenizer.getInitialToken());
        assertEquals("hello_world", tokenizer.getNextTokenNotSuccessful());
        assertEquals("peace", tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());

        // test with different delimiter
        tokenizer = new MaxGramLeftToRightTokenizer( input , "___");
        assertEquals("hello___world___peace",tokenizer.getInitialToken());
        assertEquals("hello___world", tokenizer.getNextTokenNotSuccessful());
        assertEquals("peace", tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());

        // test: successful initial token
        tokenizer = new MaxGramLeftToRightTokenizer( input , " ");
        assertEquals("hello world peace",tokenizer.getInitialToken());
        assertNull(tokenizer.getNextTokenSuccessful());

        String[] input2 = {"hello", "world", "peace", "europe"};
        tokenizer = new MaxGramLeftToRightTokenizer( input2 , " ");
        assertEquals("hello world peace europe",tokenizer.getInitialToken());
        assertEquals("hello world peace",tokenizer.getNextTokenNotSuccessful());
        assertEquals("hello world",tokenizer.getNextTokenNotSuccessful());
        assertEquals("peace europe",tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());

        String[] input3 = {"hello", "world", "peace", "european", "federation"};
        tokenizer = new MaxGramLeftToRightTokenizer( input3 , " ");
        assertEquals("hello world peace european federation",tokenizer.getInitialToken());
        assertEquals("hello world peace european",tokenizer.getNextTokenNotSuccessful());
        assertEquals("hello world peace",tokenizer.getNextTokenNotSuccessful());
        assertEquals("hello world",tokenizer.getNextTokenNotSuccessful());
        assertEquals("hello",tokenizer.getNextTokenNotSuccessful());
        assertEquals("world peace european federation",tokenizer.getNextTokenSuccessful());
        assertEquals("world peace european",tokenizer.getNextTokenNotSuccessful());
        assertEquals("federation",tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());

        String[] input4 = {"hello", "world"};
        tokenizer = new MaxGramLeftToRightTokenizer( input4 , " ");
        assertEquals("hello",tokenizer.getNextTokenNotSuccessful());
        assertEquals("world",tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());
    }


    @Test
    void getInitialToken() {
        String[] input = {"hello", "world", "peace"};
        MaxGramLeftToRightTokenizer tokenizer = new MaxGramLeftToRightTokenizer( input , " ");
        assertEquals("hello world peace" ,tokenizer.getInitialToken());
        assertEquals("hello world peace" ,tokenizer.getInitialToken());
    }

    @Test
    void terminationTest(){
        String[] input = {"hello"};
        MaxGramLeftToRightTokenizer tokenizer = new MaxGramLeftToRightTokenizer( input , " ");
        assertEquals("hello" ,tokenizer.getInitialToken());
        assertNull(tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getInitialToken());
        assertNull(tokenizer.getNextTokenSuccessful());

        tokenizer = new MaxGramLeftToRightTokenizer( input , " ");
        assertNull(tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getInitialToken());
        assertNull(tokenizer.getNextTokenSuccessful());

        tokenizer = new MaxGramLeftToRightTokenizer( input , " ");
        assertNull(tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getInitialToken());
        assertNull(tokenizer.getNextTokenNotSuccessful());

        tokenizer = new MaxGramLeftToRightTokenizer(null, "_");
        assertNull(tokenizer.getInitialToken());
        assertNull(tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenNotSuccessful());
        assertTrue(tokenizer.isTerminated());
    }

    @Test
    void getNotLinked() {

        MaxGramLeftToRightTokenizer tokenizer;

        String[] input1 = {"european", "federation"};
        tokenizer = new MaxGramLeftToRightTokenizer( input1 , "_");
        assertEquals("european_federation", tokenizer.getInitialToken());
        tokenizer.getNextTokenSuccessful();
        assertNull(tokenizer.getNextTokenNotSuccessful());
        assertEquals(0, tokenizer.getNotLinked().size());

        String[] input2 = {"united", "european", "federation"};
        tokenizer = new MaxGramLeftToRightTokenizer( input2 , "_");
        assertEquals("united_european_federation", tokenizer.getInitialToken());
        assertEquals("united_european", tokenizer.getNextTokenNotSuccessful());
        assertEquals("united", tokenizer.getNextTokenNotSuccessful());
        assertEquals("european_federation", tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());
        assertTrue(tokenizer.isTerminated());
        ArrayList<String> notFound = tokenizer.getNotLinked();
        assertEquals(1, notFound.size());
        assertEquals("united", notFound.get(0));

        // test: no success at all
        String[] input3 = {"hello", "world", "peace", "european", "federation"};
        tokenizer = new MaxGramLeftToRightTokenizer( input3 , " ");
        assertEquals("hello world peace european",tokenizer.getNextTokenNotSuccessful());
        assertEquals("hello world peace",tokenizer.getNextTokenNotSuccessful());
        assertEquals("hello world",tokenizer.getNextTokenNotSuccessful());
        assertEquals("hello",tokenizer.getNextTokenNotSuccessful());
        assertEquals("world peace european federation",tokenizer.getNextTokenNotSuccessful());
        assertEquals("world peace european",tokenizer.getNextTokenNotSuccessful());
        assertEquals("world peace",tokenizer.getNextTokenNotSuccessful());
        assertEquals("world",tokenizer.getNextTokenNotSuccessful());
        assertEquals("peace european federation",tokenizer.getNextTokenNotSuccessful());
        assertEquals("peace european",tokenizer.getNextTokenNotSuccessful());
        assertEquals("peace",tokenizer.getNextTokenNotSuccessful());
        assertEquals("european federation",tokenizer.getNextTokenNotSuccessful());
        assertEquals("european",tokenizer.getNextTokenNotSuccessful());
        assertEquals("federation",tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenNotSuccessful());
        assertTrue(tokenizer.isTerminated());

        ArrayList<String> notLinked = tokenizer.getNotLinked();
        assertTrue(input3.length == notLinked.size());
        for(int i = 0; i < notLinked.size(); i++){
            assertEquals(input3[i], notLinked.get(i));
        }

        String[] input4 = {"united", "european", "federation"};
        tokenizer = new MaxGramLeftToRightTokenizer( input4 , "_");
        assertEquals("united_european_federation", tokenizer.getInitialToken());
        assertEquals("united_european", tokenizer.getNextTokenNotSuccessful());
        assertEquals("united", tokenizer.getNextTokenNotSuccessful());
        assertEquals("european_federation", tokenizer.getNextTokenNotSuccessful());
        assertEquals("european", tokenizer.getNextTokenNotSuccessful());
        assertEquals("federation", tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());
        assertTrue(tokenizer.isTerminated());
        ArrayList<String> notFound4 = tokenizer.getNotLinked();
        assertEquals(2, notFound4.size());
        assertEquals("united", notFound4.get(0));
        assertEquals("european", notFound4.get(1));
    }

}