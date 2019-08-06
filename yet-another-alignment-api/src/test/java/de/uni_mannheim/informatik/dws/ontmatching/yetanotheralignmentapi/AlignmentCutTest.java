package de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AlignmentCutTest {
        
    @Test
    public void testNumberOfCells(){
        
        Alignment a = AlignmentGenerator.generateRandomAlignment(100000);
        
        long startTime = System.nanoTime();
        Alignment cutted = a.cut(0.7);
        System.out.println(System.nanoTime() - startTime);

        for(Correspondence c : cutted){
            assertTrue(c.confidence >= 0.7);
        }
    }
    
}
