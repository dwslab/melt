package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.TextExtractorKBert;
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
import java.util.stream.StreamSupport;

import static de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry.getTransformedObject;
import static de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry.getTransformedPropertiesOrNewInstance;
import static de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert.KBertSentenceTransformersMatcher.getIterable;
import static org.assertj.core.api.Assertions.assertThat;

class TextExtractorLongAndShortAnnotationPropertiesTest {
    @Test
    public void testExtract() throws MalformedURLException, TypeTransformationException {
        // Given
        TestCase testCase = TrackRepository.Anatomy.Default.getTestCase(0);
        URL parameters = testCase.getParameters().toURL();
        Properties properties = getTransformedPropertiesOrNewInstance(parameters);
        URL target = testCase.getTarget().toURL();
        OntModel targetOntology = getTransformedObject(target, OntModel.class, properties);
        KBertSentenceTransformersMatcher matcher = new KBertSentenceTransformersMatcher(
                new TextExtractorKBert(true), "paraphrase-MiniLM-L6-v2");
        Iterator<? extends OntResource> resourceIterator = matcher.getResourcesExtractor().get(0).extract(targetOntology, properties);
        OntResource resource = StreamSupport.stream(getIterable(resourceIterator).spliterator(), false)
                .filter(r -> {
                    return r.isURIResource() && r.getURI().equals("http://human.owl#NCI_C12519");
                }).findFirst().get();

        TextExtractorLongAndShortAnnotationProperties extractor = new TextExtractorLongAndShortAnnotationProperties();
        // When
        Map<String, Set<String>> asdf = extractor.extract(resource);
        // Then
        assertThat(asdf.get("short")).hasSize(4);
        assertThat(asdf.get("long")).hasSize(1);
    }
}