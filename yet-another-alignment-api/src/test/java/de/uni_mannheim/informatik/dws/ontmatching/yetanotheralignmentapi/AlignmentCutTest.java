package de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi;

import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AlignmentCutTest {
    private static Random randomObj = new Random(1990);
    private static Alignment generateHugeAlignmentMapping(int numberOfCorrespondences){
        Alignment m = new Alignment();
        for(int i = 0; i < numberOfCorrespondences; i++){
            int left = randomObj.nextInt(numberOfCorrespondences);
            int right = randomObj.nextInt(numberOfCorrespondences);
            m.add("http://exampleLeftWithALongURI/" + left, "http://exampleRightWithALongURI/" + right, randomObj.nextDouble());
        }     
        return m;
    }
    
    @Test
    public void testNumberOfCells(){
        
        Alignment a = generateHugeAlignmentMapping(100000);
        
        long startTime = System.nanoTime();
        Alignment cutted = a.cut(0.7);
        System.out.println(System.nanoTime() - startTime);

        for(Correspondence c : cutted){
            assertTrue(c.confidence >= 0.7);
        }
    }
    
}
