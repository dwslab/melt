package de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi;

import org.junit.jupiter.api.Test;
import java.util.Iterator;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

public class AlignmentTest {

    @Test
    public void testEmpty(){
        Alignment m = new Alignment();
        Iterator i = m.iterator();
        assertFalse(i.hasNext(), "Has next");
        assertFalse(i.hasNext(), "Has next");
    }
    
    @Test
    public void testEmptyException(){
        Alignment m = new Alignment();
        assertThrows(NoSuchElementException.class, () -> {m.iterator().next();});
    }

    @Test
    public void testOne(){
        Alignment m = new Alignment();
        m.add("a", "b");
        Iterator i = m.iterator();
        assertTrue(i.hasNext(), "Has next");
        assertNotNull(i.next(), "check value");
        assertFalse(i.hasNext(), "Has next");
    }
    
    @Test
    public void testThree(){
        Alignment m = new Alignment();
        m.add("a", "b");
        m.add("c", "d");
        m.add("e", "f");
        
        
        Iterator i = m.iterator();
        assertTrue(i.hasNext(),"Has next");
        assertTrue(i.hasNext(), "Has next");
        assertTrue(i.hasNext(), "Has next");
        
        assertTrue(i.hasNext(), "Has next");
        assertNotNull(i.next(), "check value");
        
        assertTrue(i.hasNext(), "Has next");
        assertNotNull(i.next(), "check value");
        
        assertTrue(i.hasNext(), "Has next");
        assertNotNull(i.next(), "check value");
        
        assertFalse(i.hasNext(), "Has next");
    }
    
    
    @Test
    public void testAddingMultipleSameCorrespondences(){
        Alignment m = new Alignment();
        m.add(new Correspondence("one", "two", 0.5));
        m.add(new Correspondence("one", "two", 0.7));
        m.add("one", "two", 0.8);
        m.add("one", "two", 0.9, CorrespondenceRelation.EQUIVALENCE);
        
        assertEquals(1, m.size());
        
        Correspondence c = m.getCorrespondence("one", "two", CorrespondenceRelation.EQUIVALENCE);
        assertEquals(0.5, c.getConfidence(), 0.0); // first element is added and the rest not because an element is already contained
    }

    @Test
    public void getCellsSource(){
        Alignment m = new Alignment();
        assertFalse(m.getCorrespondencesSource("shouldNotFindSourceInEmptyMapping").iterator().hasNext());
    }

    @Test
    void intersection() {
        Alignment alignment_1 = new Alignment();
        alignment_1.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_1.add("http://www.left.com/e2", "http://www.right.com/e2");

        Alignment alignment_2 = new Alignment();
        alignment_2.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_2.add("http://www.left.com/e3", "http://www.right.com/e3");

        Alignment result = Alignment.intersection(alignment_1, alignment_2);
        assertEquals(1, result.size(), "Wrong size. Intersection size should be 1.");
        assertTrue(result.contains(new Correspondence("http://www.left.com/e1", "http://www.right.com/e1")), "Intersecting correspondence not contained in result.");
    }

    @Test
    void union() {
        Alignment alignment_1 = new Alignment();
        alignment_1.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_1.add("http://www.left.com/e2", "http://www.right.com/e2");

        Alignment alignment_2 = new Alignment();
        alignment_2.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_2.add("http://www.left.com/e3", "http://www.right.com/e3");

        Alignment result = Alignment.union(alignment_1, alignment_2);
        assertEquals(3, result.size(), "Wrong size. Intersection size should be 3.");
        assertTrue(result.contains(new Correspondence("http://www.left.com/e1", "http://www.right.com/e1")), "Union correspondence not contained in result.");
        assertTrue(result.contains(new Correspondence("http://www.left.com/e2", "http://www.right.com/e2")), "Union correspondence not contained in result.");
        assertTrue(result.contains(new Correspondence("http://www.left.com/e3", "http://www.right.com/e3")), "Union correspondence not contained in result.");
    }


}
