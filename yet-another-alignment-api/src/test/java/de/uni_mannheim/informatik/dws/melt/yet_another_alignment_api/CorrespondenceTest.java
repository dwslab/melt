package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CorrespondenceTest {

    @Test
    void parseRelation() {
        assertEquals(CorrespondenceRelation.EQUIVALENCE, CorrespondenceRelation.parse("="));
    }
    
    @Test
    void parseVarargsExtensions() {
        Correspondence c = new Correspondence("one", "two", 0.0, CorrespondenceRelation.EQUIVALENCE, "key1", "value1", "key2", "value2");
        
        assertEquals("value1", c.getExtensions().getOrDefault("key1", ""));
        assertEquals("value2", c.getExtensions().getOrDefault("key2", ""));
    }
    
    @Test
    void parseVarargsExtensionsUneven() {
        Correspondence c = new Correspondence("one", "two", 0.0, CorrespondenceRelation.EQUIVALENCE, "key1", "value1", "key2");
        
        assertEquals("value1", c.getExtensions().getOrDefault("key1", ""));
        assertEquals("", c.getExtensions().getOrDefault("key2", ""));
    }
}