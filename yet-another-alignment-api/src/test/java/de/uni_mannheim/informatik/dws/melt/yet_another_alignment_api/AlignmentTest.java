package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
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
    
    private Correspondence one = new Correspondence("http://www.left.com/e1", "http://www.right.com/e1");
    private Correspondence two = new Correspondence("http://www.left.com/e2", "http://www.right.com/e2");
    private Correspondence three = new Correspondence("http://www.left.com/e3", "http://www.right.com/e3");
    
    @Test
    void substraction() {
        Alignment alignment_1 = new Alignment();
        alignment_1.add(one);
        alignment_1.add(two);

        Alignment alignment_2 = new Alignment();
        alignment_2.add(one);
        alignment_2.add(three);

        Alignment result = Alignment.subtraction(alignment_1, alignment_2);
        assertEquals(1, result.size(), "Wrong size. Subtraction size should be 2.");
        assertTrue(result.contains(two), "Subtraction correspondence not contained in result.");
    }

    @Test
    void intersection() {
        Alignment alignment_1 = new Alignment();
        alignment_1.add(one);
        alignment_1.add(two);

        Alignment alignment_2 = new Alignment();
        alignment_2.add(one);
        alignment_2.add(three);

        Alignment result = Alignment.intersection(alignment_1, alignment_2);
        assertEquals(1, result.size(), "Wrong size. Intersection size should be 1.");
        assertTrue(result.contains(one), "Intersecting correspondence not contained in result.");
    }

    @Test
    void union() {
        Alignment alignment_1 = new Alignment();
        alignment_1.add(one);
        alignment_1.add(two);

        Alignment alignment_2 = new Alignment();
        alignment_2.add(one);
        alignment_2.add(three);

        Alignment result = Alignment.union(alignment_1, alignment_2);
        assertEquals(3, result.size(), "Wrong size. Union size should be 3.");
        assertTrue(result.containsAll(Arrays.asList(one, two, three)), "Union correspondence not contained in result.");
    }

    @Test
    void getExtension(){
        Alignment alignment_1 = new Alignment();
        alignment_1.add("http://www.left.com/e1", "http://www.right.com/e1");
        alignment_1.add("http://www.left.com/e2", "http://www.right.com/e2");
        alignment_1.addExtensionValue("http://www.test.com/myExtension_value_1", "ABC");
        alignment_1.addExtensionValue("http://www.test.com/myExtension_value_2", "DEF");
        assertEquals("ABC", alignment_1.getExtensionValue("http://www.test.com/myExtension_value_1"));
        assertEquals("DEF", alignment_1.getExtensionValue("http://www.test.com/myExtension_value_2"));
        assertEquals(2, alignment_1.getExtensions().size());
    }
    
    @Test
    void reverseTest(){
        Alignment a = new Alignment();
        a.add("one", "two", CorrespondenceRelation.HAS_INSTANCE);
        a.add("one", "three");
        
        assertEquals(1, a.getDistinctSourcesAsSet().size());
        assertEquals(2, a.getDistinctTargetsAsSet().size());
        
        Alignment aSwitched = a.reverse();
        
        assertNull(aSwitched.getCorrespondence("one", "two", CorrespondenceRelation.HAS_INSTANCE));
        assertNotNull(aSwitched.getCorrespondence("two", "one", CorrespondenceRelation.INSTANCE_OF));
        
        assertEquals(2, aSwitched.getDistinctSourcesAsSet().size());
        assertEquals(1, aSwitched.getDistinctTargetsAsSet().size());
    }
    
    @Test
    void reverseWithoutRelationChangeTest(){
        Alignment a = new Alignment();
        a.add("one", "two", CorrespondenceRelation.HAS_INSTANCE);
        a.add("one", "three");
        
        assertEquals(1, a.getDistinctSourcesAsSet().size());
        assertEquals(2, a.getDistinctTargetsAsSet().size());
        
        Alignment aSwitched = a.reverseWithoutRelationChange();
        
        assertNull(aSwitched.getCorrespondence("one", "two", CorrespondenceRelation.HAS_INSTANCE));
        assertNotNull(aSwitched.getCorrespondence("two", "one", CorrespondenceRelation.HAS_INSTANCE));
        
        assertEquals(2, aSwitched.getDistinctSourcesAsSet().size());
        assertEquals(1, aSwitched.getDistinctTargetsAsSet().size());
    }
    
    
    @Test
    void addOrUseHighestConfidenceTest(){
        Alignment a = new Alignment();
        
        a.add("one", "two", 0.5);
                
        a.addOrUseHighestConfidence("one", "two", 0.4);        
        assertEquals(0.5, a.getCorrespondence("one", "two", CorrespondenceRelation.EQUIVALENCE).getConfidence());
        assertEquals(1, a.getDistinctConfidencesAsSet().size());//check if index is up to date
        assertTrue(a.getDistinctConfidencesAsSet().contains(0.5));//check if index is up to date
        
        a.addOrUseHighestConfidence("one", "two", 0.8);
        assertEquals(0.8, a.getCorrespondence("one", "two", CorrespondenceRelation.EQUIVALENCE).getConfidence());
        assertEquals(1, a.getDistinctConfidencesAsSet().size());//check if index is up to date
        assertTrue(a.getDistinctConfidencesAsSet().contains(0.8)); //check if index is up to date
    }
    
    @Test
    void addOrModifyTest(){
        Alignment a = new Alignment();
        
        a.addOrModify(new Correspondence("one", "two", 0.8));
        assertEquals(0.8, a.getCorrespondence("one", "two", CorrespondenceRelation.EQUIVALENCE).getConfidence());
        assertTrue(a.getDistinctConfidencesAsSet().contains(0.8));//check if index is up to date
        
        a.addOrModify("one", "two", 0.5, CorrespondenceRelation.EQUIVALENCE, new HashMap<>());
        assertEquals(0.5, a.getCorrespondence("one", "two", CorrespondenceRelation.EQUIVALENCE).getConfidence());
        assertEquals(1, a.getDistinctConfidencesAsSet().size()); //check if index is up to date
        assertTrue(a.getDistinctConfidencesAsSet().contains(0.5)); //check if index is up to date
    }
    
    
    @Test
    void getCorrespondencesRelationTest(){
        //run once with indices and once without inidices
        for(int i=0; i<2; i++){
            Alignment a = i > 0 ? new Alignment(false, false, false, false) : new Alignment();
            Correspondence x = new Correspondence("x_left", "x_right", CorrespondenceRelation.EQUIVALENCE);
            Correspondence y = new Correspondence("y_left", "y_right", CorrespondenceRelation.INCOMPAT);
            Correspondence z = new Correspondence("z_left", "z_right", CorrespondenceRelation.EQUIVALENCE);
            a.addAll(Arrays.asList(x, y, z));

            Set<Correspondence> s = new HashSet<>();
            a.getCorrespondencesRelation(CorrespondenceRelation.EQUIVALENCE).forEach(s::add);
            assertEquals(2, s.size());
            assertTrue(s.containsAll(Arrays.asList(x, z)));
            assertTrue(a.isRelationContained(CorrespondenceRelation.EQUIVALENCE));

            s = new HashSet<>();
            a.getCorrespondencesRelation(CorrespondenceRelation.INCOMPAT).forEach(s::add);
            assertEquals(1, s.size());
            assertTrue(s.contains(y));
            assertTrue(a.isRelationContained(CorrespondenceRelation.INCOMPAT));

            s = new HashSet<>();
            a.getCorrespondencesRelation(CorrespondenceRelation.SUBSUMED).forEach(s::add);
            assertEquals(0, s.size());
            assertFalse(a.isRelationContained(CorrespondenceRelation.SUBSUMED));
        }
        
    }
    
    @Test
    void sampleTest(){
        Alignment a = new Alignment();
        a.add("one", "two");
        a.add("three", "four");
        a.add("five", "six");
        
        assertEquals(1, a.sample(1).size());
        assertEquals(3, a.sample(3).size());
        assertThrows(IllegalArgumentException.class, () -> a.sample(5)); //too high
        assertThrows(IllegalArgumentException.class, () -> a.sample(-5)); //too low
        
        assertEquals(2, a.sampleByFraction(0.5).size());
        assertEquals(1, a.sampleByFraction(0.3).size());
        assertEquals(0, a.sampleByFraction(0.1).size());        
        assertThrows(IllegalArgumentException.class, () -> a.sampleByFraction(1.2)); //too high
        assertThrows(IllegalArgumentException.class, () -> a.sampleByFraction(-0.1)); //too low
    }

}
