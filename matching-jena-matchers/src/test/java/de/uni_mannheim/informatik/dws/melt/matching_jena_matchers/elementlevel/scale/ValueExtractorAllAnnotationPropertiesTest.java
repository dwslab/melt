package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorAllAnnotationProperties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ValueExtractorAllAnnotationPropertiesTest {


    @Test
    void extract() {
        TestCase anatomyTestCase = TrackRepository.Anatomy.Default.getFirstTestCase();
        OntModel humanOntology = anatomyTestCase.getTargetOntology(OntModel.class);

        OntResource nervousSystem = humanOntology.getOntResource("http://human.owl#NCI_C12438");

        TextExtractorAllAnnotationProperties extractor = new TextExtractorAllAnnotationProperties();

        Set<String> result = extractor.extract(nervousSystem);
        assertTrue(result.size() > 1);
        assertTrue(result.contains("The part of the nervous system that consists of the brain, spinal cord, and meninges."));
        assertTrue(result.contains("Central_Nervous_System"));

        // assert not null condition
        assertNotNull(extractor.extract(null));
    }

    @Test
    void filterAxiom(){
        // just making sure the filtering works as intended
        Set<String> mySet = new HashSet<>();
        mySet.add("Hello");
        mySet.add("  ");
        mySet.add("");
        assertTrue(mySet.stream().filter(x -> !x.trim().equals("")).collect(Collectors.toSet()).size() == 1);
    }
}