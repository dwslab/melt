package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.io.ByteArrayInputStream;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    
    @Test
    public void testCorrespondenceExtensionSerializingAndParsing() throws Exception {        
        Correspondence c = new Correspondence("one", "two");
        c.addAdditionalConfidence(Alignment.class, 0.5);
        c.addAdditionalExplanation(Alignment.class, "hello");        
        Map<String, String> map = new HashMap<>();
        map.put("foo", "bar");
        c.addExtensionValue("bla#myextension", map);
                
        
        Alignment alignment = new Alignment(Arrays.asList(c));
        
        String s = alignment.serialize();
        
        Alignment newAlign = Alignment.parse(s);
        
        Correspondence newC = newAlign.getCorrespondence("one", "two", CorrespondenceRelation.EQUIVALENCE);
        assertNotNull(newC);
        
        assertEquals(0.5, newC.getAdditionalConfidence(Alignment.class));
        assertEquals("hello", newC.getAdditionalExplanation(Alignment.class));
        Map<String, String> newMap = newC.getExtensionValueCasted("bla#myextension");
        assertEquals("bar", newMap.getOrDefault("foo", ""));
    }
    
    @Test
    public void testAlignmentExtensionSerializingAndParsing() throws Exception {
        
        Alignment alignment = new Alignment();
        
        alignment.addExtensionValue("one#two", 0.5);
        alignment.addExtensionValue("three#four", "five");
        
        Map<String, String> map = new HashMap<>();
        map.put("foo", "bar");
        alignment.addExtensionValue("bla#myextension", map);
        
        List<Integer> list = Arrays.asList(5,9,3,8);
        alignment.addExtensionValue("bla#myList", list);
        
        String s = alignment.serialize();
        
        Alignment newAlign = Alignment.parse(s);
        
        double d = newAlign.getExtensionValueCasted("one#two");
        assertEquals(0.5, d);
        
        String text = newAlign.getExtensionValueCasted("three#four");
        assertEquals("five", text);
        
        Map<String, String> newMap = newAlign.getExtensionValueCasted("bla#myextension");
        assertEquals("bar", newMap.get("foo"));
        
        List<Double> newList = newAlign.getExtensionValueCasted("bla#myList");
        assertEquals(9.0, newList.get(1));
    }
    
    
    @Test
    public void testAlignmentExtensionParsingNoJson() throws Exception {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
"<rdf:RDF xmlns=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment\"\n" +
"  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
"  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n" +
"<Alignment>\n" +
"  <map>\n" +
"    <Cell>\n" +
"      <entity1 rdf:resource=\"one\"/>\n" +
"      <entity2 rdf:resource=\"two\"/>\n" +
"      <relation>=</relation>\n" +
"      <measure rdf:datatype=\"xsd:float\">1.0</measure>\n" +
"      <alignapilocalns:Alignment_explanation xmlns:alignapilocalns=\"http://melt.dws.uni-mannheim.de/configuration#\">hello</alignapilocalns:Alignment_explanation>\n" +
"    </Cell>\n" +
"  </map>\n" +
"</Alignment>\n" +
"</rdf:RDF>";
        
        Alignment newAlign = Alignment.parse(s);
        String explanation = newAlign.getCorrespondence("one", "two", CorrespondenceRelation.EQUIVALENCE).getAdditionalExplanation(Alignment.class);
        assertEquals("hello", explanation);
        
        s = s.replace("hello", "{\"foo:"); // introducing a json parser error
        newAlign = Alignment.parse(s);
        explanation = newAlign.getCorrespondence("one", "two", CorrespondenceRelation.EQUIVALENCE).getAdditionalExplanation(Alignment.class);
        assertEquals("{\"foo:", explanation);
    }

    public static Alignment getOneMapping() throws SAXException, IOException{
        return AlignmentParser.parse(AlignmentParserTest.class.getClassLoader().getResourceAsStream("LogMap-cmt-conference.rdf"));
    }

}