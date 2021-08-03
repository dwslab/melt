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

class TextExtractorUseLongestLiteralOrFragmentTest {


    @Test
    void extract() {
        String modelText = "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"
                + "<http://example.com/one> <http://example.com/p> \"bbbbbbb\". \n"
                + "<http://example.com/one> <http://example.com/p> \"aaaaaaa\". \n"
                + "<http://example.com/one> <http://example.com/p2> \"one\".\n"
                + "<http://example.com/one> <http://example.com/p> \"ccccccc\". \n";
        
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);        
        m.read(new ByteArrayInputStream(modelText.getBytes(StandardCharsets.UTF_8)), null, "TURTLE");
        
        Resource r = m.createResource("http://example.com/one");
        
        TextExtractor extrator = new TextExtractorUseLongestLiteralOrFragment();
        Set<String> set = extrator.extract(r);
        
        assertEquals(1, set.size());
        assertTrue(set.contains("aaaaaaa"));
    }
}