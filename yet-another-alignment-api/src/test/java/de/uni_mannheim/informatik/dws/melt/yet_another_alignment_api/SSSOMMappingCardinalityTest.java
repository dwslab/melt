package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class SSSOMMappingCardinalityTest {
        
    @Test
    public void testParsing(){
        
        assertEquals(SSSOMMappingCardinality.ONE_TO_ONE, SSSOMMappingCardinality.fromString("1:1"));        
        assertEquals(SSSOMMappingCardinality.ONE_TO_ONE, SSSOMMappingCardinality.fromString("11"));
        
        assertEquals(SSSOMMappingCardinality.ONE_TO_MANY, SSSOMMappingCardinality.fromString("1:n"));
        assertEquals(SSSOMMappingCardinality.ONE_TO_MANY, SSSOMMappingCardinality.fromString("1:N"));
        assertEquals(SSSOMMappingCardinality.ONE_TO_MANY, SSSOMMappingCardinality.fromString("1n"));
        assertEquals(SSSOMMappingCardinality.ONE_TO_MANY, SSSOMMappingCardinality.fromString("1:*"));
        assertEquals(SSSOMMappingCardinality.ONE_TO_MANY, SSSOMMappingCardinality.fromString("1*"));
        
        assertEquals(SSSOMMappingCardinality.MANY_TO_MANY, SSSOMMappingCardinality.fromString("n:n"));
        assertEquals(SSSOMMappingCardinality.MANY_TO_MANY, SSSOMMappingCardinality.fromString("N:N"));
        assertEquals(SSSOMMappingCardinality.MANY_TO_MANY, SSSOMMappingCardinality.fromString("NN"));
        assertEquals(SSSOMMappingCardinality.MANY_TO_MANY, SSSOMMappingCardinality.fromString("nn"));
        assertEquals(SSSOMMappingCardinality.MANY_TO_MANY, SSSOMMappingCardinality.fromString("*:*"));
        assertEquals(SSSOMMappingCardinality.MANY_TO_MANY, SSSOMMappingCardinality.fromString("**"));
        
        assertEquals(SSSOMMappingCardinality.ONE_TO_ONE, SSSOMMappingCardinality.fromString("foo")); //default
    }
}
