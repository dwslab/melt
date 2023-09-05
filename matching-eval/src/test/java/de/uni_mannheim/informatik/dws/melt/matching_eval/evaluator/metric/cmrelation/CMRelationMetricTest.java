package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cmrelation;

import de.uni_mannheim.informatik.dws.melt.matching_data.LocalTrack;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrix;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrixMetric;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.File;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author shertlin
 */
public class CMRelationMetricTest {
    
    
    @Test
    void enhanceAlignmentTest() {
        
        Model source = ModelFactory.createDefaultModel();
        String uriA = "http://source.com/A";
        String uriB = "http://source.com/B";
        String uriC = "http://source.com/C";
        
        Resource A = source.createResource(uriA);
        Resource B = source.createResource(uriB);
        Resource C = source.createResource(uriC);
        
        A.addProperty(RDFS.subClassOf, B);
        B.addProperty(RDFS.subClassOf, C);
        
        
        Model target = ModelFactory.createDefaultModel();
        String uriOne = "http://target.com/one";
        String uriTwo = "http://target.com/two";
        String uriThree = "http://target.com/three";
        
        Resource one = target.createResource(uriOne);
        Resource two = target.createResource(uriTwo);
        Resource three = target.createResource(uriThree);
        
        one.addProperty(RDFS.subClassOf, two);
        two.addProperty(RDFS.subClassOf, three);        
        
        
        //first test
        Alignment alignment = new Alignment();
        alignment.add(uriA, uriTwo, CorrespondenceRelation.SUBSUMED);
        
        Alignment enhanced = CMRelationMetric.enhanceAlignment(source, target, alignment);
        assertEquals(2, enhanced.size());
        assertNotNull(enhanced.getCorrespondence(uriA, uriTwo, CorrespondenceRelation.SUBSUMED));
        assertNotNull(enhanced.getCorrespondence(uriA, uriThree, CorrespondenceRelation.SUBSUMED));
        
        
        //second test
        alignment = new Alignment();
        alignment.add(uriB, uriTwo, CorrespondenceRelation.EQUIVALENCE);
        
        enhanced = CMRelationMetric.enhanceAlignment(source, target, alignment);
        assertEquals(9, enhanced.size());
        assertNotNull(enhanced.getCorrespondence(uriA, uriTwo, CorrespondenceRelation.SUBSUMED));
        assertNotNull(enhanced.getCorrespondence(uriA, uriThree, CorrespondenceRelation.SUBSUMED));
        
        assertNotNull(enhanced.getCorrespondence(uriB, uriTwo, CorrespondenceRelation.EQUIVALENCE));
        assertNotNull(enhanced.getCorrespondence(uriB, uriTwo, CorrespondenceRelation.SUBSUME));
        assertNotNull(enhanced.getCorrespondence(uriB, uriTwo, CorrespondenceRelation.SUBSUMED));
        assertNotNull(enhanced.getCorrespondence(uriB, uriThree, CorrespondenceRelation.SUBSUMED));
        assertNotNull(enhanced.getCorrespondence(uriB, uriOne, CorrespondenceRelation.SUBSUME));
        
        assertNotNull(enhanced.getCorrespondence(uriC, uriTwo, CorrespondenceRelation.SUBSUME));
        assertNotNull(enhanced.getCorrespondence(uriC, uriOne, CorrespondenceRelation.SUBSUME));
    }
    
    
    //@Test
    void computeExample_1() {
        CMRelationMetric metric = new CMRelationMetric();

        /*
        C-------sub-----> three
        ^
        |
        B <-----sub----- two
                          ^
                          |
        A -----sub------> one
        */
        /*
        File src = new File("src/test/resources/cmRelation/source.rdf");
        File tgt = new File("src/test/resources/cmRelation/target.rdf");
        TestCase tc = new TestCase("test", src.toURI(), tgt.toURI(), null, new LocalTrack("test", "1.0"));
        
        
        
        Alignment referenceAlignment = new Alignment();
        referenceAlignment.add("http://source.org/A", "http://target.org/one", CorrespondenceRelation.SUBSUMED);        
        referenceAlignment.add("http://source.org/B", "http://target.org/two", CorrespondenceRelation.SUBSUME);        
        referenceAlignment.add("http://source.org/C", "http://target.org/three", CorrespondenceRelation.SUBSUMED);
        

        Alignment systemAlignment = new Alignment();
        systemAlignment.add("http://www.example.com/entity_1", "http://www.loremIpsum.com/entity_a"); // tp
        systemAlignment.add("http://www.example.com/entity_2", "http://www.loremIpsum.com/entity_b"); // tp
        systemAlignment.add("http://www.example.com/entity_4", "http://www.loremIpsum.com/entity_c"); // fp
        
        ExecutionResult executionResult = new ExecutionResult(tc, "myTestMatcher", systemAlignment, referenceAlignment);
        
        
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
        
        */
    }
    
}
