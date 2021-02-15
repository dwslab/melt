package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.clustering;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;

import java.util.Map;

public class RemoveCorrespondencesBasedOnClusterAssignments {
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
}
