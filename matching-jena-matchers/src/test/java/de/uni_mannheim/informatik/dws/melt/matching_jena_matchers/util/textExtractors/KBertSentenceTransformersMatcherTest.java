package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.TextExtractorKBert;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert.KBertSentenceTransformersMatcher;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import static de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry.getTransformedObject;
import static de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry.getTransformedPropertiesOrNewInstance;

public class KBertSentenceTransformersMatcherTest {
    @Test
    public void testGenerateKBertInputOneTarget() throws Exception {
        // given
        TestCase testCase = TrackRepository.Anatomy.Default.getTestCase(0);
        URL parameters = testCase.getParameters().toURL();
        Properties properties = getTransformedPropertiesOrNewInstance(parameters);
        URL source = testCase.getSource().toURL();
        OntModel sourceOntology = getTransformedObject(source, OntModel.class, properties);
        KBertSentenceTransformersMatcher matcher = new KBertSentenceTransformersMatcher(
                new TextExtractorKBert(false), "paraphrase-MiniLM-L6-v2");
        File corpus = FileUtil.createFileWithRandomNumber("corpus", ".csv");

        // when
        matcher.createTextFile(sourceOntology, corpus, matcher.getResourcesExtractor().get(0), properties);

        // then
        System.out.println("");
    }
    @Test
    public void testGenerateKBertInputAllTargets() throws Exception {
        // given
        TestCase testCase = TrackRepository.Anatomy.Default.getTestCase(0);
        URL parameters = testCase.getParameters().toURL();
        Properties properties = getTransformedPropertiesOrNewInstance(parameters);
        URL source = testCase.getSource().toURL();
        OntModel sourceOntology = getTransformedObject(source, OntModel.class, properties);
        KBertSentenceTransformersMatcher matcher = new KBertSentenceTransformersMatcher(
                new TextExtractorKBert(true), "paraphrase-MiniLM-L6-v2");
        File corpus = FileUtil.createFileWithRandomNumber("corpus", ".csv");

        // when
        matcher.createTextFile(sourceOntology, corpus, matcher.getResourcesExtractor().get(0), properties);

        // then
        System.out.println("");
    }
    @Test
    public void testGenerateKBertInputAllTargetsQueries() throws Exception {
        // given
        TestCase testCase = TrackRepository.Anatomy.Default.getTestCase(0);
        URL parameters = testCase.getParameters().toURL();
        Properties properties = getTransformedPropertiesOrNewInstance(parameters);
        URL target = testCase.getTarget().toURL();
        OntModel targetOntology = getTransformedObject(target, OntModel.class, properties);
        KBertSentenceTransformersMatcher matcher = new KBertSentenceTransformersMatcher(
                new TextExtractorKBert(true), "paraphrase-MiniLM-L6-v2");
        File corpus = FileUtil.createFileWithRandomNumber("queries", ".csv");

        // when
        matcher.createTextFile(targetOntology, corpus, matcher.getResourcesExtractor().get(0), properties);

        // then
        System.out.println("");
    }
}
