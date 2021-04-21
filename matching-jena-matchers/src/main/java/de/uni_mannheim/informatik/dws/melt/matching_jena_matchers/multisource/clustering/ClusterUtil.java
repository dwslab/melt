package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.clustering;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for adding and filtering correspondences based on cluster assignments.
 */
public class ClusterUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterUtil.class);
    
    /**
     * This function adds correspondences from the alignment where the source and target are in the same cluster.
     * It needs the mapping URI to set of cluster ids.
     * The provided alignment is not modified.
     * @param <T> the type of the cluster ID
     * @param alignment the alignment
     * @param uriToClusterId the mapping from uri to set of cluster ids
     * @return the alignment with added correspondences
     */
    public static <T> Alignment addCorrespondencesMultiCluster(Alignment alignment, Map<String, Set<T>> uriToClusterId){
        Alignment newAlignment = new Alignment(alignment, true);
        //first build a map from cluster to set of same resources
        Map<T, Set<String>> clusterIdToElements = new HashMap<>();
        for(Map.Entry<String, Set<T>> entry : uriToClusterId.entrySet()){
            for(T key : entry.getValue()){
                clusterIdToElements.computeIfAbsent(key, __-> new HashSet<>()).add(entry.getKey());
            }
        }
        
        int numberOfAddedCorrespondences = 0;
        for(Set<String> elements : clusterIdToElements.values()){
            List<String> orderedElements = new ArrayList<>(elements);
            for(int i = 0; i < orderedElements.size() - 1; i++){
                String left = orderedElements.get(i);
                for(int j = i + 1; j < orderedElements.size(); j++){
                    String right = orderedElements.get(j);
                    
                    if(alignment.getCorrespondence(left, right, CorrespondenceRelation.EQUIVALENCE) != null)
                        continue;
                    if(alignment.getCorrespondence(right, left, CorrespondenceRelation.EQUIVALENCE) != null)
                        continue;
                    
                    numberOfAddedCorrespondences++;                    
                    newAlignment.add(left, right);
                }
            }
        }
        LOGGER.info("Added {} correspondences based on cluster", numberOfAddedCorrespondences);
        return newAlignment;
    }
    
    
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
        LOGGER.info("Removed {} correspondences based on clustering.", alignment.size() - newAlignment.size());
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
        LOGGER.info("Removed {} correspondences based on clustering.", alignment.size() - newAlignment.size());
        return newAlignment;
    }
}
