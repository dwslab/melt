package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


public class ConfidenceFilterTest {
    
    @Test
    void testFilter() throws IOException {
        
        Alignment m = new Alignment(true, true, true, true);
        
        for(double conf=0.1;conf<=1.0;conf+=0.1){
            for(int i=0;i<10;i++){
                m.add(
                        "http://exampleLeftWithALongURI/" + Double.toString(conf) + "_" +  Integer.toString(i), 
                        "http://exampleRightWithALongURI/" + Double.toString(conf) + "_" +  Integer.toString(i), 
                        conf);
            }
        }
        Alignment filtered = new ConfidenceFilter().filter(m, null, null);
        assertEquals(10, filtered.size());
    }
    
}
