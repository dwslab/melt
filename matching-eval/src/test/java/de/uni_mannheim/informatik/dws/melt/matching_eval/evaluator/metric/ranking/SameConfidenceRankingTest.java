package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class SameConfidenceRankingTest {
    
    private static Alignment system = createSystemAlignment();            
    private static Alignment createSystemAlignment(){
        Alignment a = new Alignment();
        a.add("a", "b");
        a.add("c", "d");
        a.add("e", "f");
        a.add("g", "h");
        a.add("i", "j");
        return a;
    }
    
    private static Alignment reference = createReferenceAlignment();            
    private static Alignment createReferenceAlignment(){
        Alignment a = new Alignment();
        a.add("e", "f");
        return a;
    }
    
    @Test
    void testAlphabetically() throws Exception {
        List<Correspondence> alphabetically = new ArrayList<>();
        alphabetically.add(new Correspondence("a", "b"));
        alphabetically.add(new Correspondence("c", "d"));
        alphabetically.add(new Correspondence("e", "f"));
        alphabetically.add(new Correspondence("g", "h"));
        alphabetically.add(new Correspondence("i", "j"));       
        
        List<Correspondence> actual = SameConfidenceRanking.ALPHABETICALLY.sortAlignment(system, reference);
        assertEquals(alphabetically, actual);
    }
    
    @Test
    void testTop() throws Exception {
        List<Correspondence> alphabetically = new ArrayList<>();
        List<Correspondence> actual = SameConfidenceRanking.TOP.sortAlignment(system, reference);
        assertEquals(system.size(), actual.size());
        assertEquals(new Correspondence("e", "f"), actual.get(0));//at the top because it is in the reference alignment
    }
    
    @Test
    void testBottom() throws Exception {
        List<Correspondence> alphabetically = new ArrayList<>();
        List<Correspondence> actual = SameConfidenceRanking.BOTTOM.sortAlignment(system, reference);
        assertEquals(system.size(), actual.size());
        assertEquals(new Correspondence("e", "f"), actual.get(actual.size() - 1));//at the bottom because it is in the reference alignment
    }
    
    @Test
    void testMultipleConfidences() throws Exception {
        Alignment system = new Alignment();
        system.add("a", "b", 0.9);
        system.add("c", "d", 0.9);
        system.add("e", "f", 0.8);
        system.add("g", "h", 1.0);
        system.add("i", "j", 0.1);
        
        Alignment reference = new Alignment();
        reference.add("c", "d");
        
        
        List<Correspondence> alphabetically = new ArrayList<>();
        alphabetically.add(new Correspondence("g", "h"));
        alphabetically.add(new Correspondence("a", "b"));
        alphabetically.add(new Correspondence("c", "d"));
        alphabetically.add(new Correspondence("e", "f"));
        alphabetically.add(new Correspondence("i", "j"));       
        List<Correspondence> actual = SameConfidenceRanking.ALPHABETICALLY.sortAlignment(system, reference);        
        assertEquals(alphabetically, actual);
        
        
        List<Correspondence> top = new ArrayList<>();
        top.add(new Correspondence("g", "h"));
        top.add(new Correspondence("c", "d"));//this is now first because top
        top.add(new Correspondence("a", "b"));        
        top.add(new Correspondence("e", "f"));
        top.add(new Correspondence("i", "j"));       
        actual = SameConfidenceRanking.TOP.sortAlignment(system, reference);        
        assertEquals(top, actual);
        
        actual = SameConfidenceRanking.BOTTOM.sortAlignment(system, reference);   
        assertEquals(alphabetically, actual); // should be the same in this scenario        
    }
    
    //random test see: https://dilbert.com/strip/2001-10-25
    
}
