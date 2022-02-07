package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ClusterResultTest {
    @Test
    void testCtor() {
        //check null
        assertThrows(IllegalArgumentException.class, ()-> {
            double[] height = new double[]{};
            ClusterResult r = new ClusterResult(null, height);
        });
        
        assertThrows(IllegalArgumentException.class, ()-> {
            int[][] tree = new int[][]{{1,2}};
            ClusterResult r = new ClusterResult(tree, null);
        });
        
        //check different length
        assertThrows(IllegalArgumentException.class, ()-> {
            int[][] tree = new int[][]{{1,2}};
            double[] height = new double[]{};
            ClusterResult r = new ClusterResult(tree, height);
        });
        
        //check different merge size
        assertThrows(IllegalArgumentException.class, ()-> {
            int[][] tree = new int[][]{{1,2}, {3,5,4}};
            double[] height = new double[]{0.1, 0.2};
            ClusterResult r = new ClusterResult(tree, height);
        });
        
        int[][] tree = new int[][]{{1,2}, {3,4}};
        double[] height = new double[]{0.1, 0.2};
        ClusterResult r = new ClusterResult(tree, height);
        
        assertEquals(tree.length, r.getTree().length);
        assertEquals(height.length, r.getHeight().length);
    }
    
    @Test
    void testHashCodeAndEquals() {
 
        ClusterResult A = new ClusterResult(
                new int[][]{{1,2}, {3,4}}, 
                new double[]{0.1, 0.2});
        
        ClusterResult Asame = new ClusterResult(
                new int[][]{{1,2}, {3,4}}, 
                new double[]{0.1, 0.2});        
        assertEquals(A.hashCode(), Asame.hashCode());
        assertTrue(A.equals(Asame));
        
        ClusterResult differentOrder = new ClusterResult(
                new int[][]{{2,1}, {3,4}}, 
                new double[]{0.1, 0.2});
        assertEquals(A.hashCode(), differentOrder.hashCode());
        assertTrue(A.equals(differentOrder));
        
        ClusterResult differentHeight = new ClusterResult(
                new int[][]{{2,1}, {3,4}}, 
                new double[]{0.2, 0.3});        
        assertNotEquals(A.hashCode(), differentHeight.hashCode());
        assertFalse(A.equals(differentHeight));
        
        ClusterResult differentSize = new ClusterResult(
                new int[][]{{2,1}, {3,4}, {5,6}}, 
                new double[]{0.1, 0.2, 0.3});        
        assertNotEquals(A.hashCode(), differentSize.hashCode());
        assertFalse(A.equals(differentSize));
    }
    
}
