package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.filter.CardinalityFilter;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class CardinalityFilterTest {
    
    @Test
    void testFilter() throws IOException {
        
        Alignment m = new Alignment(true, true, true, true);
        
        m.add("one", "two", 0.9);
        m.add("one", "three", 1.0);
        
        Alignment filtered = CardinalityFilter.filter(m);
        assertEquals(1, filtered.size());
        assertEquals("three", filtered.iterator().next().getEntityTwo());
    }
    
}
