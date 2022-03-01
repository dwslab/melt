package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ScaleConfidence;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The information of how KGs should be merged together (in which order).
 * The main info is contained in the tree as int[][].
 * The data structure is the same as in <a href="https://docs.scipy.org/doc/scipy/reference/generated/scipy.cluster.hierarchy.linkage.html"> the result of the scipy linkage function</a>
 * and <a href="http://haifengl.github.io/api/java/smile/clustering/HierarchicalClustering.html#getTree--">the hierarchical clustering of the smile library</a>.
 * In the i-th iteration, clusters with indices tree[i, 0] and tree[i, 1] are combined to form a cluster with index n+i.
 * A cluster with an index less than n, corresponds to one of the original observations.
 * The distance between clusters tree[i, 0] and tree[i, 1] is given by distance[i].
 * Equals and hash are overriden in such a way, that tree[i, 0] and tree[i, 1] may be switched but the order (i) need to be the same.
 */
public class MergeOrder implements Serializable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MergeOrder.class);    
    private static final long serialVersionUID = 1L;
    
    private final int numberOfExamples;
    private final int[][] tree;
    private final double[] distances; 
    private final int height;
    private List<String> labels;

    public MergeOrder(int[][] tree) {
        this(tree, getDefaultDistances(tree));
    }
    
    public MergeOrder(int[][] tree, List<String> labels) {
        this(tree, getDefaultDistances(tree), labels);
    }
    
    public MergeOrder(int[][] tree, double[] distances) {
        this(tree, distances, IntStream.range(0, tree.length + 1).mapToObj(i-> Integer.toString(i)).collect(Collectors.toList()));
    }
    
    public MergeOrder(int[][] tree, double[] distances, List<String> labels) {
        //check parameters:
        if(tree == null || distances == null || labels == null)
            throw new IllegalArgumentException("tree, height, or labels is null.");
        if(tree.length != distances.length)
            throw new IllegalArgumentException("tree and height do not have the same length.");
        for(int[] treeMerge : tree){
            if(treeMerge.length != 2)
                throw new IllegalArgumentException("tree contains not exactly two clusters.");
        }
        this.numberOfExamples = tree.length + 1;
        if(labels.size() != this.numberOfExamples)
            throw new IllegalArgumentException("the number of labels is wrong.");
        this.tree = tree;
        this.distances = distances;
        this.labels = labels;
        this.height = MergeTreeNode.fromMatrix(tree).getHeight();
    }
    
    private static double[] getDefaultDistances(int[][] tree){
        double[] h = new double[tree.length];
        Arrays.fill(h, 1.0);
        return h;
    }

    /**
     * Returns the merge tree as int[][].
     * The data structure is the same as in <a href="https://docs.scipy.org/doc/scipy/reference/generated/scipy.cluster.hierarchy.linkage.html"> the result of the scipy linkage function</a>
     * and <a href="http://haifengl.github.io/api/java/smile/clustering/HierarchicalClustering.html#getTree--">the hierarchical clustering of the smile library</a>.
     * In the i-th iteration, clusters with indices tree[i, 0] and tree[i, 1] are combined to form a cluster with index n+i.
     * A cluster with an index less than n, corresponds to one of the original observations.
     * The distance between clusters tree[i, 0] and tree[i, 1] is given by distance[i].
     * @return the merge tree as int[][]
     */
    public int[][] getTree() {
        return tree;
    }

    /**
     * Return the distances between the clusters.
     * @return the distances between the clusters
     */
    public double[] getDistances() {
        return distances;
    }
    
    /**
     * Return the normalized distances between the clusters.
     * @return the normalized distances between the clusters
     */
    public double[] getDistancesNormalized() {
        return ScaleConfidence.scaleArray(distances);
    }
    
    /**
     * Returns true if the height variable is set to some non default value.
     * Returns false if the values are all set to the default of 1.0.
     * @return true if the height variable is set to some non default value, false otherwiese.
     */
    public boolean isDistancesSet(){
        for(double h : distances){
            if(h!= 1.0)
                return true;
        }
        return false;
    }
    
    /**
     * Return the height of the merge tree.
     * @return the height of the tree
     */
    public int getHeight(){
        return this.height;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }    
    
    @Override
    public int hashCode() {
        int hash = 7;
        if(this.tree != null){
            for (int[] element : this.tree) {
                hash = 31 * hash + Arrays.stream(element).sum();
            }
        }
        if(this.distances != null){
            for (double element : this.distances) {
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
        final MergeOrder other = (MergeOrder) obj;

        if (!equalsTree(this.tree, other.tree)) {
            return false;
        }
        
        if (!equalsDistances(this.distances, other.distances, 0.0000001)) {
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
    
    private static boolean equalsDistances(double[] a, double[] a2, double eps){
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
    
    /**
     * Serializes this object to the given file.
     * @param file the file where this objetc should be serialized to
     * @return true if everything worked, false if exception occured (which is logged)
     */
    public boolean serializeToFile(File file){
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))){
            out.writeObject(this);
            return true;
        } catch (IOException ex) {
            LOGGER.error("Could not save the instance of FileCache to file.", ex);
            return false;
        }
    }
    
    public static MergeOrder loadFromFile(File file){
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
            return (MergeOrder) ois.readObject();
        } catch (Exception ex) {
            LOGGER.error("Could not load the instance from file. Call the suplier to get a new one.", ex);
            return null;
        }
    }
    
    /**
     * Write the tree to file. This format is human readable and can't be used for serialization.
     * The format looks like
     * <pre>
     * 79998
     * ├── 79855
     * │   ├── 78859
     * │   │   ├── 73335
     * │   │   │   ├── 49126
     * │   │   │   │   ├── 267534~en~tauschrausch.nt
     * │   │   │   │   └── 972074~en~studieloopbaan.nt
     * │   │   │   └── 72061
     * │   │   │       ├── 1056364~en~atlan.nt
     * │   │   │       └── 69495
     * </pre>
     * @param file the destination
     */
    public void writeToFile(File file){
        TreeNode root = getJTree();
        try(PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)))){
            printTreeNode(root, printWriter, "", "");
        } catch (FileNotFoundException ex) {
            LOGGER.warn("File could not be found. Do not write ClusterResult to file.", ex);
        }
    }
    
    private void printTreeNode(TreeNode node, PrintWriter writer, String prefix, String childrenPrefix) {
        writer.print(prefix);
        writer.println(node.toString());        
        for(int i=0; i < node.getChildCount(); i++){
            TreeNode next = node.getChildAt(i);            
            if(i != node.getChildCount() - 1){
                printTreeNode(next, writer, childrenPrefix + "├── ", childrenPrefix + "│   ");
            }else{
                printTreeNode(next, writer, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }
    
    
    public String getLabel(int pos){
        if(pos < this.numberOfExamples){
            return this.labels.get(pos);
        }
        return Integer.toString(pos);
    }
    
    /**
     * Displays the tree in a JFrame to interatcively look at the tree.
     */
    public void displayTree(){
        TreeNode root = getJTree();

        JFrame f = new JFrame();
        f.setSize(new Dimension(1000, 1000));
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setTitle("Tree visualization"); 
        f.getContentPane().add(new JScrollPane(new JTree(root)));
        f.setVisible(true);
    }
    
    
    //public void displayDendrogram(){
    //    import smile.plot.swing.Dendrogram;
    //    JFrame f = new JFrame();
    //    f.setSize(new Dimension(1000, 1000));
    //    f.setLocationRelativeTo(null);
    //    f.getContentPane().add(new Dendrogram(this.tree, this.distances).canvas().panel());
    //    f.setVisible(true);
    //}
    
    
    protected TreeNode getJTree(){
        Map<Integer, DefaultMutableTreeNode> uiTree = new HashMap<>();
        for(int i=0; i < this.tree.length; i++){
            // with distance
            //final String s = String.format(" dist: %.2f norm_dist: %.2f",this.distances[i], this.getDistancesNormalized()[i]);
            //DefaultMutableTreeNode parent = uiTree.computeIfAbsent(this.numberOfExamples + i, x->new DefaultMutableTreeNode(Integer.toString(x) + s));
            
            //without distance
            DefaultMutableTreeNode parent = uiTree.computeIfAbsent(this.numberOfExamples + i, x->new DefaultMutableTreeNode(Integer.toString(x)));
            DefaultMutableTreeNode childOne = uiTree.computeIfAbsent(this.tree[i][0], pos->new DefaultMutableTreeNode(getLabel(pos)));
            DefaultMutableTreeNode childTwo = uiTree.computeIfAbsent(this.tree[i][1], pos->new DefaultMutableTreeNode(getLabel(pos)));
            parent.add(childOne);
            parent.add(childTwo);
        }
        DefaultMutableTreeNode root = uiTree.get(this.numberOfExamples + (this.tree.length - 1));
        return root;
    }
    
    /**
     * Returns a list of numbers which represents the count of merges which can performed in this step.
     * @return a list of numbers
     */
    public List<Integer> getCountOfParallelExecutions(){
        List<Integer> resultList = new ArrayList<>();        
        int n = this.tree.length + 1;
        List<MergeTaskPos> merges = new ArrayList<>();
        double[] distancesNormalized = this.getDistancesNormalized();
        for(int i=0; i < this.tree.length; i++){
            int[] mergePair = this.tree[i];
            if(mergePair.length < 2)
                throw new IllegalArgumentException("Merge tree is not valid. In row " + i + " less than two elements appear: " + Arrays.toString(mergePair));
            merges.add(new MergeTaskPos(mergePair[0], mergePair[1], n + i, this.distances[i], distancesNormalized[i]));
        }
        List<Boolean> mergedModels = new ArrayList<>();//Collections.nCopies(n + (n -1), false)
        for(int i=0; i < n; i++){
            mergedModels.add(true);
        }
        for(int i=0; i < n - 1; i++){
            mergedModels.add(false);
        }
        
        while(!merges.isEmpty()){
            List<MergeTaskPos> runnable = new ArrayList<>();
            for(MergeTaskPos task : merges){
                Boolean one = mergedModels.get(task.getClusterOnePos());
                Boolean two = mergedModels.get(task.getClusterTwoPos());
                if(one && two){
                    runnable.add(task);
                }
            }
            merges.removeAll(runnable);
            resultList.add(runnable.size());
            for(MergeTaskPos task : runnable){
                mergedModels.set(task.getClusterResultPos(), true);
            }
        }
        return resultList;
    }
    
    
}
