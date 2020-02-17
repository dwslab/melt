package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import org.xml.sax.SAXException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AlignmentParserTest {

    @Test
    public void testNumberOfCells() throws SAXException, IOException {
        assertEquals(11,getOneMapping().size(), "Alignment does not contain 11 cells.");
    }

    @Test
    public void testExtensionParsing() throws Exception {
        File alignmentFile = new File("src/test/resources/alignment_extension_test.rdf");
        Alignment alignment = AlignmentParser.parse(alignmentFile);

        //----------------------------------------------------------------------
        // Extensions on Alignment Level
        //----------------------------------------------------------------------
        assertEquals(2, alignment.getExtensions().size());
        assertEquals(alignment.getExtensionValue("http://www.alignment_extension_1.com/extensionLabel_1"), "alignment extension value 1");
        assertEquals(alignment.getExtensionValue("http://www.alignment_extension_2.com#extensionLabel_2"), "alignment extension value 2");

        //----------------------------------------------------------------------
        // Extensions on Correspondence Level
        //----------------------------------------------------------------------
        int extensionsCount = 0;
        for(Correspondence c : alignment){
            if(c.getExtensions() != null){
                extensionsCount = extensionsCount + c.getExtensions().size();
            }
        }
        assertEquals(2, extensionsCount);
        Correspondence correspondence = alignment.getCorrespondence("http://cmt#assignExternalReviewer", "http://conference#invites_co-reviewers", CorrespondenceRelation.EQUIVALENCE);
        assertNotNull(correspondence);
        assertEquals("correspondence extension value 1", correspondence.getExtensionValue("http://www.correspondence_extension_1.com/extensionLabel_1"));
        assertEquals("correspondence extension value 2", correspondence.getExtensionValue("http://www.correspondence_extension_2.com#extensionLabel_2"));


    }

    public static Alignment getOneMapping() throws SAXException, IOException{
        return AlignmentParser.parse(AlignmentParserTest.class.getClassLoader().getResourceAsStream("LogMap-cmt-conference.rdf"));
    }

}