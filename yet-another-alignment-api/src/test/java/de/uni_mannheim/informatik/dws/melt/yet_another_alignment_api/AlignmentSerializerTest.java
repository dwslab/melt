package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlignmentSerializerTest {

    @Test
    void serializeString(){

        //-------------------------------------------------
        // Test 1: Simple test with extension (# in extension uri).
        //-------------------------------------------------

        Alignment alignment = new Alignment();
        Correspondence cell = new Correspondence("http://cmt#assignExternalReviewer", "http://conference#invites_co-reviewers", 1.0);
        cell.addExtensionValue("http://www.extension.com#extensionLabel", "MyExtensionValue");
        alignment.add(cell);
        String result = AlignmentSerializer.serialize(alignment);
        //System.out.println(result);
        assertTrue(result.contains("<alignapilocalns:extensionLabel xmlns:alignapilocalns=\"http://www.extension.com#\">MyExtensionValue</alignapilocalns:extensionLabel>"));
        assertTrue(result.contains("<entity1 rdf:resource=\"http://cmt#assignExternalReviewer\"/>"));
        assertTrue(result.contains("<entity2 rdf:resource=\"http://conference#invites_co-reviewers\"/>"));
        assertTrue(result.contains("<relation>=</relation>"));
        assertTrue(result.contains("<relation>=</relation>"));
        assertTrue(result.contains("<measure rdf:datatype=\"xsd:float\">1.0</measure>"));


        //-------------------------------------------------
        // Test 2: Simple test with extension. (slash in extension uri)
        //-------------------------------------------------

        alignment = new Alignment();
        cell = new Correspondence("http://cmt#assignExternalReviewer", "http://conference#invites_co-reviewers", 1.0);
        cell.addExtensionValue("http://www.extension.com/extensionLabel", "MyExtensionValue");
        alignment.add(cell);
        result = AlignmentSerializer.serialize(alignment);
        assertTrue(result.contains("<alignapilocalns:extensionLabel xmlns:alignapilocalns=\"http://www.extension.com/\">MyExtensionValue</alignapilocalns:extensionLabel>"));
        assertTrue(result.contains("<entity1 rdf:resource=\"http://cmt#assignExternalReviewer\"/>"));
        assertTrue(result.contains("<entity2 rdf:resource=\"http://conference#invites_co-reviewers\"/>"));
        assertTrue(result.contains("<relation>=</relation>"));
        assertTrue(result.contains("<relation>=</relation>"));
        assertTrue(result.contains("<measure rdf:datatype=\"xsd:float\">1.0</measure>"));


    }
    
    
    @Test
    void getExtensionLabel(){
        assertEquals("extensionLabel", AlignmentSerializer.getExtensionLabel("http://www.extension.com#extensionLabel"));
        assertEquals("extensionLabel", AlignmentSerializer.getExtensionLabel("http://www.extension.com/extensionLabel"));
        assertThrows(IllegalArgumentException.class, ()->{
            AlignmentSerializer.getExtensionLabel("extensionLabel");
        });
    }
    
    @Test
    void getExtensionBaseUri(){
        assertEquals("http://www.extension.com#", AlignmentSerializer.getExtensionBaseUri("http://www.extension.com#extensionLabel"));
        assertEquals("http://www.extension.com/", AlignmentSerializer.getExtensionBaseUri("http://www.extension.com/extensionLabel"));
        assertThrows(IllegalArgumentException.class, ()->{
            AlignmentSerializer.getExtensionBaseUri("extensionLabel");
        });
    }
    
    

}