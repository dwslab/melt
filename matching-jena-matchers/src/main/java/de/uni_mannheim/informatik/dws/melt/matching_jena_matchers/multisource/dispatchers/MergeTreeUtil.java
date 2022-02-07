package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge.ClusterNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for a merge tree represented by an int[][].
 * The datat structure is described at <a href="http://haifengl.github.io/api/java/smile/clustering/HierarchicalClustering.html#getTree--">smile</a> like that:
 * An n-1 by 2 matrix of which row i describes the merging of clusters at step i of the clustering. If an element j in the row is less than n, 
 * then observation j was merged at this stage. If j ≥ n then the merge was with the cluster formed at the (earlier) stage j-n of the algorithm.
 */
public class MergeTreeUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MergeTreeUtil.class);
    
    /**
     * Return the height of the merge tree.
     * @param mergeTree the merge tree to compute the height for
     * @return the height of the tree
     */
    public static int getHeight(int[][] mergeTree){
        return ClusterNode.fromMatrix(mergeTree).getHeight();
    }
    
    /**
     * Returns a list of number which represents the count of merges which can performed in this step.
     * @param mergeTree the merge tree to compute the height for
     * @return a list of number which represents the count of merges which can performed in this step
     */
    public static List<Integer> getCountOfParallelExecutions(int[][] mergeTree){
        List<Integer> resultList = new ArrayList<>();        
        int n = mergeTree.length + 1;
        List<MergeTaskPos> merges = new ArrayList<>();
        for(int i=0; i < mergeTree.length; i++){
            int[] mergePair = mergeTree[i];
            if(mergePair.length < 2)
                throw new IllegalArgumentException("Merge tree is not valid. In row " + i + " less than two elements appear: " + Arrays.toString(mergePair));
            merges.add(new MergeTaskPos(mergePair[0], mergePair[1], n + i));
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
    
    /**
     * Logs the result of {@link #getCountOfParallelExecutions(int[][]) } to the info level.
     * @param mergeTree the merge tree for which the information is logged.
     */
    public static void logCountOfParallelExecutions(int[][] mergeTree){
        LOGGER.info("List of number of task which can run in parallel at each step: {}", getCountOfParallelExecutions(mergeTree));
    }
}
