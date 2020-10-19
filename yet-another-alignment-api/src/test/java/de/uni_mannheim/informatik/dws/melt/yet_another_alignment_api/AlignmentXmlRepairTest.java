package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class AlignmentXmlRepairTest {
    
    @Test
    public void testXmlRepair() throws IOException, SAXException{
        File unparsableAlignment = new File("src/test/resources/unparsable_alignment.rdf");
        
        assertThrows(SAXParseException.class, () -> {
            Alignment a = new Alignment(unparsableAlignment);
        });
        
        String repaired = AlignmentXmlRepair.repair(unparsableAlignment);        
        InputStream stream = new ByteArrayInputStream(repaired.getBytes(StandardCharsets.UTF_8));        
        Alignment a = new Alignment(stream); // should not throw any error
        assertEquals(3, a.size());

        Alignment b = AlignmentXmlRepair.loadRepairedAlignment(unparsableAlignment);
        assertEquals(3, b.size());
    }
    
    private static final String NEWLINE = System.getProperty("line.separator");
    
    @Test
    public void alreadyEncodedTest(){
        String init = "<entity1 rdf:resource=\"http://dbkwik.webdatacommons.org/memory-alpha.wikia.com/resource/Cloak_&amp;_Dagger\"/>";
        String result = AlignmentXmlRepair.repair(init);        
        String expected = "<entity1 rdf:resource=\"http://dbkwik.webdatacommons.org/memory-alpha.wikia.com/resource/Cloak_&amp;_Dagger\"/>" + NEWLINE;
        assertEquals(expected, result);
    }
    
    @Test
    public void encodedTest(){
        String init = "<entity1 rdf:resource=\"http://dbkwik.webdatacommons.org/memory-alpha.wikia.com/resource/Cloak_&_Dagger\"/>";
        String result = AlignmentXmlRepair.repair(init);        
        String expected = "<entity1 rdf:resource=\"http://dbkwik.webdatacommons.org/memory-alpha.wikia.com/resource/Cloak_&amp;_Dagger\"/>" + NEWLINE;
        assertEquals(expected, result);
    }
}
