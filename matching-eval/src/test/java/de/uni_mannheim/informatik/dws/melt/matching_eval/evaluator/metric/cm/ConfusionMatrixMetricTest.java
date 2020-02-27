package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.MAC;

/**
 * ConfusionMatrixMetric TestCase
 * Note: When executing this test case, multiple parsing errors will be thrown. This is intended and ok if the tests pass.
 *
 * @author Jan Portisch
 */
class ConfusionMatrixMetricTest {

    @Test
    //@EnabledOnOs({ MAC })
    void computeExample_1() throws Exception {
        ConfusionMatrixMetric metric = new ConfusionMatrixMetric();

        Alignment referenceAlignment = new Alignment();
        referenceAlignment.add("http://www.example.com/entity_1", "http://www.loremIpsum.com/entity_a");
        referenceAlignment.add("http://www.example.com/entity_2", "http://www.loremIpsum.com/entity_b");
        referenceAlignment.add("http://www.example.com/entity_3", "http://www.loremIpsum.com/entity_c");
        referenceAlignment.add("http://www.example.com/entity_4", "http://www.loremIpsum.com/entity_d"); // fn

        Alignment systemAlignment = new Alignment();
        systemAlignment.add("http://www.example.com/entity_1", "http://www.loremIpsum.com/entity_a"); // tp
        systemAlignment.add("http://www.example.com/entity_2", "http://www.loremIpsum.com/entity_b"); // tp
        systemAlignment.add("http://www.example.com/entity_4", "http://www.loremIpsum.com/entity_c"); // fp

        ExecutionResult executionResult = new ExecutionResult(TrackRepository.Anatomy.Default.getTestCases().get(0), "myTestMatcher", systemAlignment, referenceAlignment);
        ConfusionMatrix confusionMatrix = metric.compute(executionResult);
        System.out.println("Precision: " + confusionMatrix.getPrecision());
        System.out.println("Recall: " + confusionMatrix.getRecall());
        System.out.println("F1: " + confusionMatrix.getF1measure());

        double precision = 2.0 / 3.0;
        double recall = 2.0 / 4.0;
        double f1 = (2 * precision * recall) / (precision + recall);

        assertEquals(precision, confusionMatrix.getPrecision());
        assertEquals(recall, confusionMatrix.getRecall());
        assertEquals(f1, confusionMatrix.getF1measure());
    }


    @Test
    //@EnabledOnOs({ MAC })
    void computeExample_2() throws Exception {
        ConfusionMatrixMetric metric = new ConfusionMatrixMetric();

        Alignment referenceAlignment = new Alignment();
        referenceAlignment.add("http://www.example.com/entity_1", "http://www.loremIpsum.com/entity_a");
        referenceAlignment.add("http://www.example.com/entity_2", "http://www.loremIpsum.com/entity_b");
        referenceAlignment.add("http://www.example.com/entity_3", "http://www.loremIpsum.com/entity_c");
        referenceAlignment.add("http://www.example.com/entity_4", "http://www.loremIpsum.com/entity_d"); // fn

        Alignment systemAlignment = new Alignment();
        systemAlignment.add("http://www.example.com/entity_1", "http://www.loremIpsum.com/entity_a"); // tp
        systemAlignment.add("http://www.example.com/entity_2", "http://www.loremIpsum.com/entity_b"); // tp
        systemAlignment.add("http://www.example.com/entity_4", "http://www.loremIpsum.com/entity_c"); // fp
        systemAlignment.add("http://www.example.com/entity_5", "http://www.loremIpsum.com/entity_z"); // fp

        ExecutionResult executionResult = new ExecutionResult(TrackRepository.Anatomy.Default.getTestCases().get(0), "myTestMatcher", systemAlignment, referenceAlignment);
        ConfusionMatrix confusionMatrix = metric.compute(executionResult);
        System.out.println("Precision: " + confusionMatrix.getPrecision());
        System.out.println("Recall: " + confusionMatrix.getRecall());
        System.out.println("F1: " + confusionMatrix.getF1measure());

        double precision = 2.0 / 4.0;
        double recall = 2.0 / 4.0;
        double f1 = (2 * precision * recall) / (precision + recall);

        assertEquals(precision, confusionMatrix.getPrecision());
        assertEquals(recall, confusionMatrix.getRecall());
        assertEquals(f1, confusionMatrix.getF1measure());
    }

