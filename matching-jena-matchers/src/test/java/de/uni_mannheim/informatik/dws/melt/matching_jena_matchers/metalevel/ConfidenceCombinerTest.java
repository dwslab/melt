package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ConfidenceCombinerTest {
    
    @Test
    void testConfidenceCombiner() {
        
        Correspondence a = new Correspondence("a", "a'");
        a.addAdditionalConfidence("conf1", 0.7);
        a.addAdditionalConfidence("conf2", 0.8);
        a.addAdditionalConfidence("conf3", 0.9);
        
        Correspondence b = new Correspondence("b", "b'");
        b.addAdditionalConfidence("conf1", 0.1);
        b.addAdditionalConfidence("conf2", 0.5);
        b.addAdditionalConfidence("conf3", 1.2);
        
        Alignment alignment = new Alignment(Arrays.asList(a, b));
        
        //use only the first two confidences
        ConfidenceCombiner combiner = new ConfidenceCombiner(new HashSet(Arrays.asList("conf1", "conf2")));
        Alignment combinedAlignment = combiner.combine(alignment);
        assertEquals(0.75, combinedAlignment.getCorrespondence("a", "a'", CorrespondenceRelation.EQUIVALENCE).getConfidence());
        assertEquals(0.3, combinedAlignment.getCorrespondence("b", "b'", CorrespondenceRelation.EQUIVALENCE).getConfidence());
        
        //use all confidence with mean
        combiner = new ConfidenceCombiner(); 
        combinedAlignment = combiner.combine(alignment);
        assertEquals(0.8, combinedAlignment.getCorrespondence("a", "a'", CorrespondenceRelation.EQUIVALENCE).getConfidence());
        assertEquals(0.6, combinedAlignment.getCorrespondence("b", "b'", CorrespondenceRelation.EQUIVALENCE).getConfidence());
        
        //use all confidence with max
        combiner = new ConfidenceCombiner(new Max()); 
        combinedAlignment = combiner.combine(alignment);
        assertEquals(0.9, combinedAlignment.getCorrespondence("a", "a'", CorrespondenceRelation.EQUIVALENCE).getConfidence());
        assertEquals(1.2, combinedAlignment.getCorrespondence("b", "b'", CorrespondenceRelation.EQUIVALENCE).getConfidence());
        
        
    }
    
    @Test
    void testEmpty(){
        Correspondence a = new Correspondence("a", "a'", 0.9);        
        Correspondence b = new Correspondence("b", "b'", 0.8);
        
        Alignment alignment = new Alignment(Arrays.asList(a, b));
        ConfidenceCombiner combiner = new ConfidenceCombiner(new HashSet(Arrays.asList("conf1", "conf2")));
        Alignment combinedAlignment = combiner.combine(alignment);
        assertEquals(0.9, combinedAlignment.getCorrespondence("a", "a'", CorrespondenceRelation.EQUIVALENCE).getConfidence());
        assertEquals(0.8, combinedAlignment.getCorrespondence("b", "b'", CorrespondenceRelation.EQUIVALENCE).getConfidence());
    }
}
