package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.LiteralExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorAllAnnotationProperties;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorForTransformers;
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

class LiteralExtractorAllAnnotationPropertiesTest {


    @Test
    void extract() {
        TestCase anatomyTestCase = TrackRepository.Anatomy.Default.getFirstTestCase();
        OntModel humanOntology = anatomyTestCase.getTargetOntology(OntModel.class);

        OntResource nervousSystem = humanOntology.getOntResource("http://human.owl#NCI_C12438");

        LiteralExtractorAllAnnotationProperties extractor = new LiteralExtractorAllAnnotationProperties();

        Set<Literal> result = extractor.extract(nervousSystem);
        assertTrue(result.size() > 1);

        Set<String> resultStringSet= result.stream().map(x -> x.getLexicalForm()).collect(Collectors.toSet());
        assertTrue(resultStringSet.contains("The part of the nervous system that consists of the brain, spinal cord, and meninges."));
        assertTrue(resultStringSet.contains("Central_Nervous_System"));

        // assert not null condition
        assertNotNull(extractor.extract(null));
    }
    
    @Test
    void extractTwo() {
        String modelText = "@prefix :      <http://human.owl#> .\n"
                + "@prefix oboInOwl: <http://www.geneontology.org/formats/oboInOwl#> .\n"
                + "@prefix owl:   <http://www.w3.org/2002/07/owl#> .\n"
                + "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n"
                + "oboInOwl:hasRelatedSynonym a owl:AnnotationProperty. \n"
                + ":NCI_C53161 oboInOwl:hasRelatedSynonym  :genid1332 , :genid1333 , :genid1334.\n"
                + ":NCI_C53161 rdfs:label \"mylabel\".\n"
                + ":genid1332  rdfs:label  \"one\".\n"
                + ":genid1333  rdfs:label  \"two\".\n"
                + ":genid1334  rdfs:label  \"three\".\n";
        
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);        
        m.read(new ByteArrayInputStream(modelText.getBytes(StandardCharsets.UTF_8)), null, "TURTLE");
        
        Resource r = m.createResource("http://human.owl#NCI_C53161");
        
        LiteralExtractor extractor = new LiteralExtractorAllAnnotationProperties();

        Set<Literal> result = extractor.extract(r);
        assertEquals(4, result.size());

        Set<String> resultStringSet= result.stream().map(x -> x.getLexicalForm()).collect(Collectors.toSet());
        assertTrue(resultStringSet.contains("mylabel"));
        assertTrue(resultStringSet.contains("one"));
        assertTrue(resultStringSet.contains("two"));
        assertTrue(resultStringSet.contains("three"));
    }
}