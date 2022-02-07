package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * The result of a hierarchical clustering containing the tree and height information.
 * The tree information is the same as in the <a href="https://docs.scipy.org/doc/scipy/reference/generated/scipy.cluster.hierarchy.linkage.html">scipy linkage function</a>.
 * In the i-th iteration, clusters with indices tree[i, 0] and tree[i, 1] are combined to form a cluster with index n+i.
 * A cluster with an index less than n, corresponds to one of the original observations.
 * The distance between clusters tree[i, 0] and tree[i, 1] is given by height[i].
 * Equals and hash are overriden in such a way, that tree[i, 0] and tree[i, 1] may be switched but the order (i) need to be the same.
 */
public class ClusterResult {
    
    private final int[][] tree;
    private final double[] height;

    public ClusterResult(int[][] tree, double[] height) {
        //check parameters:
        if(tree == null || height == null)
            throw new IllegalArgumentException("tree or height is null.");
        if(tree.length != height.length)
            throw new IllegalArgumentException("tree and height do not have the same length.");
        for(int[] treeMerge : tree){
            if(treeMerge.length != 2)
                throw new IllegalArgumentException("tree contains not exactly two clusters.");
        }
        this.tree = tree;
        this.height = height;
    }

    public int[][] getTree() {
        return tree;
    }

    public double[] getHeight() {
        return height;
    }

    
    @Override
    public int hashCode() {
        int hash = 7;
        if(this.tree != null){
            for (int[] element : this.tree) {
                hash = 31 * hash + Arrays.stream(element).sum();
            }
        }
        if(this.height != null){
            for (double element : this.height) {
                //hash = 79 * hash + Double.hashCode(new BigDecimal(element).setScale(5, RoundingMode.HALF_UP).doubleValue());
                hash = 79 * hash + Double.hashCode(new BigDecimal(element).setScale(4, RoundingMode.HALF_UP).doubleValue());
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClusterResult other = (ClusterResult) obj;

        if (!equalsTree(this.tree, other.tree)) {
            return false;
        }
        
        if (!equalsHeight(this.height, other.height, 0.0000001)) {
            return false;
        }
        return true;
    }
    
    private static boolean equalsTree(int[][] a1, int[][] a2){
        if (a1 == a2)
            return true;
        if (a1 == null || a2==null)
            return false;
        int length = a1.length;
        if (a2.length != length)
            return false;
        
        for (int i = 0; i < length; i++) {
            int[] e1 = a1[i];
            int[] e2 = a2[i];
            if (e1 == e2)
                continue;
            
            if (e1 == null)
                return false;
            
            if (!equalsTreeElement(e1, e2))
                return false;
        }
        return true;
    }
    
    private static boolean equalsTreeElement(int[] a1, int[] a2){
        if (a1 == a2)
            return true;
        if (a1 == null || a2==null)
            return false;
        if (a1.length != 2)
            return false;
        if (a2.length != 2)
            return false;
        return (a1[0] == a2[0] && a1[1] == a2[1]) ||
                (a1[0] == a2[1] && a1[1] == a2[0]);
    }
    
    private static boolean equalsHeight(double[] a, double[] a2, double eps){
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (Math.abs(a[i]-a2[i]) > eps)
                return false;

        return true;
    }
    
}
