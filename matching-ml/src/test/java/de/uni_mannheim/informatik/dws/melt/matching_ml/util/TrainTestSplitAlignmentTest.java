package de.uni_mannheim.informatik.dws.melt.matching_ml.util;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TrainTestSplitAlignmentTest {
    
    @Test
    void testRandom() {
        Alignment collection = getCollection();
        TrainTestSplitAlignment split = new TrainTestSplitAlignment(collection, 0.8);        
        Alignment trainOne = split.getTrain();
        Alignment testOne = split.getTest();
        assertTrainTest(trainOne, testOne, 8, 2, collection);
    }
    
    @Test
    void testSeedRandom() {
        Alignment collection = getCollection();
        TrainTestSplitAlignment split = new TrainTestSplitAlignment(collection, 0.8, 1234);        
        Alignment trainOne = split.getTrain();
        Alignment testOne = split.getTest();
        
        assertTrainTest(trainOne, testOne, 8, 2, collection);
        
        split = new TrainTestSplitAlignment(collection, 0.8, 1234);        
        Alignment trainTwo = split.getTrain();
        Alignment testTwo = split.getTest();
        assertTrainTest(trainTwo, testTwo, 8, 2, collection);
        
        assertEquals(trainOne, trainTwo);
        assertEquals(testOne, testTwo);
        
        split = new TrainTestSplitAlignment(collection, 0.8, 4321);        
        Alignment trainThree = split.getTrain();
        Alignment testThree = split.getTest();
        assertTrainTest(trainThree, testThree, 8, 2, collection);
        
        assertNotEquals(trainOne, trainThree);
        assertNotEquals(testOne, testThree);
    }
    
    
    @Test
    void testStratifiedSplit() {        
        Alignment collection = getCollection();
        TrainTestSplitAlignment split = new TrainTestSplitAlignment(collection, 0.5, correspondence->correspondence.getConfidence());
        
        Alignment train = split.getTrain();
        Alignment test = split.getTest();
        
        assertTrainTest(train, test, 5, 5, collection);
        
        assertStratified(train, 2, 3, 5);
        assertStratified(test, 2, 3, 5);        
    }
    
    @Test
    void testStratifiedSplitSeed() {        
        Alignment collection = getCollection();
        TrainTestSplitAlignment split = new TrainTestSplitAlignment(collection, 0.5, 1324, correspondence->correspondence.getConfidence());
        Alignment trainOne = split.getTrain();
        Alignment testOne = split.getTest();
        
        split = new TrainTestSplitAlignment(collection, 0.5, 1324, correspondence->correspondence.getConfidence());
        Alignment trainTwo = split.getTrain();
        Alignment testTwo = split.getTest();
        
        assertEquals(trainOne, trainTwo);
        assertEquals(testOne, testTwo);
        
        
        split = new TrainTestSplitAlignment(collection, 0.5, 14321, correspondence->correspondence.getConfidence());
        Alignment trainThree = split.getTrain();
        Alignment testThree = split.getTest();
        
        assertNotEquals(trainOne, trainThree);
        assertNotEquals(testOne, testThree);
    }
    
    
    
    private void assertTrainTest(Alignment train, Alignment test, int sizeTrain, int sizeTest, Alignment all){
        assertEquals(sizeTrain, train.size());
        assertEquals(sizeTest, test.size());
        
        Alignment union = new Alignment(train);
        union.addAll(test);
        
        assertEquals(all, union);        
    }
    
    private void assertStratified(Alignment set, int expectedTextWithTwo, int expectedTextWithFour, int all){
        int countFour = 0;
        int countTwo = 0;
        for(Correspondence s : set){
            if(s.getConfidence() == 0.2){
                countTwo++;
            }else if(s.getConfidence() == 0.4){
                countFour++;
            }
        }
        assertEquals(all, set.size());
        assertEquals(expectedTextWithFour, countFour);
        assertEquals(expectedTextWithTwo, countTwo);
    }
    
    
    private Alignment getCollection(){
        Alignment collection = new Alignment();
        
        collection.add("A", "B", 0.4);
        collection.add("C", "D", 0.4);
        collection.add("E", "F", 0.4);
        collection.add("G", "H", 0.4);
        collection.add("I", "J", 0.4);
        collection.add("K", "L", 0.4);
        
        collection.add("M", "N", 0.2);
        collection.add("O", "P", 0.2);
        collection.add("Q", "R", 0.2);
        collection.add("S", "T", 0.2);
        return collection;
    }
}
