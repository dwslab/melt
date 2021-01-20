package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NgramLeftToRightTokenizerTest {


    @Test
    void getNextTokenNotSuccessful(){

        // ngram size: 2
        String[] dummy = {"a", "b", "c"};
        NgramLeftToRightTokenizer tokenizer = new NgramLeftToRightTokenizer(dummy," ", 2);
        assertEquals("a b", tokenizer.getInitialToken());
        assertEquals("a",tokenizer.getNextTokenNotSuccessful());
        assertEquals("b c",tokenizer.getNextTokenNotSuccessful());
        assertEquals("b",tokenizer.getNextTokenNotSuccessful());
        assertEquals("c",tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenNotSuccessful());

    }

    @Test
    void getNextTokenSuccessful(){

        // ngram size: 2
        String[] data = {"a", "b", "c", "d"};
        NgramLeftToRightTokenizer tokenizer = new NgramLeftToRightTokenizer(data," ", 2);
        assertEquals("a b", tokenizer.getInitialToken());
        assertEquals("c d",tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());

        // ngram size: 3
        String[] data2 = {"a", "b", "c", "d"};
        tokenizer = new NgramLeftToRightTokenizer(data2," ", 3);
        assertEquals("a b c", tokenizer.getInitialToken());
        assertEquals("d",tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());

        // ngram size: 3
        String[] data3 = {"a", "b", "c", "d", "e", "f"};
        tokenizer = new NgramLeftToRightTokenizer(data3," ", 3);
        assertEquals("a b c", tokenizer.getInitialToken());
        assertEquals("d e f",tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());
    }


    @Test
    void mixedNext(){
        // ngram size: 2
        String[] dummy = {"a", "b", "c", "d"};
        NgramLeftToRightTokenizer tokenizer = new NgramLeftToRightTokenizer(dummy," ", 2);
        assertEquals("a b", tokenizer.getInitialToken());
        assertEquals("c d",tokenizer.getNextTokenSuccessful());
        assertEquals("c", tokenizer.getNextTokenNotSuccessful());
        assertEquals("d", tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenNotSuccessful());

        // ngram size: 2
        tokenizer = new NgramLeftToRightTokenizer(dummy," ", 2);
        assertEquals("a b", tokenizer.getInitialToken());
        assertEquals("c d",tokenizer.getNextTokenSuccessful());
        assertEquals("c", tokenizer.getNextTokenNotSuccessful());
        assertEquals("d", tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());

        // ngram size: 2
        tokenizer = new NgramLeftToRightTokenizer(dummy," ", 2);
        assertEquals("a b", tokenizer.getInitialToken());
        assertEquals("c d",tokenizer.getNextTokenSuccessful());
        assertEquals("c", tokenizer.getNextTokenNotSuccessful());
        assertEquals("d", tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());

        // ngram size: 3
        String[] data3 = {"a", "b", "c", "d", "e", "f"};
        tokenizer = new NgramLeftToRightTokenizer(data3," ", 3);
        assertEquals("a b c", tokenizer.getInitialToken());
        assertEquals("a b",tokenizer.getNextTokenNotSuccessful());
        assertEquals("a",tokenizer.getNextTokenNotSuccessful());
        assertEquals("b c d",tokenizer.getNextTokenNotSuccessful());
        assertEquals("e f",tokenizer.getNextTokenSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());

        tokenizer = new NgramLeftToRightTokenizer(data3," ", 3);
        assertEquals("a b c", tokenizer.getInitialToken());
        assertEquals("a b",tokenizer.getNextTokenNotSuccessful());
        assertEquals("a",tokenizer.getNextTokenNotSuccessful());
        assertEquals("b c d",tokenizer.getNextTokenNotSuccessful());
        assertEquals("e f",tokenizer.getNextTokenSuccessful());
        assertEquals("e",tokenizer.getNextTokenNotSuccessful());
        assertEquals("f",tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());

        tokenizer = new NgramLeftToRightTokenizer(data3," ", 4);
        assertEquals("a b c d", tokenizer.getInitialToken());
        assertEquals("e f",tokenizer.getNextTokenSuccessful());
        assertEquals("e",tokenizer.getNextTokenNotSuccessful());
        assertEquals("f",tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenNotSuccessful());
        assertNull(tokenizer.getNextTokenSuccessful());

    }


    @Test
    void getnGramsize() {
        String[] dummy = {"d", "f"};
        NgramLeftToRightTokenizer tokenizer = new NgramLeftToRightTokenizer(dummy,"", -1);
        assertEquals(1, tokenizer.getnGramsize());
    }

    @Test
    void setnGramsize() {
        String[] dummy = {"d", "f"};
        NgramLeftToRightTokenizer tokenizer = new NgramLeftToRightTokenizer(dummy,"", -1);
        assertEquals(1, tokenizer.getnGramsize());
        tokenizer.setnGramsize(0);
        assertEquals(1, tokenizer.getnGramsize());
        tokenizer.setnGramsize(10);
        assertEquals(10, tokenizer.getnGramsize());
    }

    @Test
    void getInitialToken(){

        // ngram size: 1
        String[] dummy = {"a", "b", "c"};
        NgramLeftToRightTokenizer tokenizer = new NgramLeftToRightTokenizer(dummy," ", -1);
        assertEquals("a", tokenizer.getInitialToken());

        // ngram size: 2
        tokenizer = new NgramLeftToRightTokenizer(dummy," ", 2);
        assertEquals("a b", tokenizer.getInitialToken());

        // ngram size: 3
        tokenizer = new NgramLeftToRightTokenizer(dummy," ", 3);
        assertEquals("a b c", tokenizer.getInitialToken());

        // ngram size: 4
        tokenizer = new NgramLeftToRightTokenizer(dummy," ", 4);
        assertEquals("a b c", tokenizer.getInitialToken());
    }

}