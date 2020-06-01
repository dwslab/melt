package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class NumberToWordsConvertTest {
    
    @Test
    void testConvert() {
        assertEquals("one hundred twenty three million four hundred fifty six thousand seven hundred eighty nine", NumberToWordsConverter.convert(123456789));
        assertEquals("one hundred twenty three million four hundred fifty six thousand seven hundred eighty nine", NumberToWordsConverter.convert("123456789"));
        assertEquals("zero", NumberToWordsConverter.convert(0));
        assertEquals("eleven", NumberToWordsConverter.convert(11));
        assertEquals("minus forty two", NumberToWordsConverter.convert(-42));
    }
    
    @Test
    void testConvertText() {
        assertEquals("hello forty two nicetwohave", NumberToWordsConverter.replaceAllNumbersInText("hello 42 nice2have"));
    }
    
    @Test
    void testConvertTextRange() {
        assertEquals("hifive, six dogs and ten animals but not 11 cats", 
                new NumberToWordsConverter(10).replaceNumbersInText("hi5, 6 dogs and 10 animals but not 11 cats"));
        
        assertEquals("eight nine 10", 
                new NumberToWordsConverter().replaceNumbersInText("8 9 10"));
    }
    
    @Test
    void testreplaceToken() {
        assertEquals("five", new NumberToWordsConverter().replaceNumberToken("5"));
        assertEquals("11", new NumberToWordsConverter().replaceNumberToken("11"));
        assertEquals("foo", new NumberToWordsConverter().replaceNumberToken("foo"));
        
        assertEquals(3, new NumberToWordsConverter(5,7).getReplacementMap().size());
    }
}
