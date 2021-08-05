package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Test;

import java.util.Set;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

import static org.junit.jupiter.api.Assertions.*;

class TextExtractorForTransformersTest {


    @Test
    void extractOne() {
        String modelText = "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n"
                + "<http://example.com/one> rdfs:comment \"alpha beta Gamma\". \n"
                + "<http://example.com/one> rdfs:label \"Alpha\".\n"
                + "<http://example.com/one> rdfs:label \"Beta\".\n";
        
        Set<String> set = extract(modelText, "http://example.com/one");
        assertEquals(2, set.size());
        assertTrue(set.contains("alpha beta Gamma"));
        assertTrue(set.contains("one"));
    }
    
    
    @Test
    void extractTwo() {
        String modelText = "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n"
                + "<http://example.com/one> rdfs:comment \"alpha beta Gamma\". \n"
                + "<http://example.com/one> rdfs:label \"Alpha\".\n"
                + "<http://example.com/one> rdfs:label \"Beta\".\n"
                + "<http://example.com/one> rdfs:label \"New\".\n";
        
        Set<String> set = extract(modelText, "http://example.com/one");
        assertEquals(3, set.size());
        assertTrue(set.contains("alpha beta Gamma"));
        assertTrue(set.contains("New"));
        assertTrue(set.contains("one"));
    }
    
    @Test
    void extractThree() {
        String modelText = "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n"
                + "<http://example.com/one> rdfs:comment \"alpha beta Gamma\". \n"
                + "<http://example.com/one> <http://example.com/foo> \"This is a very long comment and contains alpha beta Gamma\".\n"
                + "<http://example.com/one> rdfs:label \"Beta\".\n"
                + "<http://example.com/one> rdfs:label \"New\".\n";
        
        Set<String> set = extract(modelText, "http://example.com/one");
        assertEquals(3, set.size());
        assertTrue(set.contains("This is a very long comment and contains alpha beta Gamma"));
        assertTrue(set.contains("New"));
        assertTrue(set.contains("one"));
    }
    
    @Test
    void extractFour() {
        String modelText = "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n"
                + "<http://example.com/Elizabeth_Lense> rdfs:comment \"Doctor Elizabeth Lense was Human female who served as a Starfleet medical officer\". \n"
                + "<http://example.com/Elizabeth_Lense> rdfs:label   \"Elizabeth Lense\".\n"
                + "<http://example.com/Elizabeth_Lense> <http://example.com/abstract> \"Doctor Elizabeth Lense was Human female who served as a Starfleet medical officer in the late-24th century.\".\n";
        
        Set<String> set = extract(modelText, "http://example.com/Elizabeth_Lense");        
        assertEquals(1, set.size());
        assertTrue(set.contains("Doctor Elizabeth Lense was Human female who served as a Starfleet medical officer in the late-24th century."));
    }
    
    @Test
    void extractFive() {
        
        String modelText = "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n"
                + "<http://example.com/Risk,_Part_One> rdfs:comment \"Captain Benjamin Sisko… murderer?!\".\n"
                + "<http://example.com/Risk,_Part_One> rdfs:label \"Risk, Part One\".\n"
                + "<http://example.com/Risk,_Part_One> <http://example.com/abstract> \"Captain Benjamin Sisko… murderer?!\".\n";
        
        Set<String> set = extract(modelText, "http://example.com/Risk,_Part_One");        
        assertEquals(2, set.size());
        assertTrue(set.contains("Captain Benjamin Sisko… murderer?!"));
        assertTrue(set.contains("Risk, Part One"));
    }
    
    @Test
    void extractSix() {
        String modelText = "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n"
                + "<http://example.com/T%27Paal_(city)> rdfs:label \"T'Paal (city)\".\n";

        Set<String> set = extract(modelText, "http://example.com/T%27Paal_(city)");        
        assertEquals(1, set.size());
        assertTrue(set.contains("T'Paal (city)") || set.contains("T%27Paal_(city)"));
    }
    
    @Test
    void extractSeven() {
        String modelText = "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n"
                + "<http://example.com/example> rdfs:label \"A label with % in it.\".\n";

        Set<String> set = extract(modelText, "http://example.com/example");        
        assertEquals(2, set.size());
        assertTrue(set.contains("example"));
        assertTrue(set.contains("A label with % in it."));
    }
    
    
    private static Set<String> extract(String modelText, String resourceURI){
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);        
        m.read(new ByteArrayInputStream(modelText.getBytes(StandardCharsets.UTF_8)), null, "TURTLE");
        
        Resource r = m.createResource(resourceURI);
        
        TextExtractor extrator = new TextExtractorForTransformers();
        return extrator.extract(r);
    }
}