    @Test
    //@EnabledOnOs({ MAC })
    void getMicroAverages() throws Exception {
        ConfusionMatrixMetric metric = new ConfusionMatrixMetric();

        Alignment referenceAlignment1 = new Alignment();
        referenceAlignment1.add("http://www.example.com/entity_1", "http://www.loremIpsum.com/entity_a");
        referenceAlignment1.add("http://www.example.com/entity_2", "http://www.loremIpsum.com/entity_b");
        referenceAlignment1.add("http://www.example.com/entity_3", "http://www.loremIpsum.com/entity_c");
        referenceAlignment1.add("http://www.example.com/entity_4", "http://www.loremIpsum.com/entity_d"); // fn

        Alignment systemAlignment1 = new Alignment();
        systemAlignment1.add("http://www.example.com/entity_1", "http://www.loremIpsum.com/entity_a"); // tp
        systemAlignment1.add("http://www.example.com/entity_2", "http://www.loremIpsum.com/entity_b"); // tp
        systemAlignment1.add("http://www.example.com/entity_4", "http://www.loremIpsum.com/entity_c"); // fp
        systemAlignment1.add("http://www.example.com/entity_5", "http://www.loremIpsum.com/entity_z"); // fp


        Alignment referenceAlignment2 = new Alignment();
        referenceAlignment2.add("http://www.example.com/entity_1", "http://www.loremIpsumDolor.com/entity_a");
        referenceAlignment2.add("http://www.example.com/entity_2", "http://www.loremIpsumDolor.com/entity_b");
        referenceAlignment2.add("http://www.example.com/entity_3", "http://www.loremIpsumDolor.com/entity_c");
        referenceAlignment2.add("http://www.example.com/entity_4", "http://www.loremIpsumDolor.com/entity_d"); // fn


        Alignment systemAlignment2 = new Alignment();
        systemAlignment2.add("http://www.example.com/entity_1", "http://www.loremIpsumDolor.com/entity_a"); // tp
        systemAlignment2.add("http://www.example.com/entity_2", "http://www.loremIpsumDolor.com/entity_b"); // tp
        systemAlignment2.add("http://www.example.com/entity_4", "http://www.loremIpsumDolor.com/entity_c"); // fp

        ExecutionResult executionResult1 = new ExecutionResult(TrackRepository.Anatomy.Default.getTestCases().get(0), "myTestMatcher", systemAlignment1, referenceAlignment1);
        ExecutionResult executionResult2 = new ExecutionResult(TrackRepository.Anatomy.Default.getTestCases().get(0), "myTestMatcher", systemAlignment2, referenceAlignment2);

        ConfusionMatrix confusionMatrix1 = metric.compute(executionResult1);
        ConfusionMatrix confusionMatrix2 = metric.compute(executionResult2);

        // testing matrix 1
        double precision_1 = 2.0 / 4.0;
        double recall_1 = 2.0 / 4.0;
        double f1_1 = (2 * precision_1 * recall_1) / (precision_1 + recall_1);
        assertEquals(precision_1, confusionMatrix1.getPrecision());
        assertEquals(recall_1, confusionMatrix1.getRecall());
        assertEquals(f1_1, confusionMatrix1.getF1measure());

        // testing matrix 2
        double precision_2 = 2.0 / 3.0;
        double recall_2 = 2.0 / 4.0;
        double f1_2 = (2 * precision_2 * recall_2) / (precision_2 + recall_2);
        assertEquals(precision_2, confusionMatrix2.getPrecision());
        assertEquals(recall_2, confusionMatrix2.getRecall());
        assertEquals(f1_2, confusionMatrix2.getF1measure());

        // testing micro average
        ArrayList<ConfusionMatrix> confusionMatrices = new ArrayList<>();
        confusionMatrices.add(confusionMatrix1);
        confusionMatrices.add(confusionMatrix2);
        double precisionMicro = 4.0 / (4 + 3);
        double recallMicro = 4.0 / (4 + 4);
        double f1Micro = (2 * precisionMicro * recallMicro) / (precisionMicro + recallMicro);

        ConfusionMatrix confusionMatrixMicro = metric.getMicroAverages(confusionMatrices);
        assertEquals(precisionMicro, confusionMatrixMicro.getPrecision());
        assertEquals(recallMicro, confusionMatrixMicro.getRecall());
        assertEquals(f1Micro, confusionMatrixMicro.getF1measure());
    }

