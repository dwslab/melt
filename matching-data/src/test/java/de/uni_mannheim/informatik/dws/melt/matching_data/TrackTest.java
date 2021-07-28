package de.uni_mannheim.informatik.dws.melt.matching_data;

import java.net.URL;
import java.util.ArrayList;
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
    
    
    @Test
    public void testTrackOrderedAndUnique(){
        assertEquals("cmt-conference" , TrackRepository.Conference.V1.getTestCase(0).getName());
        assertEquals("cmt-confof" , TrackRepository.Conference.V1.getTestCase(1).getName());
        assertEquals("cmt-edas" , TrackRepository.Conference.V1.getTestCase(2).getName());
        assertEquals("cmt-ekaw" , TrackRepository.Conference.V1.getTestCase(3).getName());
        assertEquals("cmt-iasted" , TrackRepository.Conference.V1.getTestCase(4).getName());
        assertEquals("cmt-sigkdd" , TrackRepository.Conference.V1.getTestCase(5).getName());
        assertEquals("conference-confof" , TrackRepository.Conference.V1.getTestCase(6).getName());
        
        //unique test cases:
        
        List<String> testCaseNames = new ArrayList<>();
        for(TestCase tc : TrackRepository.Conference.V1.getTestCases()){
            testCaseNames.add(tc.getName());
        }
        assertEquals(new HashSet<>(testCaseNames).size(), testCaseNames.size());
        
        testCaseNames = new ArrayList<>();
        for(int i = 0; i < TrackRepository.Conference.V1.getTestCases().size(); i++){
            testCaseNames.add(TrackRepository.Conference.V1.getTestCase(i).getName());
        }
        assertEquals(new HashSet<>(testCaseNames).size(), testCaseNames.size());        
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