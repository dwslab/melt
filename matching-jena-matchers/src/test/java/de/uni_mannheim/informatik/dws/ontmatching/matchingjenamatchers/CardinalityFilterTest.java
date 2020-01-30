package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.filter.CardinalityFilter;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class CardinalityFilterTest {
    
    @Test
    void testFilter() throws IOException {
        
        Alignment m = new Alignment();
        
        m.add("one", "two", 0.9);
        m.add("one", "three", 1.0);
        
        Alignment filtered = CardinalityFilter.filter(m);
        assertEquals(1, filtered.size());
        assertEquals("three", filtered.iterator().next().getEntityTwo());
    }
    
    @Test
    void testFilterisDeterministic() throws IOException {
        
        Alignment x = new Alignment();        
        x.add("one", "two", 1.0);
        x.add("one", "three", 1.0);    
        x.add("one", "four", 1.0);
        x.add("one", "five", 1.0);
        Alignment filteredX = CardinalityFilter.filter(x);
        
        Alignment y = new Alignment(); 
        y.add("one", "five", 1.0);
        y.add("one", "three", 1.0); 
        y.add("one", "four", 1.0);
        y.add("one", "two", 1.0);
        Alignment filteredY = CardinalityFilter.filter(y);
        
        assertEquals(filteredX, filteredY);
    }
    
    @Test
    void testFilterisDeterministicTwo() throws IOException {
        
        Alignment x = new Alignment();        
        x.add("one", "two", 1.0);
        x.add("one", "aaaa", 1.0);
        x.add("one", "zzzzz", 1.0);
        x.add("one", "three", 0.9);    
        x.add("one", "four", 0.8);
        x.add("one", "five", 0.7);
        Alignment filtered = CardinalityFilter.filter(x);
        
        assertEquals(1, filtered.size());
        assertEquals("aaaa", filtered.iterator().next().getEntityTwo());
    }
    
}
