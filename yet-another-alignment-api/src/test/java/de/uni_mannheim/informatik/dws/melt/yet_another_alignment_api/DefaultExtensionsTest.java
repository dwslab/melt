package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class DefaultExtensionsTest {
        
    @Test
    public void testURIs(){        
        assertEquals("http://purl.org/dc/elements/1.1/creator", DefaultExtensions.DublinCore.CREATOR.toString());
        assertEquals("CREATOR", DefaultExtensions.DublinCore.CREATOR.name());
        
        assertEquals(DefaultExtensions.SSSOM.SUBJECT_ID, DefaultExtensions.SSSOM.fromName("subject_id"));
        assertNull(DefaultExtensions.SSSOM.fromName("https://w3id.org/sssom/schema/subject_id"));
        
        assertEquals(DefaultExtensions.SSSOM.SUBJECT_ID, DefaultExtensions.SSSOM.fromURL("https://w3id.org/sssom/schema/subject_id"));
        assertNull(DefaultExtensions.SSSOM.fromURL("subject_id"));
        
        assertEquals(DefaultExtensions.SSSOM.SUBJECT_ID, DefaultExtensions.SSSOM.fromURLOrName("https://w3id.org/sssom/schema/subject_id"));
        assertEquals(DefaultExtensions.SSSOM.SUBJECT_ID, DefaultExtensions.SSSOM.fromURLOrName("subject_id"));
        
    }
    
}
