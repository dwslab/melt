package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ScaleConfidence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


public class ScaleConfidenceTest {
    
    @Test
    void testFilter() throws IOException {
        
        Alignment m = new Alignment();
        
        m.add("1", "2", 0.5);
        m.add("3", "4", 0.6);
        m.add("5", "6", 0.7);
        
        m = ScaleConfidence.scale(m);
        

        assertEquals(0.0, m.getCorrespondencesSource("1").iterator().next().getConfidence() );
        assertEquals(0.5, m.getCorrespondencesSource("3").iterator().next().getConfidence());
        assertEquals(1.0, m.getCorrespondencesSource("5").iterator().next().getConfidence());
    }
    
}