    @Test
    //@EnabledOnOs({ MAC })
    void getMacroAverages() throws Exception {
        ConfusionMatrixMetric metric = new ConfusionMatrixMetric();

        Alignment referenceAlignment1 = new Alignment();
        referenceAlignment1.add("http://www.example.com/entity_1", "http://www.loremIpsum.com/entity_a");
        referenceAlignment1.add("http://www.example.com/entity_2", "http://www.loremIpsum.com/entity_b");
        referenceAlignment1.add("http://www.example.com/entity_3", "http://www.loremIpsum.com/entity_c");
        referenceAlignment1.add("http://www.example.com/entity_4", "http://www.loremIpsum.com/entity_d"); // fn

        Alignment systemAlignment1 = new Alignment();
        systemAlignment1.add("http://www.example.com/entity_1", "http://www.loremIpsum.com/entity_a"); // tp
        systemAlignment1.add("http://www.example.com/entity_2", "http://www.loremIpsum.com/entity_b"); // tp
        systemAlignment1.add("http://www.example.com/entity_4", "http://www.loremIpsum.com/entity_c"); // fp
        systemAlignment1.add("http://www.example.com/entity_5", "http://www.loremIpsum.com/entity_z"); // fp


        Alignment referenceAlignment2 = new Alignment();
        referenceAlignment2.add("http://www.example.com/entity_1", "http://www.loremIpsumDolor.com/entity_a");
        referenceAlignment2.add("http://www.example.com/entity_2", "http://www.loremIpsumDolor.com/entity_b");
        referenceAlignment2.add("http://www.example.com/entity_3", "http://www.loremIpsumDolor.com/entity_c");
        referenceAlignment2.add("http://www.example.com/entity_4", "http://www.loremIpsumDolor.com/entity_d"); // fn


        Alignment systemAlignment2 = new Alignment();
        systemAlignment2.add("http://www.example.com/entity_1", "http://www.loremIpsumDolor.com/entity_a"); // tp
        systemAlignment2.add("http://www.example.com/entity_2", "http://www.loremIpsumDolor.com/entity_b"); // tp
        systemAlignment2.add("http://www.example.com/entity_4", "http://www.loremIpsumDolor.com/entity_c"); // fp

        ExecutionResult executionResult1 = new ExecutionResult(TrackRepository.Anatomy.Default.getTestCases().get(0), "myTestMatcher", systemAlignment1, referenceAlignment1);
        ExecutionResult executionResult2 = new ExecutionResult(TrackRepository.Anatomy.Default.getTestCases().get(0), "myTestMatcher", systemAlignment2, referenceAlignment2);


        ConfusionMatrix confusionMatrix1 = metric.compute(executionResult1);
        ConfusionMatrix confusionMatrix2 = metric.compute(executionResult2);

        // testing matrix 1
        double precision_1 = 2.0 / 4.0;
        double recall_1 = 2.0 / 4.0;
        double f1_1 = (2 * precision_1 * recall_1) / (precision_1 + recall_1);
        assertEquals(precision_1, confusionMatrix1.getPrecision());
        assertEquals(recall_1, confusionMatrix1.getRecall());
        assertEquals(f1_1, confusionMatrix1.getF1measure());

        // testing matrix 2
        double precision_2 = 2.0 / 3.0;
        double recall_2 = 2.0 / 4.0;
        double f1_2 = (2 * precision_2 * recall_2) / (precision_2 + recall_2);
        assertEquals(precision_2, confusionMatrix2.getPrecision());
        assertEquals(recall_2, confusionMatrix2.getRecall());
        assertEquals(f1_2, confusionMatrix2.getF1measure());

        // testing micro average
        ArrayList<ConfusionMatrix> confusionMatrices = new ArrayList<>();
        confusionMatrices.add(confusionMatrix1);
        confusionMatrices.add(confusionMatrix2);
        double precisionMacro = (precision_1 + precision_2) / 2.0;
        double recallMacro = (recall_1 + recall_2) / 2.0;
        double f1Macro = (2 * precisionMacro * recallMacro) / (precisionMacro + recallMacro);

        ConfusionMatrix confusionMatrixMicro = metric.getMacroAverages(confusionMatrices);
        assertEquals(precisionMacro, confusionMatrixMicro.getPrecision());
        assertEquals(recallMacro, confusionMatrixMicro.getRecall());
        assertEquals(f1Macro, confusionMatrixMicro.getF1measure());
    }


    /**
     * This tests uses the 2018 alignment files for DOME and ALOD2Vec and evaluates them on the OAEI Anatomy data set
     * and compares the MELT results with those given on the Web Page (http://oaei.ontologymatching.org/2018/results/anatomy/index.html).
     **/
    @Test
    //@EnabledOnOs({ MAC })
    void realTest() {
        TestCase testCase = TrackRepository.Anatomy.Default.getTestCases().get(0);
        ExecutionResultSet resultSet = Executor.loadFromFolder("./src/test/resources/externalAlignmentForEvaluation", testCase);
        ConfusionMatrixMetric metric = new ConfusionMatrixMetric();
        ExecutionResult alodResult = resultSet.get(testCase, "ALOD2Vec");
        ExecutionResult domeResult = resultSet.get(testCase, "DOME");

        assertNotNull(alodResult);
        assertNotNull(domeResult);

        ConfusionMatrix confusionMatrix1Alod = metric.compute(alodResult);
        assertEquals(0.785, confusionMatrix1Alod.getF1measure(), 0.001);
        //assertEquals(0.996, confusionMatrix1Alod.getPrecision(), 0.001);
        assertEquals(0.648, confusionMatrix1Alod.getRecall(), 0.001);

        ConfusionMatrix confusionMatrix1Dome = metric.compute(domeResult);
        assertEquals(0.761, confusionMatrix1Dome.getF1measure(), 0.001);
        //assertEquals(0.997, confusionMatrix1Dome.getPrecision(), 0.001);
        assertEquals(0.615, confusionMatrix1Dome.getRecall(), 0.001);
    }

}