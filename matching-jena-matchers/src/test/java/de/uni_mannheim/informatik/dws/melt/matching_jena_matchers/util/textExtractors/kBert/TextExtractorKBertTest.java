package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert.KBertSentenceTransformersMatcher;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry.getTransformedObject;
import static de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry.getTransformedPropertiesOrNewInstance;
import static de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert.KBertSentenceTransformersMatcher.getIterable;
import static de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert.KBertSentenceTransformersMatcher.streamFromIterator;
import static org.assertj.core.api.Assertions.assertThat;

class TextExtractorKBertTest {
    @Test
    public void testExtract() throws MalformedURLException, TypeTransformationException {
        // Given
        TestCase testCase = TrackRepository.Anatomy.Default.getTestCase(0);
        URL parameters = testCase.getParameters().toURL();
        Properties properties = getTransformedPropertiesOrNewInstance(parameters);
        URL target = testCase.getTarget().toURL();
        OntModel targetOntology = getTransformedObject(target, OntModel.class, properties);
        TextExtractorKBertImpl extractor = new TextExtractorKBertImpl(true, true);
        KBertSentenceTransformersMatcher matcher = new KBertSentenceTransformersMatcher(
                extractor, "paraphrase-MiniLM-L6-v2");
        Iterator<? extends OntResource> resourceIterator = matcher.getResourcesExtractor().get(0).extract(targetOntology, properties);
        OntResource resource = StreamSupport.stream(getIterable(resourceIterator).spliterator(), false)
                .filter(r -> r.isURIResource() && r.getURI().equals("http://human.owl#NCI_C12519")).findFirst().get();
        // When
        Set<String> asdf = extractor.extract(resource);
        // Then
        System.out.println("");
    }

    @Test
    public void testNoDuplicateStatements() throws MalformedURLException, TypeTransformationException, JsonProcessingException {
        // Given
        String uriOfResourceWithDuplicateStatements = "http://human.owl#NCI_C12664";
        TestCase testCase = TrackRepository.Anatomy.Default.getTestCase(0);
        URL parameters = testCase.getParameters().toURL();
        Properties properties = getTransformedPropertiesOrNewInstance(parameters);
        URL target = testCase.getTarget().toURL();
        OntModel targetOntology = getTransformedObject(target, OntModel.class, properties);
        KBertSentenceTransformersMatcher matcher = new KBertSentenceTransformersMatcher(
                new TextExtractorKBertImpl(true, true), "paraphrase-MiniLM-L6-v2");
        TextExtractorKBertImpl simpleTextExtractor = new TextExtractorKBertImpl(false, true);
        Iterator<? extends OntResource> resourceIterator = matcher.getResourcesExtractor().get(0).extract(targetOntology, properties);
        OntResource resource = streamFromIterator(resourceIterator)
                .filter(r -> r.isURIResource() && r.getURI().equals(uriOfResourceWithDuplicateStatements))
                .findFirst()
                .get();
        // When
        Map<String, Object> molecule = simpleTextExtractor.moleculeFromResource(resource);
        // Then
        assertThat((Set) molecule.get("s")).hasSize(3);
    }

    @Test
    public void testMultipleTargets() throws MalformedURLException, TypeTransformationException {
        // Given
        String uriOfResourceWithDuplicateStatements = "http://human.owl#NCI_C12393";
        TestCase testCase = TrackRepository.Anatomy.Default.getTestCase(0);
        URL parameters = testCase.getParameters().toURL();
        Properties properties = getTransformedPropertiesOrNewInstance(parameters);
        URL target = testCase.getTarget().toURL();
        OntModel targetOntology = getTransformedObject(target, OntModel.class, properties);
        TextExtractorKBertImpl textExtractor = new TextExtractorKBertImpl(true, true);
        KBertSentenceTransformersMatcher matcher = new KBertSentenceTransformersMatcher(
                textExtractor, "paraphrase-MiniLM-L6-v2");
        Iterator<? extends OntResource> resourceIterator = matcher.getResourcesExtractor().get(0).extract(targetOntology, properties);
        OntResource resource = streamFromIterator(resourceIterator)
                .filter(r -> r.isURIResource() && r.getURI().equals(uriOfResourceWithDuplicateStatements))
                .findFirst()
                .get();
        // When
        Map<String, Object> molecule = textExtractor.moleculeFromResource(resource);
        // Then
        assertThat((Set) molecule.get("t")).hasSize(2);
    }

    @Test
    public void testGetIndexStream() throws MalformedURLException, TypeTransformationException {
        // Given
        String uriOfResourceWithDuplicateStatements = "http://human.owl#NCI_C12499";
        TestCase testCase = TrackRepository.Anatomy.Default.getTestCase(0);
        URL parameters = testCase.getParameters().toURL();
        Properties properties = getTransformedPropertiesOrNewInstance(parameters);
        URL target = testCase.getTarget().toURL();
        OntModel targetOntology = getTransformedObject(target, OntModel.class, properties);
        TextExtractorKBertImpl textExtractor = new TextExtractorKBertImpl(true, true);
        KBertSentenceTransformersMatcher matcher = new KBertSentenceTransformersMatcher(
                textExtractor, "paraphrase-MiniLM-L6-v2");
        Iterator<? extends OntResource> resourceIterator = matcher.getResourcesExtractor().get(0).extract(targetOntology, properties);
        // When
        Stream<String> molecule = textExtractor.getIndexStream(resourceIterator);
        // Then
        System.out.println("");
    }
}
