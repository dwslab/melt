package de.uni_mannheim.informatik.dws.melt.matching_ml;

import de.uni_mannheim.informatik.dws.melt.matching_ml.python.MachineLearningScikitFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class MachineLearningScikitFilterTest {
    
    @Test
    void testEndToEnd() throws Exception {             
        Random rand = new Random(42);
        
        Alignment train = new Alignment();
        for(int i=0;i<100;i++){
            Correspondence c = new Correspondence("http://trainLeft/" +  Integer.toString(i),
                  "http://trainRight/" + Integer.toString(i));
            if(i < 50){
                c.setRelation(CorrespondenceRelation.INCOMPAT); //negative examples
                c.addAdditionalConfidence("feature_a", rand.nextDouble()); //important feature
                c.addAdditionalConfidence("feature_b", rand.nextDouble()); //uninformative feature
            }else{
                c.setRelation(CorrespondenceRelation.EQUIVALENCE); //positive examples
                c.addAdditionalConfidence("feature_a", 1 + rand.nextDouble()); //important feature
                c.addAdditionalConfidence("feature_b", rand.nextDouble()); //uninformative feature
            }
            train.add(c);
        }
        //should learn that positive is exactly when feature_a > 1
        
        List<Correspondence> positive = new ArrayList();
        List<Correspondence> negative = new ArrayList();
        Alignment test = new Alignment();
        for(int i=0;i<10;i++){
            Correspondence c = new Correspondence("http://testLeft/" +  Integer.toString(i),
                  "http://testRight/" + Integer.toString(i));
            if(i < 5){
                c.addAdditionalConfidence("feature_a", rand.nextDouble()); //important feature
                c.addAdditionalConfidence("feature_b", rand.nextDouble()); //uninformative feature
                negative.add(c);
            }else{
                c.addAdditionalConfidence("feature_a", 1 + rand.nextDouble()); //important feature
                c.addAdditionalConfidence("feature_b", rand.nextDouble()); //uninformative feature
                positive.add(c);
            }
            test.add(c);
        }
        
        //Test trainAndApplyMLModel
        
        
        Alignment filtered = MachineLearningScikitFilter.trainAndApplyMLModel(train, test, null, 2, 1);
        
        for(Correspondence c : positive){
            assertTrue(filtered.contains(c));
        }
        for(Correspondence c : negative){
            assertFalse(filtered.contains(c));
        }
        //Test trainAndStoreMLModel and applyStoredMLModel
        
        File modelFile = new File("modelFile.pickle");
        
        assertFalse(modelFile.exists());
        List<String> confidenceNames = MachineLearningScikitFilter.trainAndStoreMLModel(train, modelFile, null, 2, 1);
        assertTrue(modelFile.exists());
        
        filtered = MachineLearningScikitFilter.applyStoredMLModel(modelFile, test, confidenceNames);
        for(Correspondence c : positive){
            assertTrue(filtered.contains(c));
        }
        for(Correspondence c : negative){
            assertFalse(filtered.contains(c));
        }
        
        modelFile.delete();
        
    }
}
