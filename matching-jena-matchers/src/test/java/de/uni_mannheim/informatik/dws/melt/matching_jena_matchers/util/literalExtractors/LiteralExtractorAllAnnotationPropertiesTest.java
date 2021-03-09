package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.valueExtractors.ValueExtractorAllAnnotationProperties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Literal;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

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
}