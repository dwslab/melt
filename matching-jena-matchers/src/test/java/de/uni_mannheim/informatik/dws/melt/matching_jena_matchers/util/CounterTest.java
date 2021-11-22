package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CounterTest {

    @Test
    void testCounter(){        
        Counter<String> c = new Counter<>();
        c.addAll(Arrays.asList("one", "one", "two", "two", "two", "three"));
        List<Entry<String, Integer>> mostCommon = c.mostCommon();
        assertEquals(3, mostCommon.get(0).getValue());
        assertEquals("two", mostCommon.get(0).getKey());

        
        assertEquals("two", c.mostCommonElement());
        assertEquals(Arrays.asList("two"), c.mostCommonElements(1));
        assertEquals(Arrays.asList(new SimpleEntry<>("two", 0.5)), c.mostCommonByPercentage(0.5));
        assertEquals(Arrays.asList(new SimpleEntry<>("two", 0.5), new SimpleEntry<>("one", (double)2/6)), c.mostCommonByPercentage(0.3));
        
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
    void testSecondComparator(){     
        //sorty by size ascending
        Comparator<String> comp = (c1, c2) -> Integer.compare(c1.length(), c2.length());
        Counter<String> c = new Counter<>(comp);
        c.addAll(Arrays.asList("aaaaaa", "aaaaaa", "bbbbbbb", "bbbbbbb", "c", "c", "dd", "dd"));
        
        List<String> expected = Arrays.asList("c", "dd", "aaaaaa", "bbbbbbb");
        List<String> actual = c.mostCommonElements();
        assertEquals(expected, actual);
        
        //-----
        //sorty by size descending
        comp = (c1, c2) -> Integer.compare(c2.length(), c1.length());
        c = new Counter<>(comp);
        c.addAll(Arrays.asList("aaaaaa", "aaaaaa", "bbbbbbb", "bbbbbbb", "c", "c", "dd", "dd"));
        
        expected = Arrays.asList("bbbbbbb", "aaaaaa", "dd", "c");
        actual = c.mostCommonElements();
        assertEquals(expected, actual);
        
        //----------
        //sort by characters
        
        comp = (c1, c2) -> c1.compareTo(c2);
        c = new Counter<>(comp);
        c.addAll(Arrays.asList("aaaaaa", "aaaaaa", "bbbbbbb", "bbbbbbb", "c", "c", "dd", "dd"));
        
        expected = Arrays.asList("aaaaaa", "bbbbbbb", "c", "dd");
        actual = c.mostCommonElements();
        assertEquals(expected, actual);
        
        actual = c.mostCommonElementsByPercentage(0.2);
        assertEquals(expected, actual);
    }
    
    @Test
    void testEmptyCounter(){
        Counter<String> c = new Counter<>();
        assertNull(c.mostCommonElement());
        assertEquals(Arrays.asList(), c.mostCommon());
        assertEquals(Arrays.asList(), c.mostCommon(10));
        assertEquals(Arrays.asList(), c.mostCommonElements(10));
    }
    
    
    @Test
    void testZeroN(){
        Counter<String> c = new Counter<>();
        c.addAll(Arrays.asList("one", "one", "two", "two", "two", "three"));
        
        assertEquals(3,c.mostCommonElementsByPercentage(0.0).size());
        assertEquals(0,c.mostCommonElements(0).size());
        
        assertEquals(3,c.mostCommonElementsByPercentageOrTopN(0,0).size());
    }
    
    @Test
    void testExceptions(){
        Counter<String> c = new Counter<>();
        c.addAll(Arrays.asList("one", "one", "two", "two", "two", "three"));
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            c.mostCommonElements(-20);
        });
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            c.mostCommonByPercentage(-0.1);
        });
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            c.mostCommonByPercentage(2.0);
        });
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            c.mostCommonElementsByPercentageOrTopN(-0.2, -3);
        });
    }
    
    @Test
    void testAddAllCounter(){
        Counter<String> one = new Counter<>();
        one.add("hello");
        one.add("good", 2);
        
        assertEquals(3, one.getCount());
        
        Counter<String> two = new Counter<>();
        two.add("good", 4);
        assertEquals(4, two.getCount());
        two.addAll(one);
        
        assertEquals(3, one.getCount());
        assertEquals(7, two.getCount());
        
        assertEquals(6, two.getCount("good"));
        assertEquals(1, two.getCount("hello"));
    }
    
    @Test
    void testEqualityOfCounters(){
        Counter<String> one = new Counter<>();
        Counter<String> two = new Counter<>();
        
        one.add("hello", 10);
        one.add("foo", 20);
        
        two.add("hello", 10);
        two.add("foo", 20);
        
        assertEquals(one, two);
        
        one.add("day");
        two.add("hello");
        assertNotEquals(one, two);
        
        two.add("day");
        one.add("hello");
        assertEquals(one, two);
        
        
        one = new Counter<>(Comparator.comparingInt(s->s.length()));
        two = new Counter<>();
        
        one.add("hello", 10);
        one.add("foo", 20);
        
        two.add("hello", 10);
        two.add("foo", 20);
        assertNotEquals(one, two); // because of different comparator
    }
    
    
    @Test
    void testSerialization(){
        Counter<String> one = new Counter<>();
        one.add("hello", 10);
        one.add("foo", 20);
        one.add("test", 97);
        one.add("bar", 54);
        
        Counter<String> two = Counter.loadFromJsonString(one.toJson());
        assertEquals(one, two);
    }
    
    @Test
    void testSerializationBackslash(){
        Counter<String> one = new Counter<>();
        one.add("hello", 10);
        one.add("foo", 20);
        one.add("\\", 97);
        one.add("bar", 54);
        
        Counter<String> two = Counter.loadFromJsonString(one.toJson());
        assertEquals(one, two);
    }
}
