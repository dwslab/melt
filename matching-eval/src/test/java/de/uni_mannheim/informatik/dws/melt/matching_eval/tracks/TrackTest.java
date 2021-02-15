package de.uni_mannheim.informatik.dws.melt.matching_eval.tracks;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class TrackTest {

    @Test
    public void testDistinctTracks(){
        List<URL> distinctOntologies = TrackRepository.Conference.V1.getDistinctOntologies();
        
        Set<String> testcaseNames = new HashSet<>();
        for(URL url : distinctOntologies){
            testcaseNames.add(getName(url));
        }
        assertEquals(7, testcaseNames.size());
        assertTrue(testcaseNames.contains("cmt"));
        assertTrue(testcaseNames.contains("iasted"));
        assertTrue(testcaseNames.contains("conference"));
        assertTrue(testcaseNames.contains("sigkdd"));
        assertTrue(testcaseNames.contains("edas"));
        assertTrue(testcaseNames.contains("ekaw"));
        assertTrue(testcaseNames.contains("confof"));
    }
    
    
    
    private String getName(URL url){
        String[] elements = url.toString().split("/");
        if(elements[elements.length - 1].startsWith("source")){
            return elements[elements.length - 2].split("-")[0];
        } else{
            return elements[elements.length - 2].split("-")[1];
        }        
    }
}