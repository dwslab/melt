package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


public class NtoMCorrespondenceFilterTest {
    
    @Test
    void testFilter() throws IOException {
        
        Alignment a = new Alignment();
        a.add("A", "B");
        a.add("C", "D");
        
        //the following should be removed
        a.add("E", "F");        
        a.add("G" ,"H");        
        
        a.add("G" ,"I");
        a.add("Z" ,"F");
        
        Alignment x = NtoMCorrespondenceFilter.filter(a);

        assertEquals(2, x.size());
        assertTrue(x.contains(new Correspondence("A", "B")));
        assertTrue(x.contains(new Correspondence("C", "D")));
    }
    
}
