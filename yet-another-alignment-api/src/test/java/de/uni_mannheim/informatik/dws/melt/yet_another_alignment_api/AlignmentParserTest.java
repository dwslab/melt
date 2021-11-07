package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.io.ByteArrayInputStream;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AlignmentParserTest {
    
    private static final String newline = System.getProperty("line.separator");
    
    @Test
    public void testParseCSVWithoutHeader() throws IOException{
        File tsv = new File(AlignmentParserTest.class.getResource("/tsv_parsing.tsv").getFile());
        Alignment alignment = AlignmentParser.parseCSVWithoutHeader(tsv, '\t');
        checkAlignmentTSV(alignment);
    }
    
    @Test
    public void testParseTSV() throws IOException{
        File tsv = new File(AlignmentParserTest.class.getResource("/tsv_parsing.tsv").getFile());
        Alignment alignment = AlignmentParser.parseTSV(tsv);
        checkAlignmentTSV(alignment);
    }
    
    @Test
    public void testParseCSV() throws IOException{
        File csv = new File(AlignmentParserTest.class.getResource("/csv_parsing.csv").getFile());
        Alignment alignment = AlignmentParser.parseCSV(csv);
        checkAlignmentTSV(alignment);
    }
    
    @Test
    public void testParseCSVHeaderChanged() throws IOException{
        File csvheaderSwitch = new File(AlignmentParserTest.class.getResource("/csv_parsing_different_header.csv").getFile());
        Alignment alignment = AlignmentParser.parseCSV(csvheaderSwitch);
        checkAlignmentTSV(alignment);
    }
    
    private void checkAlignmentTSV(Alignment a){
        assertEquals(5, a.size());
        for(Correspondence c : a){
            if(c.getEntityOne().equals("http://dbpedia.org/resource/E491310")){
                assertEquals(CorrespondenceRelation.INCOMPAT, c.getRelation());
            }
            if(c.getEntityOne().equals("http://dbpedia.org/resource/E491311")){
                assertEquals(1.0, c.getConfidence());
            }else{
                assertTrue(c.getConfidence() > 0.3);
                assertTrue(c.getConfidence() < 0.35);
            }
            assertTrue(c.getEntityOne().startsWith("http://dbpedia.org/resource/"));
            assertTrue(c.getEntityTwo().startsWith("http://www.wikidata.org/entity/"));
        }
    }
    
    @Test
    public void testExtensionValues() throws SAXException, IOException {
        Alignment a = new Alignment(Arrays.asList(
                new Correspondence("http://cmt#assignExternalReviewer", "http://conference#invites_co-reviewers", 1.0, 
                "http://melt.dws.uni-mannheim.de/configuration#test", "[]")
        ));
        String serialized = a.serialize();
        Alignment b = new Alignment(new ByteArrayInputStream(serialized.getBytes()));
        assertEquals("[]", b.iterator().next().getExtensionValueAsString("http://melt.dws.uni-mannheim.de/configuration#test"));
    }
    
    @Test
    public void testConfidenceRepresentations() throws SAXException, IOException {
        //https://docs.oracle.com/javase/7/docs/api/java/lang/Double.html#toString%28double%29
        List<Double> doubles = Arrays.asList(
                Double.NaN, 
                Double.MIN_VALUE, Double.MAX_VALUE, 
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                0.0, -0.0,
                0.9, 2.0, //some normal values
                0.000001, 12345678d); //small values, large values
        
        for(Double d : doubles){
            //test serializer and parser
            String serialized = new Alignment(Arrays.asList(new Correspondence("one", "two", d))).serialize();
            Alignment b = new Alignment(new ByteArrayInputStream(serialized.getBytes()));
            assertEquals(d, b.iterator().next().getConfidence());
            
            //test parser
            String serlizedWithConfidence = new Alignment(Arrays.asList(new Correspondence("one", "two", 0.55))).serialize();
            String replaced = serlizedWithConfidence.replace("0.55", Double.toString(d));
            Alignment c = new Alignment(new ByteArrayInputStream(replaced.getBytes()));
            assertEquals(d, c.iterator().next().getConfidence());
            
            //introduce newlines whitespace etc
            String replacedWhitespace = serlizedWithConfidence.replace("0.55", "\n" + Double.toString(d) + "\t" + newline);
            Alignment e = new Alignment(new ByteArrayInputStream(replacedWhitespace.getBytes()));
            assertEquals(d, e.iterator().next().getConfidence());
        }
    }
    
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