package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalExtractors.LiteralExtractorAllAnnotationProperties;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorAllAnnotationProperties;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Literal;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import static org.junit.jupiter.api.Assertions.*;

class TextExtractorOnlyLabelTest {


    @Test
    void extract() {
        TextExtractor extrator = new TextExtractorOnlyLabel();
        
        
        
        String modelText = "<http://example.com/one> <http://www.w3.org/2000/01/rdf-schema#label> \"x\". \n";
        assertOneText(modelText, "x");
        
        modelText = 
                "<http://example.com/one> <http://www.w3.org/2000/01/rdf-schema#label> \"x\". \n" +
                "<http://example.com/one> <http://www.w3.org/2000/01/rdf-schema#label> \"y\"@en. \n";
        assertOneText(modelText, "y");
        
        modelText = 
                "<http://example.com/one> <http://www.w3.org/2000/01/rdf-schema#label> \"x\". \n" +
                "<http://example.com/one> <http://www.w3.org/2000/01/rdf-schema#label> \"y\"@en. \n"+
                "<http://example.com/one> <http://www.w3.org/2004/02/skos/core#prefLabel> \"z\". \n";
        assertOneText(modelText, "z");
        
        modelText = 
                "<http://example.com/one> <http://www.w3.org/2000/01/rdf-schema#label> \"x\". \n" +
                "<http://example.com/one> <http://www.w3.org/2000/01/rdf-schema#label> \"y\"@en. \n"+
                "<http://example.com/one> <http://www.w3.org/2004/02/skos/core#prefLabel> \"z\". \n"+
                "<http://example.com/one> <http://www.w3.org/2004/02/skos/core#prefLabel> \"a\"@en. \n";
        assertOneText(modelText, "a");
        
        
        modelText = 
                "<http://example.com/one> <http://www.w3.org/1999/02/22-rdf-syntax-ns#> <http://www.w3.org/2000/01/rdf-schema#Class>. \n";
        assertOneText(modelText, "one");
        
        modelText = 
                "<http://example.com/one> <http://www.w3.org/2000/01/rdf-schema#label> \"test\"@de. \n";
        assertOneText(modelText, "one");
    }
    
    
    
    private void assertOneText(String modelText, String result){        
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);        
        m.read(new ByteArrayInputStream(modelText.getBytes(StandardCharsets.UTF_8)), null, "TURTLE");
        
        Resource r = m.createResource("http://example.com/one");
        TextExtractor extrator = new TextExtractorOnlyLabel();        
        Set<String> set = extrator.extract(r);        
        assertEquals(1, set.size());
        assertTrue(set.contains(result));
    }
}