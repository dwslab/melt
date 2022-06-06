package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TransitiveClosureTest {
    
    @Test
    void testTransitiveClosure() {
        TransitiveClosure<String> tc = new TransitiveClosure<>();
        tc.add("a");
        tc.add("b", "c");
        tc.add("d", "e");
        tc.add("f", "g");
        tc.add("h");
        assertEquals(5, tc.getClosure().size());
        
        tc.add("a", "b", "d", "f", "h");
        Collection<Set<String>> test = tc.getClosure();
        assertEquals(1, tc.getClosure().size());        
        assertEquals(8, tc.getClosure().iterator().next().size());        
        assertTrue(tc.getClosure().iterator().next().containsAll(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h")));
        
        tc = new TransitiveClosure<>();
        tc.add("a", "b");
        tc.add("c", "d", "b");
        assertEquals(1, tc.getClosure().size());        
        assertEquals(4, tc.getClosure().iterator().next().size());        
        assertTrue(tc.getClosure().iterator().next().containsAll(Arrays.asList("a", "b", "c", "d")));
    }
    
    @Test
    void getIdentitySetForElementTest() {
        TransitiveClosure<String> tc = new TransitiveClosure<>();
        tc.add("a", "b");
        tc.add("b", "c");
        tc.add("d", "e");
        
        assertEquals(3, tc.getIdentitySetForElement("a").size());        
        assertEquals(2, tc.getIdentitySetForElement("d").size());
        
        assertEquals(tc.getIdentitySetForElement("a"), tc.getIdentitySetForElement("b"));
        assertEquals(tc.getIdentitySetForElement("a"), tc.getIdentitySetForElement("c"));
        assertEquals(tc.getIdentitySetForElement("d"), tc.getIdentitySetForElement("e"));
        
        assertNotEquals(tc.getIdentitySetForElement("a"), tc.getIdentitySetForElement("d"));
        
    }
    
}
