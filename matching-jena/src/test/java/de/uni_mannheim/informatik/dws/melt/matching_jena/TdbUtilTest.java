package de.uni_mannheim.informatik.dws.melt.matching_jena;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class TdbUtilTest {
    
    @Test
    void getFileFromURLTest() throws Exception {        
        assertNull(TdbUtil.getFileFromURL("https://www.google.de"));
        assertNull(TdbUtil.getFileFromURL("abcde"));
        assertNull(TdbUtil.getFileFromURL("mailto:max@example.org"));
        
        File pom = new File("pom.xml");        
        File actual = TdbUtil.getFileFromURL(pom.toURI().toURL().toString());        
        assertNotNull(actual);
        assertEquals(pom.getCanonicalPath(), actual.getCanonicalPath());
    }
}