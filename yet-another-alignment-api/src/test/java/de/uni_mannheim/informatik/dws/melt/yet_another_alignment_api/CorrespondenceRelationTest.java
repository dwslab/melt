package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CorrespondenceRelationTest {

    @Test
    public void testParsing() throws IOException{
        assertEquals(CorrespondenceRelation.CLOSE, CorrespondenceRelation.parse("skos:closeMatch"));
        assertEquals(CorrespondenceRelation.CLOSE, CorrespondenceRelation.parse("http://www.w3.org/2004/02/skos/core#closeMatch"));
        
        
        assertEquals(CorrespondenceRelation.EQUIVALENCE, CorrespondenceRelation.parse("=")); 
        assertEquals(CorrespondenceRelation.EQUIVALENCE, CorrespondenceRelation.parse("http://www.w3.org/2002/07/owl#equivalentClass"));
        assertEquals(CorrespondenceRelation.EQUIVALENCE, CorrespondenceRelation.parse("owl:equivalentClass"));
        assertEquals(CorrespondenceRelation.EQUIVALENCE, CorrespondenceRelation.parse("http://www.w3.org/2004/02/skos/core#exactMatch"));
        assertEquals(CorrespondenceRelation.EQUIVALENCE, CorrespondenceRelation.parse("skos:exactMatch"));
        
        assertEquals(CorrespondenceRelation.EQUIVALENCE, CorrespondenceRelation.parse("skos:exactmatch")); //lowercase
        
        
        
        
        assertEquals(CorrespondenceRelation.UNKNOWN, CorrespondenceRelation.parse("foo"));
        assertEquals(CorrespondenceRelation.UNKNOWN, CorrespondenceRelation.parse("bar"));
        assertEquals(CorrespondenceRelation.UNKNOWN, CorrespondenceRelation.parse(null)); 
    }
}