package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.TextExtractorKBert;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert.KBertSentenceTransformersMatcher;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import static de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry.getTransformedObject;
import static de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry.getTransformedPropertiesOrNewInstance;
import static de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer.PYTHON_DIRECTORY_NAME;

public class KBertSentenceTransformersMatcherTest {

    @Test
    public void testGenerateKBertInputVariations() throws Exception {
        Map<Boolean, String> normalizedMap = Map.of(true, "normalized", false, "raw");
        Map<Boolean, String> allTargetsMap = Map.of(true, "all_targets", false, "one_target");
        File rootFile = new File(
                new File(
                        this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()
                ).getParentFile().getParentFile().getParentFile(),
                "matching-ml-python/" + PYTHON_DIRECTORY_NAME + "/kbert/test/resources/kbert"
        );
        TestCase testCase = TrackRepository.Anatomy.Default.getTestCase(0);
        URL parameters = testCase.getParameters().toURL();
        Properties properties = getTransformedPropertiesOrNewInstance(parameters);
        for (Boolean normalized : Arrays.asList(true, false)) {
            for (Boolean allTargets : Arrays.asList(true, false)) {
                for (Map.Entry<String, URI> entry : Map.of("corpus", testCase.getSource(), "queries", testCase.getTarget()).entrySet()) {
                    URL source = entry.getValue().toURL();
                    OntModel sourceOntology = getTransformedObject(source, OntModel.class, properties);
                    KBertSentenceTransformersMatcher matcher = new KBertSentenceTransformersMatcher(
                            new TextExtractorKBert(allTargets, normalized), "paraphrase-MiniLM-L6-v2");
                    File targetFile = new File(rootFile, normalizedMap.get(normalized) + "/" + allTargetsMap.get(allTargets) + "/" + entry.getKey() + ".csv");

                    // when
                    matcher.createTextFile(sourceOntology, targetFile, matcher.getResourcesExtractor().get(0), properties);
                }
            }
        }
        // given


        // then
        System.out.println("");
    }
}
