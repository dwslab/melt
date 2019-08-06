package de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class AlignmentSortTest {
        
    @Test
    public void testNumberOfCells(){
        
        Alignment a = AlignmentGenerator.generateRandomAlignment(100000, true, true, false, false);

        long startTime = System.nanoTime();
        List<Correspondence> correspondences = a.getConfidenceOrderedMapping();
        System.out.println((System.nanoTime() - startTime)/1000000f);
        
        List<Correspondence> sorted = new ArrayList(correspondences);
        sorted.sort(new CorrespondenceConfidenceComparator());
        assertTrue(sorted.equals(correspondences), "Method getConfidenceOrderedMapping in Alignment returns a unsorted list.");
    }
    
}
