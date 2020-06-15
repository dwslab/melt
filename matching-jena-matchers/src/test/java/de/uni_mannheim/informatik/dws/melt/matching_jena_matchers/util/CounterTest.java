package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CounterTest {

    @Test
    void testCounter(){        
        Counter<String> c = new Counter();
        c.addAll(Arrays.asList("one", "one", "two", "two", "two", "three"));
        List<Entry<String, Integer>> mostCommon = c.mostCommon();
        assertEquals(3, mostCommon.get(0).getValue());
        assertEquals("two", mostCommon.get(0).getKey());
        
        assertEquals("two", c.mostCommonElement());
        assertEquals(Arrays.asList("two"), c.mostCommonElements(1));
        assertEquals(Arrays.asList(new SimpleEntry("two", 0.5)), c.mostCommonByPercentage(0.5));
        assertEquals(Arrays.asList(new SimpleEntry("two", 0.5), new SimpleEntry("one", (double)2/6)), c.mostCommonByPercentage(0.3));
        
        assertEquals(Arrays.asList("two"), c.mostCommonElementsByPercentage(0.5));
        assertEquals(Arrays.asList("two", "one"), c.mostCommonElementsByPercentage(0.3));
        
        
        //should also work, even if n is larger than count
        List<Entry<String, Integer>> mostCommonTwo = c.mostCommon(15);
        assertEquals(3, mostCommonTwo.get(0).getValue());
        assertEquals("two", mostCommonTwo.get(0).getKey());
        
        c.add("two");
        assertEquals(4, c.getCount("two"));
        //System.out.println(mostCommonTwo.toString());
        
        assertEquals(4, c.getCount("two"));
        
        //List<Integer> randomList = new Random().ints().limit(6000000).boxed().collect(Collectors.toList());
    }
    
    @Test
    void testEmptyCounter(){
        Counter c = new Counter();
        assertNull(c.mostCommonElement());
        assertEquals(Arrays.asList(), c.mostCommon());
        assertEquals(Arrays.asList(), c.mostCommon(10));
        assertEquals(Arrays.asList(), c.mostCommonElements(10));
    }
    
}
