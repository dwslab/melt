package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.clustering;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;

import java.util.Map;
import java.util.Set;

/**
 * Helper calss for filtering correspondences based on cluster assignments.
 * @author shertlin
 */
public class RemoveCorrespondencesBasedOnClusterAssignments {
    
    /**
     * This function removes correspondences from the alignment where the two matched entities are not in the same cluster.
     * It needs the mapping URI to cluster id.
     * The provided alignment is not modified.
     * @param <T> the type of the cluster ID
     * @param alignment the alignemnt to filter
     * @param uriToClusterId the mapping from uri to cluster id
     * @return the filtered alignment
     */
    public static <T> Alignment removeCorrespondences(Alignment alignment, Map<String, T> uriToClusterId){
        Alignment newAlignment = new Alignment(alignment, false);
        for(Correspondence c : alignment){
            T sourceClusterId = uriToClusterId.get(c.getEntityOne());
            if(sourceClusterId == null){
                //if the clusterid is not known, then just keep the correspondence
                newAlignment.add(c);
                continue;
            }
            T targetClusterId = uriToClusterId.get(c.getEntityTwo());
            if(targetClusterId == null){
                //if the clusterid is not known, then just keep the correspondence
                newAlignment.add(c);
                continue;
            }
            
            if(sourceClusterId.equals(targetClusterId)){
                newAlignment.add(c);
            }//else we do nothing and do not add it to the resulting alignment.
        }
        return newAlignment;
    }
    
    /**
     * This function removes correspondences from the alignment where the two matched entities are not in the same cluster.
     * It needs the mapping URI to set of cluster ids.
     * The provided alignment is not modified.
     * @param <T> the type of the cluster ID
     * @param alignment the alignemnt to filter
     * @param uriToClusterId the mapping from uri to set of cluster ids
     * @return the filtered alignment
     */
    public static <T> Alignment removeCorrespondencesMultiCluster(Alignment alignment, Map<String, Set<T>> uriToClusterId){
        Alignment newAlignment = new Alignment(alignment, false);
        for(Correspondence c : alignment){
            Set<T> sourceClusterIds = uriToClusterId.get(c.getEntityOne());
            if(sourceClusterIds == null){
                //if the clusterid is not known, then just keep the correspondence
                newAlignment.add(c);
                continue;
            }
            Set<T> targetClusterIds = uriToClusterId.get(c.getEntityTwo());
            if(targetClusterIds == null){
                //if the clusterid is not known, then just keep the correspondence
                newAlignment.add(c);
                continue;
            }
            
            for(T sourceClusterId : sourceClusterIds){
                if(targetClusterIds.contains(sourceClusterId)){
                    newAlignment.add(c);
                    break;
                }
            }
        }
        return newAlignment;
    }
}
