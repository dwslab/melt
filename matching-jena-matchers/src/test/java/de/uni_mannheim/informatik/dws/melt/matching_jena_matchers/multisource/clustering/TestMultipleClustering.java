package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.clustering;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractorUrlPattern;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

@EnabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_11)
public class TestMultipleClustering {


    @Test
    public void testFamer(){

        FamerClustering c = new FamerClustering(getTestExtractor());
        Alignment actual = c.processAlignment(getTestAlignment());
        
        Alignment expected = getTestAlignment();
        expected.removeCorrespondencesSourceTarget("http://b.com/b", "http://a.com/x");

        assertEquals(expected, actual);
    } 
    
    
    @Test
    public void testErrorDegree(){
        FilterByErrorDegree c = new FilterByErrorDegree(0.6);
        Alignment actual = c.filter(getTestAlignment());
        
        Alignment expected = getTestAlignment();
        expected.removeCorrespondencesSourceTarget("http://b.com/b", "http://a.com/x");

        assertEquals(expected, actual);
    } 
    
    
    
    private Alignment getTestAlignment(){
        Alignment a = new Alignment();
        
        /*
        A --- B ---wrong---- X ----Y
         \   /                \   /
          \ /                  \ /
           C                    Z
        */
        
        //cluster 1
        a.add("http://a.com/a", "http://b.com/b", 0.9);
        a.add("http://a.com/a", "http://c.com/c", 0.9);
        a.add("http://b.com/b", "http://c.com/c", 0.9);
        //cluster 2
        a.add("http://a.com/x", "http://b.com/y", 0.9);
        a.add("http://a.com/x", "http://c.com/z", 0.9);
        a.add("http://b.com/y", "http://c.com/z", 0.9);
        
        //wrong
        a.add("http://b.com/b", "http://a.com/x", 0.9);
        
        return a;
    }
    
    
    
    private DatasetIDExtractor getTestExtractor(){
        return new DatasetIDExtractorUrlPattern("http://", ".com", s->s);
    }
}
