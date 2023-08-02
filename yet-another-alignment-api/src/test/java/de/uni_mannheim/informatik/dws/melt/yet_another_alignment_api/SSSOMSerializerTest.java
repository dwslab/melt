package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.DefaultExtensions.SSSOM;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.xml.sax.SAXException;

class SSSOMSerializerTest {    
    
    @Test
    public void testSerializer() throws IOException, SAXException{
        Alignment a = new Alignment();
        a.addExtensionValue(SSSOM.AUTHOR_ID.toString(), Arrays.asList("bla"));
        a.addExtensionValue("foo#bar", "one");
        
        String s = SSSOMSerializer.serialize(a);
        //System.out.println(s);
        assertFalse(s.contains("#bar: one")); //asert false
        assertTrue(s.contains("#author_id:"));
        assertTrue(s.contains("#- bla"));
        assertTrue(s.contains("#curie_map:"));
        assertTrue(s.contains("subject_id	object_id	predicate_id	confidence"));
        
        
        s = SSSOMSerializer.serialize(a, true);
        //System.out.println(s);
        assertTrue(s.contains("#bar: one"));
        assertTrue(s.contains("#author_id:"));
        assertTrue(s.contains("#- bla"));
        assertTrue(s.contains("#curie_map:"));
        assertTrue(s.contains("subject_id	object_id	predicate_id	confidence"));
    }
    
    @Test
    public void testAlignmentExtensionAlternatives() throws IOException, SAXException{
        InputStream i = this.getClass().getClassLoader().getResourceAsStream("mp_hp_mgi_all.sssom.tsv");
        Alignment a = new Alignment();
        a.addExtensionValue(DefaultExtensions.StandardApi.METHOD, "bla");
        
        String s = SSSOMSerializer.serialize(a);
        //System.out.println(s);
        assertTrue(s.contains("#mapping_tool: bla"));
    }
    
    
    @Test
    public void testCorrespondenceExtension() throws IOException, SAXException{
        Correspondence c = new Correspondence("one", "two");
        c.addAdditionalConfidence(Alignment.class, 0.6);
        c.addAdditionalExplanation(Alignment.class, "hello");
        c.addExtensionValue(DefaultExtensions.SSSOM.COMMENT, "mycomment");
        c.addExtensionValue(DefaultExtensions.DublinCore.CREATOR, "me");
        Alignment a = new Alignment();
        a.add(c);
        
        String s = SSSOMSerializer.serialize(a);
        //System.out.println(s);
        assertTrue(s.contains("subject_id	object_id	predicate_id	confidence	author_label	comment"));
        assertTrue(s.contains("one	two	skos:exactMatch	1.0	me	mycomment"));
        
        
        s = SSSOMSerializer.serialize(a, true);
        //System.out.println(s);
        assertTrue(s.contains("subject_id	object_id	predicate_id	confidence	author_label	comment	Alignment_confidence	Alignment_explanation	creator"));
        assertTrue(s.contains("one	two	skos:exactMatch	1.0	me	mycomment	0.6	hello	me"));
    }
}