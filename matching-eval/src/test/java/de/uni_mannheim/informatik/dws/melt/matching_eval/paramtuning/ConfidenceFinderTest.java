package de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning;

import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrix;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrixMetric;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.xml.sax.SAXException;

import static org.junit.jupiter.api.Assertions.*;

public class ConfidenceFinderTest {


    /**
     * Problematic real-life test. Nothing should be changed here.
     */
    @Test
    void realLifeTest(){
        File alignmentFile = loadFile("threshold_optimize_conference_confOf-de-en.rdf");
        try {
            Alignment alignment = AlignmentParser.parse(alignmentFile);
            TestCase testCase = null;
            for(Track track : TrackRepository.Multifarm.getMultifarmTrackForLanguage("de-en")){
                testCase = track.getTestCase("conference-confOf-de-en");
            }
            assertNotNull(testCase);
            ExecutionResult beforeOptimizing = new ExecutionResult(testCase, "before", alignment, testCase.getParsedReferenceAlignment());
            ConfusionMatrixMetric confusionMatrixMetric = new ConfusionMatrixMetric();
            double f1before = confusionMatrixMetric.compute(beforeOptimizing).getF1measure();

            double optimalThreshold = ConfidenceFinder.getBestConfidenceForFmeasure(beforeOptimizing.getReferenceAlignment(),
                    alignment, GoldStandardCompleteness.COMPLETE, 100);
            ConfidenceFilter filter = new ConfidenceFilter(optimalThreshold);
            ExecutionResult afterOptimizing = new ExecutionResult(
                    testCase,
                    "after",
                    filter.filter(
                    alignment, testCase.getSourceOntology(OntModel.class), testCase.getTargetOntology(OntModel.class)),
                    testCase.getParsedReferenceAlignment()
            );

            double f1after = confusionMatrixMetric.compute(afterOptimizing).getF1measure();

            if(!(f1after >= f1before)){
                fail("F1 before: " + f1before + "   F1 after: " + f1after);
            }

        } catch (Exception e) {
           fail("An exception should not occur.", e);
        }
    }

    @Test
    public void getBestConfidenceForFMeasureIncomplete() {
        Alignment reference = new Alignment();
        reference.add("A", "A");
        reference.add("B", "B");
        reference.add("C", "C");

        Alignment system = new Alignment();

        // correct
        system.add("A", "A", 1.0);
        system.add("B", "B", 0.8);

        // cannot be judged
        system.add("D", "D", 0.9);
        system.add("E", "E", 0.9);

        // wrong
        system.add("A", "B", 0.7);

        double result = ConfidenceFinder.getBestConfidenceForFmeasure(reference, system,
                GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE);
        Assertions.assertEquals(0.8, result);
    }

    @Test
    public void getBestconfidenceAllSame() {
        Alignment reference = new Alignment();
        reference.add("A", "A");
        reference.add("B", "B");
        reference.add("C", "C");

        Alignment system = new Alignment();

        // correct
        system.add("A", "A", 0.8192879);
        system.add("B", "C", 0.8192879);
        system.add("D", "C", 0.8192879);

        double result = ConfidenceFinder.getBestConfidenceForFmeasure(reference, system,
                GoldStandardCompleteness.COMPLETE);


        Assertions.assertEquals(0.82, Math.round(result * 100.0) / 100.0 );
    }

    @Test
    public void getBestConfidenceForFMeasureIncomplete2() {
        Alignment reference = new Alignment();
        reference.add("A", "A");
        reference.add("B", "B");
        reference.add("C", "C");

        Alignment system = new Alignment();

        // correct
        system.add("A", "A", 1.0);
        system.add("B", "B", 0.5);

        // cannot be judged
        system.add("D", "D", 0.9);
        system.add("E", "E", 0.9);

        // wrong
        system.add("A", "B", 0.7);
        system.add("B", "C", 0.7);
        system.add("A", "C", 0.7);

        double result = ConfidenceFinder.getBestConfidenceForFmeasure(reference, system,
                GoldStandardCompleteness.PARTIAL_SOURCE_INCOMPLETE_TARGET_INCOMPLETE);
        Assertions.assertEquals(0.5, result);

        result = ConfidenceFinder.getBestConfidenceForFmeasure(reference, system,
                GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE);
        Assertions.assertEquals(1.0, result);
    }

    @Test
    public void getBestConfidenceForFMeasure() {
        Alignment myAlignment = new Alignment();

        // correct
        myAlignment.add("http://mouse.owl#MA_0002401", "http://human.owl#NCI_C52561", 1.0);
        myAlignment.add("http://mouse.owl#MA_0000270", "http://human.owl#NCI_C33736", 0.9);

        // incorrect
        myAlignment.add("FALSE", "FALSE", 0.8);

        // build the test case
        TestCase anatomyTc = TrackRepository.Anatomy.Default.getFirstTestCase();
        ExecutionResult er = new ExecutionResult(anatomyTc,
                "name",
                myAlignment,
                anatomyTc.getParsedReferenceAlignment());

        double result = ConfidenceFinder.getBestConfidenceForFmeasure(er);
        Assertions.assertEquals(0.9, result);
    }

    /**
     * Helper function to load files in class path that contain spaces.
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private File loadFile(String fileName){
        try {
            URL resultUri =  this.getClass().getClassLoader().getResource(fileName);
            assertNotNull(resultUri);
            File result = FileUtils.toFile(resultUri.toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception){
            exception.printStackTrace();
            fail("Could not load file.", exception);
            return null;
        }
    }
}
