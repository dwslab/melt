package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools.TestOperations;
import org.junit.jupiter.api.Test;


import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class NgramTokenizerTest {

    @Test
    void getNgrams() {
        NgramTokenizer tokenizer = new NgramTokenizer(3, "_");
        String[] input = {"hello", "world", "i", "wish", "you", "peace"};
        HashSet<String> result = new HashSet<>();
        result.add("hello_world_i");
        result.add("world_i_wish");
        result.add("i_wish_you");
        result.add("wish_you_peace");
        assertTrue(TestOperations.setContainsSameContent(result, tokenizer.getNgrams(input)));

        tokenizer.setnGramsize(1);
        result = new HashSet<>();
        result.add("hello");
        result.add("world");
        result.add("i");
        result.add("wish");
        result.add("you");
        result.add("peace");
        assertTrue(TestOperations.setContainsSameContent(result, tokenizer.getNgrams(input)));

        // having ngram sizes larger than the data
        tokenizer.setnGramsize(6);
        result = new HashSet<>();
        result.add("hello_world_i_wish_you_peace");
        assertTrue(TestOperations.setContainsSameContent(result, tokenizer.getNgrams(input)));
        tokenizer.setnGramsize(10);
        assertTrue(TestOperations.setContainsSameContent(result, tokenizer.getNgrams(input)));

        // setting a different delimiter
        tokenizer.setDelimiter("__");
        assertEquals("__", tokenizer.getDelimiter());
        result = new HashSet<>();
        result.add("hello__world__i__wish__you__peace");
        assertTrue(TestOperations.setContainsSameContent(result, tokenizer.getNgrams(input)));
    }


    @Test
    void setAndGetNgramSize(){
        NgramTokenizer tokenizer = new NgramTokenizer(-1, "_");
        assertEquals(1, tokenizer.getnGramsize());

        tokenizer.setnGramsize(5);
        assertEquals(5, tokenizer.getnGramsize());

        tokenizer.setnGramsize(0);
        assertEquals(1, tokenizer.getnGramsize());
    }
}