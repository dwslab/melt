package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * This class is a utility class which represents the merge tree is a different way
 * (for which it is easier to calculate the height of the tree).
 */
public class MergeTreeNode {
    
    private int id;
    private MergeTreeNode left;
    private MergeTreeNode right;
    
    private double distance;

    public MergeTreeNode(int id, MergeTreeNode left, MergeTreeNode right, double distance) {
        this.id = id;
        this.left = left;
        this.right = right;
        this.distance = distance;
    }

    public MergeTreeNode(int id, MergeTreeNode left, MergeTreeNode right) {
        this(id, left, right, 0.0d);
    }
    
    public MergeTreeNode(int id) {
        this(id, null, null, 0.0d);
    }
    
    /*
    public int getHeight(){
        //https://www.techiedelight.com/calculate-height-binary-tree-iterative-recursive/
        int leftHeight = this.left == null ? 0 : this.left.getHeight();
        int rightHeight = this.right == null ? 0 : this.right.getHeight();
        return Math.max(leftHeight, rightHeight) + 1;
    }
    */
    public int getHeight(){
        //https://www.geeksforgeeks.org/iterative-method-to-find-height-of-binary-tree/
        Queue<MergeTreeNode> q = new LinkedList<>();
        q.add(this);
        int height = 0;
  
        while (!q.isEmpty()){
            int nodeCount = q.size();
            while (nodeCount > 0){
                MergeTreeNode newnode = q.poll();
                if (newnode.left != null)
                    q.add(newnode.left);
                if (newnode.right != null)
                    q.add(newnode.right);
                nodeCount--;
            }
            height++;
        }
        return height;
    }
    
    
    
    
    public static MergeTreeNode fromMatrix(int[][] clusterMatrix){
        return fromMatrix(clusterMatrix, null);
    }
    
    public static MergeTreeNode fromMatrix(int[][] clusterMatrix, double[] height){
        //https://github.com/scipy/scipy/blob/v1.7.1/scipy/cluster/hierarchy.py#L1400-L1496
        int n = clusterMatrix.length + 1;
        
        List<MergeTreeNode> nodes = new ArrayList<>();
        
        for(int i = 0; i < n; i++){
            nodes.add(new MergeTreeNode(i));
        }
        if(height != null){
            if(clusterMatrix.length != height.length){
                throw new IllegalArgumentException("clusterMatrix and height does not have the same size. Please fix.");
            }
        }
        MergeTreeNode nd = null;
        for(int i=0; i < clusterMatrix.length; i++){
            int[] array = clusterMatrix[i];
            if(array.length < 2 )
                throw new IllegalArgumentException("Cluster matrix is not valid because size of inner array is less than two.");
            int fi = array[0];
            int fj  = array[1];
            
            if(fi > i + n)
                throw new IllegalArgumentException("Corrupt cluster matrix. Index to cluster is used before it is formed. See row "  + i + " column 0");
            if(fj > i + n)
                throw new IllegalArgumentException("Corrupt cluster matrix. Index to cluster is used before it is formed. See row "  + i + " column 1");
            
            if(height == null){
                nd = new MergeTreeNode(i+n, nodes.get(fi), nodes.get(fj));
            }else{
                nd = new MergeTreeNode(i+n, nodes.get(fi), nodes.get(fj), height[i]);
            }
            nodes.add(nd);
        }
        return nd;
    }
    
}
