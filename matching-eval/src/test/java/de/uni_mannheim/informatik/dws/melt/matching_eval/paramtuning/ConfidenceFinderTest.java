package de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class ConfidenceFinderTest {
    
    @Test
    public void checkGetAllPossibleSteps(){
        Alignment a = new Alignment();
        
        Set<Double> reference = new HashSet<>();
        
        BigDecimal begin = BigDecimal.ZERO;
        BigDecimal end = BigDecimal.valueOf(1.0);
        BigDecimal step = BigDecimal.valueOf(0.001);
        for (BigDecimal i = begin; i.compareTo(end) < 0; i = i.add(step)){
            double d = i.doubleValue();
             a.add("http://one.com/" + d, "http://two.com/" + d, d);
             reference.add(d);
        }
        
        //Set<Double> confs = ConfidenceFinder.getOccurringConfidences(a);
        //assertSame(confs, reference);
        
        //Set<Double> confs = ConfidenceFinder.getOccurringConfidences(a,1);
        //assertSame(confs, new HashSet<>(Arrays.asList(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0)));
        //TODO: make assertion
    }
}
