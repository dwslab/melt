package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert.KBertSentenceTransformersMatcher;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.StreamSupport;

import static de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry.getTransformedObject;
import static de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry.getTransformedPropertiesOrNewInstance;
import static de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert.KBertSentenceTransformersMatcher.getIterable;

class TextExtractorKBertTest {
    @Test
    public void testExtract() throws MalformedURLException, TypeTransformationException {
        // Given
        TestCase testCase = TrackRepository.Anatomy.Default.getTestCase(0);
        URL parameters = testCase.getParameters().toURL();
        Properties properties = getTransformedPropertiesOrNewInstance(parameters);
        URL target = testCase.getTarget().toURL();
        OntModel targetOntology = getTransformedObject(target, OntModel.class, properties);
        KBertSentenceTransformersMatcher matcher = new KBertSentenceTransformersMatcher(
                new TextExtractorKBert(true, true), "paraphrase-MiniLM-L6-v2");
        TextExtractorMap simpleTextExtractor = matcher.getExtractorMap();
        Iterator<? extends OntResource> resourceIterator = matcher.getResourcesExtractor().get(0).extract(targetOntology, properties);
        OntResource resource = StreamSupport.stream(getIterable(resourceIterator).spliterator(), false)
                .filter(r -> {
                    return r.isURIResource() && r.getURI().equals("http://human.owl#NCI_C12519");
                }).findFirst().get();
        // When
        Map<String, Set<String>> asdf = simpleTextExtractor.extract(resource);
        // Then
        System.out.println("");
    }
}