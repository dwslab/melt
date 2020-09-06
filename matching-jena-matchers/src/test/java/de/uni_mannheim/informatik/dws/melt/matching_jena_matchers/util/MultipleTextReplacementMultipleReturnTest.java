package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MultipleTextReplacementMultipleReturnTest {
    
    @Test
    void testReplacement() {
        Map<String, Set<String>> replacement = new HashMap();
        replacement.put("band", new HashSet(Arrays.asList("band", "music group", "ring")));
        
        MultipleTextReplacementMultiReturn replacer = new MultipleTextReplacementMultiReturn(replacement);
        
        Set<String> replacements = replacer.replace("hello band test");
        assertEquals(3, replacements.size());
        
        assertTrue(replacements.contains("hello band test"));
        assertTrue(replacements.contains("hello music group test"));
        assertTrue(replacements.contains("hello ring test"));
    }
    
    @Test
    void testMultiReplacement() {
        Map<String, Set<String>> replacement = new HashMap();
        replacement.put("help", new HashSet(Arrays.asList("help", "assist")));
        replacement.put("quiet", new HashSet(Arrays.asList("quiet", "silent")));
        
        MultipleTextReplacementMultiReturn replacer = new MultipleTextReplacementMultiReturn(replacement);
        
        Set<String> replacements = replacer.replace("remember: help and be quiet !");
        assertEquals(4, replacements.size());
        
        assertTrue(replacements.contains("remember: help and be quiet !"));
        assertTrue(replacements.contains("remember: help and be silent !"));        
        assertTrue(replacements.contains("remember: assist and be quiet !"));
        assertTrue(replacements.contains("remember: assist and be silent !"));
    }
}
