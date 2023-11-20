package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm;

import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_data.LocalTrack;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfusionMatrixMetric TestCase
 * Note: When executing this test case, multiple parsing errors will be thrown. This is intended and ok if the tests pass.
 *
 * @author Jan Portisch
 */
class ConfusionMatrixMetricTest {


    @Test
    void computeExample_1() {
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
        double f1Macro = (confusionMatrix1.getF1measure() + confusionMatrix2.getF1measure()) / 2.0d;

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

    
    @Test
    void micromacroTest() {
        ConfusionMatrixMetric metric = new ConfusionMatrixMetric();
        double delta = 0.01;
        //https://iamirmasoud.com/2022/06/19/understanding-micro-macro-and-weighted-averages-for-scikit-learn-metrics-in-multi-class-classification-with-example/
        ExecutionResult resultFirst = createResultWith("testCaseA", GoldStandardCompleteness.COMPLETE,
                    2,1,1,0,0,0,0,0,0);
        ExecutionResult resultSecond = createResultWith("testCaseB", GoldStandardCompleteness.COMPLETE,
                    1,3,0,0,0,0,0,0,0);
        ExecutionResult resultThird = createResultWith("testCaseC", GoldStandardCompleteness.COMPLETE,
                    3,0,3,0,0,0,0,0,0);
        ExecutionResultSet all = new ExecutionResultSet();
        all.add(resultFirst);
        all.add(resultSecond);
        all.add(resultThird);
        
        ConfusionMatrix confusionMatrixFirst = metric.compute(resultFirst);
        ConfusionMatrix confusionMatrixSecond = metric.compute(resultSecond);
        ConfusionMatrix confusionMatrixThird = metric.compute(resultThird);
        
        assertEquals(2, confusionMatrixFirst.getTruePositiveSize());
        assertEquals(1, confusionMatrixFirst.getFalsePositiveSize());
        assertEquals(1, confusionMatrixFirst.getFalseNegativeSize());
        assertEquals(0.67, confusionMatrixFirst.getPrecision(), delta);
        assertEquals(0.67, confusionMatrixFirst.getRecall(), delta);
        assertEquals(0.67, confusionMatrixFirst.getF1measure(), delta);
        
        
        assertEquals(1, confusionMatrixSecond.getTruePositiveSize());
        assertEquals(3, confusionMatrixSecond.getFalsePositiveSize());
        assertEquals(0, confusionMatrixSecond.getFalseNegativeSize());
        assertEquals(0.25, confusionMatrixSecond.getPrecision(), delta);
        assertEquals(1.0, confusionMatrixSecond.getRecall(), delta);
        assertEquals(0.4, confusionMatrixSecond.getF1measure(), delta);
        
        assertEquals(3, confusionMatrixThird.getTruePositiveSize());
        assertEquals(0, confusionMatrixThird.getFalsePositiveSize());
        assertEquals(3, confusionMatrixThird.getFalseNegativeSize());
        assertEquals(1.0, confusionMatrixThird.getPrecision(), delta);
        assertEquals(0.5, confusionMatrixThird.getRecall(), delta);
        assertEquals(0.67, confusionMatrixThird.getF1measure(), delta);
        
        
        
        ConfusionMatrix microAll = metric.getMicroAveragesForResults(all); 
        
        assertEquals(6, microAll.getTruePositiveSize());
        assertEquals(4, microAll.getFalsePositiveSize());
        assertEquals(4, microAll.getFalseNegativeSize());
        assertEquals(0.6, microAll.getPrecision(), delta);
        assertEquals(0.6, microAll.getRecall(), delta);
        assertEquals(0.6, microAll.getF1measure(), delta);
        
        
        ConfusionMatrix macroAll = metric.getMacroAveragesForResults(all);
        assertEquals(6, macroAll.getTruePositiveSize());
        assertEquals(4, macroAll.getFalsePositiveSize());
        assertEquals(4, macroAll.getFalseNegativeSize());
        assertEquals(0.64, macroAll.getPrecision(), delta);
        assertEquals(0.72, macroAll.getRecall(), delta);
        assertEquals(0.58, macroAll.getF1measure(), delta);
        
        ConfusionMatrix macroSpecifiedNumber = metric.getMacroAveragesForResults(all, 3);
        assertEquals(6, macroSpecifiedNumber.getTruePositiveSize());
        assertEquals(4, macroSpecifiedNumber.getFalsePositiveSize());
        assertEquals(4, macroSpecifiedNumber.getFalseNegativeSize());
        assertEquals(0.64, macroSpecifiedNumber.getPrecision(), delta);
        assertEquals(0.72, macroSpecifiedNumber.getRecall(), delta);
        assertEquals(0.58, macroSpecifiedNumber.getF1measure(), delta);
    }
    
    
    
    
    
    private static ExecutionResult createResultWith(String testCase, GoldStandardCompleteness goldStandardCompleteness, 
                                                    int classTP, int classFP, int classFN,
                                                    int propTP, int propFP, int propFN,
                                                    int instTP, int instFP, int instFN){
        int counter = 0;
        String sourceBase = "http://source.com/" + testCase + "/";
        String targetBase = "http://target.com/" + testCase + "/";
        
        Alignment systemAlignment = new Alignment();
        Alignment refAlignment = new Alignment();
        
        OntModel src = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        OntModel tgt = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        
        //TP
        for(int i = 0; i < classTP; i++){
            String sourceURI = sourceBase + counter++;
            String targetURI = targetBase + counter++;
            src.createClass(sourceURI);
            tgt.createClass(targetURI);
            systemAlignment.add(sourceURI, targetURI);
            refAlignment.add(sourceURI, targetURI);
        }
        for(int i = 0; i < propTP; i++){
            String sourceURI = sourceBase + counter++;
            String targetURI = targetBase + counter++;
            src.createProperty(sourceURI);
            tgt.createProperty(targetURI);
            systemAlignment.add(sourceURI, targetURI);
            refAlignment.add(sourceURI, targetURI);
        }
        for(int i = 0; i < instTP; i++){
            String sourceURI = sourceBase + counter++;
            String targetURI = targetBase + counter++;
            src.createIndividual(sourceURI, OWL.Thing);
            tgt.createIndividual(targetURI, OWL.Thing);
            systemAlignment.add(sourceURI, targetURI);
            refAlignment.add(sourceURI, targetURI);
        }
        
        //FP
        for(int i = 0; i < classFP; i++){
            String sourceURI = sourceBase + counter++;
            String targetURI = targetBase + counter++;
            src.createClass(sourceURI);
            tgt.createClass(targetURI);
            systemAlignment.add(sourceURI, targetURI);
        }
        for(int i = 0; i < propFP; i++){
            String sourceURI = sourceBase + counter++;
            String targetURI = targetBase + counter++;
            src.createProperty(sourceURI);
            tgt.createProperty(targetURI);
            systemAlignment.add(sourceURI, targetURI);
        }
        for(int i = 0; i < instFP; i++){
            String sourceURI = sourceBase + counter++;
            String targetURI = targetBase + counter++;
            src.createIndividual(sourceURI, OWL.Thing);
            tgt.createIndividual(targetURI, OWL.Thing);
            systemAlignment.add(sourceURI, targetURI);
        }
        
        //FN
        for(int i = 0; i < classFN; i++){
            String sourceURI = sourceBase + counter++;
            String targetURI = targetBase + counter++;
            src.createClass(sourceURI);
            tgt.createClass(targetURI);
            refAlignment.add(sourceURI, targetURI);
        }
        for(int i = 0; i < propFN; i++){
            String sourceURI = sourceBase + counter++;
            String targetURI = targetBase + counter++;
            src.createProperty(sourceURI);
            tgt.createProperty(targetURI);
            refAlignment.add(sourceURI, targetURI);
        }
        for(int i = 0; i < instFN; i++){
            String sourceURI = sourceBase + counter++;
            String targetURI = targetBase + counter++;
            src.createIndividual(sourceURI, OWL.Thing);
            tgt.createIndividual(targetURI, OWL.Thing);
            refAlignment.add(sourceURI, targetURI);
        }
        
        
        LocalTrack track = new LocalTrack("testtrack", "1.0");
        TestCase tc = new TestCaseWithModel(testCase, src, tgt, refAlignment, track, goldStandardCompleteness);
        
        return new ExecutionResult(tc, "myTestMatcher", systemAlignment, refAlignment);
    }
}