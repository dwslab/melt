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
    
    @Test
    void testAdd() {
        TransitiveClosure<String> a = new TransitiveClosure<>();
        a.add("a", "b");
        a.add("b", "c");
        a.add("d", "e");
        
        TransitiveClosure<String> b = new TransitiveClosure<>();
        b.add("f", "g");
        b.add("a", "x");
        
        assertEquals(2, a.getClosure().size());
        assertEquals(2, b.getClosure().size());
        
        assertEquals(5, a.countOfAllElements());
        assertEquals(4, b.countOfAllElements());
        
        a.add(b);
        
        assertEquals(8, a.countOfAllElements());
        assertEquals(4, b.countOfAllElements());
        
        assertEquals(3, a.getClosure().size()); //a,b,c,x  d,e   f,g   
        assertTrue(a.getIdentitySetForElement("a").contains("x"));
    }
    
}
