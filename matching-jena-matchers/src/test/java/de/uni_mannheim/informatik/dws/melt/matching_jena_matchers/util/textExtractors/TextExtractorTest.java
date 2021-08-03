package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TextExtractorTest {
    
    @Test
    public void testAppend(){
        TextExtractor textExtractor = new TextExtractorProperty(RDFS.label);
        
        
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource("http://example.com/one");
        r.addProperty(RDFS.label, "hello_with_underscores");
        
        Set<String> extracted = textExtractor.extract(r);
        assertEquals(1, extracted.size());
        assertEquals("hello_with_underscores", extracted.iterator().next());
        
        textExtractor = TextExtractor.appendStringPostProcessing(textExtractor, StringProcessing::normalizeOnlyCamelCaseAndUnderscore);
        
        extracted = textExtractor.extract(r);
        assertEquals(1, extracted.size());
        assertEquals("hello with underscores", extracted.iterator().next());
    }
}
