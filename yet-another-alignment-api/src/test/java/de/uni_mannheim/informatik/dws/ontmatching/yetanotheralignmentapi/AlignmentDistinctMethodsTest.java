package de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AlignmentDistinctMethodsTest {
    
    @Test
    public void testNumberOfCells(){
        
        Alignment m = new Alignment();
        m.add("one", "two", 0.1, CorrespondenceRelation.EQUIVALENCE);
        m.add("one", "three", 0.2, CorrespondenceRelation.EQUIVALENCE);
        m.add("one", "four", 0.2, CorrespondenceRelation.INCOMPAT);
        
        assertEquals(
                setFromIterable(m.getDistinctSources()), 
                new HashSet<>(Arrays.asList("one")), 
                "only one source");
        assertIterableEquals(
                setFromIterable(m.getDistinctTargets()), 
                new HashSet<>(Arrays.asList("two", "three", "four")), 
                "test getDistinctTargets");
        assertIterableEquals(
                setFromIterable(m.getDistinctRelations()), 
                new HashSet<>(Arrays.asList(CorrespondenceRelation.EQUIVALENCE, CorrespondenceRelation.INCOMPAT)), 
                "test getDistinctRelations");
        assertIterableEquals(
                setFromIterable(m.getDistinctConfidences()), 
                new HashSet<>(Arrays.asList(0.1, 0.2)), 
                "test getDistinctConfidences");
        
    }
    
    private static <T> Set<T> setFromIterator(Iterator<T> it) {
        Set<T> s = new HashSet<>();
        while (it.hasNext()) s.add(it.next());
        return s;
    }
    private static <T> Set<T> setFromIterable(Iterable<T> it) {
        return setFromIterator(it.iterator());
    }
}
