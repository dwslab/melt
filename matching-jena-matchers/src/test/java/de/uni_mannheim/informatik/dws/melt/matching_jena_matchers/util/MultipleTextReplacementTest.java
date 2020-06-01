package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MultipleTextReplacementTest {
    
    @Test
    void testReplacement() {
        Map<String, String> replacement = new HashMap();
        replacement.put("one", "1");
        replacement.put("two", "2");        
        MultipleTextReplacement replacer = new MultipleTextReplacement(replacement);
        assertEquals("hello 1 and word2vec", replacer.replace("hello one and wordtwovec"));
    }
    
    @Test
    void testReplacementSpecialCharactersAsInput() {
        Map<String, String> replacement = new HashMap();
        replacement.put("\\s", "x");
        replacement.put(".", "dot");
        replacement.put("(test)", "brackets");
        MultipleTextReplacement replacer = new MultipleTextReplacement(replacement);
        assertEquals("hello x replace dot brackets", replacer.replace("hello \\s replace . (test)"));
    }
    
    @Test
    void testReplacementSpecialCharactersToReplace() {
        Map<String, String> replacement = new HashMap();
        replacement.put("x", "foo$20bar");
        replacement.put("y", "\\s");
        MultipleTextReplacement replacer = new MultipleTextReplacement(replacement);
        assertEquals("a foo$20bar \\s b", replacer.replace("a x y b"));
    }
    
    @Test
    void testReplacementSamePrefix() {
        List<Entry<String, String>> replacements = new ArrayList();
        replacements.add(new SimpleEntry("hi", "hello"));
        replacements.add(new SimpleEntry("hii", "hellooo"));        
        MultipleTextReplacement replacer = new MultipleTextReplacement(replacements);
        assertEquals("helloi", replacer.replace("hii"));
        
        replacements = new ArrayList();
        replacements.add(new SimpleEntry("hii", "hellooo"));
        replacements.add(new SimpleEntry("hi", "hello"));        
        replacer = new MultipleTextReplacement(replacements);
        assertEquals("hellooo", replacer.replace("hii"));
        
        Map<String, String> replacement = new HashMap();
        replacement.put("hi", "hello");
        replacement.put("hii", "hellooo");
        replacer = new MultipleTextReplacement(replacement);
        assertEquals("hellooo", replacer.replace("hii"));
    }
    
    @Test
    void testReplacementLargeReplacement() {
        Map<String, String> replacement = new HashMap();
        for(int i=0; i < 30000; i++){
            replacement.put(Integer.toString(i), "(" + i + ")");
        }
        MultipleTextReplacement replacer = new MultipleTextReplacement(replacement);
        assertEquals("hello (15) and within(20001)text", replacer.replace("hello 15 and within20001text"));
    }
    
    
}
