package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cmrelation;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class CMRelation {

    private Map<Entry<CorrespondenceRelation, CorrespondenceRelation>, Alignment> cm;
    
    public CMRelation(Map<Entry<CorrespondenceRelation, CorrespondenceRelation>, Alignment> cm){
        
    }
        
    public void bla(ExecutionResult r){
        
        Map<Entry<CorrespondenceRelation, CorrespondenceRelation>, Alignment> map = new HashMap<>();
        
        Alignment referenceAlignment = r.getReferenceAlignment();
        Alignment systemAlignment = r.getSystemAlignment();
        
        for(Correspondence referenceCell : referenceAlignment){
            if(referenceCell.getRelation().equals(CorrespondenceRelation.UNKNOWN) == false){                
                boolean found = false;
                for(Correspondence systemCell : systemAlignment.getCorrespondencesSourceTarget(referenceCell.getEntityOne(), referenceCell.getEntityTwo())){
                    map.computeIfAbsent(new SimpleEntry<>(referenceCell.getRelation(), systemCell.getRelation()), __-> new Alignment()).add(systemCell);
                    found = true;
                }
                if(found == false){
                    map.computeIfAbsent(new SimpleEntry<>(referenceCell.getRelation(), CorrespondenceRelation.UNKNOWN), __-> new Alignment()).add(referenceCell);
                }
            }
        }
        
    }
    
    
    
}
