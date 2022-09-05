package de.uni_mannheim.informatik.dws.melt.matching_owlapi_matchers.logmap;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.Set;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

/**
 * Converts Set of MappingObjectStr to Alignment.
 */
public class LogmapMapping2Alignment { //extends AbstractTypeTransformer<Set<MappingObjectStr>, Alignment>{

    /*
    public Alignment2LogmapMapping() {
        super(Set<MappingObjectStr>.class, Alignment.class);
    }
    
    @Override
    public Alignment transform(Set<MappingObjectStr> value, Properties parameters) throws TypeTransformationException {
        
        return transformAlignment(value);
    }
    */
    
    /**
     * This function is for filtering possibilities. It uses the correspondences from the oldAlignment (and thus keeps the correspondences extensions).
     * @param subsetOldAlignment this should be a subset of the oldAlignment.
     * @param oldAlignment the old alignment with the correspondence extensions which should be used.
     * @return the transformed alignment
     */
    public static Alignment transformAlignment(Set<MappingObjectStr> subsetOldAlignment, Alignment oldAlignment){
        
        Alignment alignment = new Alignment(oldAlignment, false);
        for(MappingObjectStr mapping : subsetOldAlignment){
            Correspondence correspondence = oldAlignment.getCorrespondence(
                    mapping.getIRIStrEnt1(), 
                    mapping.getIRIStrEnt2(), 
                    transformRelation(mapping.getMappingDirection())
            );
            if(correspondence != null){
                alignment.add(correspondence);
            }else{
                alignment.add(new Correspondence(
                        mapping.getIRIStrEnt1(), 
                        mapping.getIRIStrEnt2(), 
                        mapping.getConfidence(), 
                        transformRelation(mapping.getMappingDirection())
                ));
            }
        }
        return alignment;
    }
     
    public static Alignment transformAlignment(Set<MappingObjectStr> mappings){
        Alignment alignment = new Alignment();
        for(MappingObjectStr mapping : mappings){
            alignment.add(new Correspondence(
                mapping.getIRIStrEnt1(), 
                mapping.getIRIStrEnt2(), 
                mapping.getConfidence(), 
                transformRelation(mapping.getMappingDirection())
            ));
        }
        return alignment;
    }
    
    private static CorrespondenceRelation transformRelation(int rel){
        switch(rel){
            case MappingObjectStr.EQ:
                return CorrespondenceRelation.EQUIVALENCE;
            case MappingObjectStr.SUP:
                return CorrespondenceRelation.SUBSUMED; //not sure here...
            case MappingObjectStr.SUB:
                return CorrespondenceRelation.SUBSUME; //not sure here...
            default:
                return CorrespondenceRelation.EQUIVALENCE; // do not have any other representations....
        }
    }
}
