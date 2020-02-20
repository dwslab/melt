package de.uni_mannheim.informatik.dws.melt.matching_eval.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CounterTest {

    @Test
    void testCounter(){        
        Counter c = new Counter();
        c.add(Arrays.asList("one", "one", "two", "two", "two", "three"));
        List<Entry<String, Integer>> mostCommon = c.mostCommon();
        assertEquals(3, mostCommon.get(0).getValue());
        assertEquals("two", mostCommon.get(0).getKey());
        
        //should also work, even if n is larger than count
        List<Entry<String, Integer>> mostCommonTwo = c.mostCommon(15);
        assertEquals(3, mostCommonTwo.get(0).getValue());
        assertEquals("two", mostCommonTwo.get(0).getKey());
        
        c.add("two");
        assertEquals(4, c.getCount("two"));
        //System.out.println(mostCommonTwo.toString());
    }
    
}
