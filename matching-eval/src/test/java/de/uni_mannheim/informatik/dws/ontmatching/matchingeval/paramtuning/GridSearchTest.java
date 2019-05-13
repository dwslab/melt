package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.paramtuning;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;

import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GridSearchTest {
    
    public static void main(String[] args){
        /*
        TestMatcher matcher = new TestMatcher();
        matcher.setThreshold(0.8);
        ExecutionResultSet s = Executor.run(tc, matcher);
        System.out.println(s);
        */
        TestCase tc = TrackRepository.Conference.V1.getTestCases().get(0);
        ExecutionResultSet r =  new GridSearch(TestMatcher.class)
                .addParameter("one", Arrays.asList("a", "b", "c"))
                .addParameter("two", Arrays.asList(1, 2, 3))
                .addParameter("threshold", Arrays.asList(0.1, 0.5, 1.0))
                .runGrid(tc);
        
        assertEquals(27, r.size());
        System.out.println(r);
    }
    //@Test
    void simpleGridSearch() throws Exception{        
        
    }
    
}
