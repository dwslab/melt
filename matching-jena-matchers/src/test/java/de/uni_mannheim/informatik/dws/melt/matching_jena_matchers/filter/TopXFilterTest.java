package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopXFilterTest {


    private static final Alignment ALIGNMENT_1 = new Alignment();

    @BeforeAll
    static void setUp(){
        ALIGNMENT_1.add("A","A", 1.0);
        ALIGNMENT_1.add("A","B", 0.9);
        ALIGNMENT_1.add("A","C", 0.8);
        ALIGNMENT_1.add("A","D", 0.7);

        ALIGNMENT_1.add("B","B", 1.0);
        ALIGNMENT_1.add("B","C", 0.9);
        ALIGNMENT_1.add("B","D", 0.8);
        ALIGNMENT_1.add("B","E", 0.7);

        ALIGNMENT_1.add("C","C", 1.0);
        ALIGNMENT_1.add("C","D", 0.9);
        ALIGNMENT_1.add("C","E", 0.8);
        ALIGNMENT_1.add("C","F", 0.7);
    }

    @Test
    void topXfilter(){
        // source
        TopXFilter filter = new TopXFilter(2, TopXFilter.TopFilterMode.SOURCE, 0.0);
        Alignment result = filter.filter(ALIGNMENT_1);
        assertEquals(6, result.size());
        assertTrue(result.contains(new Correspondence("A", "A", 1.0)));
        assertFalse(result.contains(new Correspondence("A", "C", 0.8)));

        // source with threshold filter
        filter = new TopXFilter(2, TopXFilter.TopFilterMode.SOURCE, 0.9);
        result = filter.filter(ALIGNMENT_1);
        assertEquals(3, result.size());
        assertTrue(result.contains(new Correspondence("A", "A", 1.0)));
        assertFalse(result.contains(new Correspondence("A", "C", 0.8)));

        // smallest
        filter = new TopXFilter(2, TopXFilter.TopFilterMode.SMALLEST, 0.0);
        result = filter.filter(ALIGNMENT_1);
        assertEquals(6, result.size());
        assertTrue(result.contains(new Correspondence("A", "A", 1.0)));
        assertFalse(result.contains(new Correspondence("A", "C", 0.8)));

        // target
        filter = new TopXFilter(2, TopXFilter.TopFilterMode.TARGET, 0.0);
        result = filter.filter(ALIGNMENT_1);
        assertEquals(10, result.size());
        assertTrue(result.contains(new Correspondence("A", "A", 1.0)));

        // largest
        filter = new TopXFilter(2, TopXFilter.TopFilterMode.LARGEST, 0.0);
        result = filter.filter(ALIGNMENT_1);
        assertEquals(10, result.size());
        assertTrue(result.contains(new Correspondence("A", "A", 1.0)));

        // null
        assertNull(filter.filter(null));
    }

    @Test
    void setGetX() {
        TopXFilter filter = new TopXFilter(10);
        assertEquals(10, filter.getX());
        filter.setX(20);
        assertEquals(20, filter.getX());
        filter.setX(0);
        assertEquals(TopXFilter.DEFAULT_X, filter.getX());
    }

    @Test
    void setGetThreshold(){
        TopXFilter filter = new TopXFilter(10, 0.5);
        assertEquals(0.5, filter.getThreshold());
    }
}