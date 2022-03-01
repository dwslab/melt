package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel;

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
    
    
    @Test
    void testSameConfidence() throws IOException {
        
        Alignment m = new Alignment();
        
        m.add("1", "2", 0.8);
        m.add("3", "4", 0.8);
        m.add("5", "6", 0.8);
        
        m = ScaleConfidence.scale(m);

        assertEquals(0.8, m.getCorrespondencesSource("1").iterator().next().getConfidence());
        assertEquals(0.8, m.getCorrespondencesSource("3").iterator().next().getConfidence());
        assertEquals(0.8, m.getCorrespondencesSource("5").iterator().next().getConfidence());
        
        m = ScaleConfidence.scale(m, 2, 3);

        assertEquals(2.8, m.getCorrespondencesSource("1").iterator().next().getConfidence() );
        assertEquals(2.8, m.getCorrespondencesSource("3").iterator().next().getConfidence());
        assertEquals(2.8, m.getCorrespondencesSource("5").iterator().next().getConfidence());
    }
    
    
    @Test
    void scaleArrayTest() throws IOException {
        double[] array = new double[]{ 0.5, 0.6, 0.7 };        
        array = ScaleConfidence.scaleArray(array);
        assertEquals(0.0, array[0]);
        assertEquals(0.5, array[1]);
        assertEquals(1.0, array[2]);
        
        array = new double[]{ 0.8, 0.8, 0.8 };        
        array = ScaleConfidence.scaleArray(array);
        assertEquals(0.8, array[0]);
        assertEquals(0.8, array[1]);
        assertEquals(0.8, array[2]);
        
        array = ScaleConfidence.scaleArray(array, 2.0, 3.0);
        assertEquals(2.8, array[0]);
        assertEquals(2.8, array[1]);
        assertEquals(2.8, array[2]);
    }
}
