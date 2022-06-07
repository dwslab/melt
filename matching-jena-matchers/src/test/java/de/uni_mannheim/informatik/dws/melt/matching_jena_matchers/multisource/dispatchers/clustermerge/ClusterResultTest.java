package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MergeOrder;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ClusterResultTest {
    @Test
    void testCtor() {
        //check null
        assertThrows(IllegalArgumentException.class, ()-> {
            double[] distances = new double[]{};
            MergeOrder r = new MergeOrder(null, distances);
        });
        
        assertThrows(IllegalArgumentException.class, ()-> {
            int[][] tree = new int[][]{{0,1}};
            MergeOrder r = new MergeOrder(tree, (double[])null);
        });
        
        //check different length
        assertThrows(IllegalArgumentException.class, ()-> {
            int[][] tree = new int[][]{{0,1}};
            double[] distances = new double[]{};
            MergeOrder r = new MergeOrder(tree, distances);
        });
        
        //check different merge size
        assertThrows(IllegalArgumentException.class, ()-> {
            int[][] tree = new int[][]{{0,1}, {2,3,4}};
            double[] distances = new double[]{0.1, 0.2};
            MergeOrder r = new MergeOrder(tree, distances);
        });
        
        int[][] tree = new int[][]{{0, 1}, {2, 3}};
        double[] distances = new double[]{0.1, 0.2};
        MergeOrder r = new MergeOrder(tree, distances);
        
        assertEquals(tree.length, r.getTree().length);
        assertEquals(distances.length, r.getDistances().length);
    }
    
    @Test
    void testHashCodeAndEquals() {
 
        MergeOrder A = new MergeOrder(
                new int[][]{{0, 1}, {2, 3}}, 
                new double[]{0.1, 0.2});
        
        MergeOrder Asame = new MergeOrder(
                new int[][]{{0, 1}, {2, 3}}, 
                new double[]{0.1, 0.2});        
        assertEquals(A.hashCode(), Asame.hashCode());
        assertTrue(A.equals(Asame));
        
        MergeOrder differentOrder = new MergeOrder(
                new int[][]{{1, 0}, {2, 3}}, 
                new double[]{0.1, 0.2});
        assertEquals(A.hashCode(), differentOrder.hashCode());
        assertTrue(A.equals(differentOrder));
        
        MergeOrder differentHeight = new MergeOrder(
                new int[][]{{1,0}, {2, 3}}, 
                new double[]{0.2, 0.3});        
        assertNotEquals(A.hashCode(), differentHeight.hashCode());
        assertFalse(A.equals(differentHeight));
        
        MergeOrder differentSize = new MergeOrder(
                new int[][]{{1, 0}, {2, 3}, {4,5}}, 
                new double[]{0.1, 0.2, 0.3});        
        assertNotEquals(A.hashCode(), differentSize.hashCode());
        assertFalse(A.equals(differentSize));
    }
    
}
