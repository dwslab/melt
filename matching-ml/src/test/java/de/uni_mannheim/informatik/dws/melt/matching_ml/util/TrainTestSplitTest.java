package de.uni_mannheim.informatik.dws.melt.matching_ml.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TrainTestSplitTest {
    
    @Test
    void testRandomSplit() {
        
        Set<String> collection = getCollection();
        TrainTestSplit<String> split = new TrainTestSplit<>(collection, 0.8);
        
        Set<String> train = new HashSet<>(split.getTrain());
        Set<String> test = new HashSet<>(split.getTest());
        
        assertTrainTest(train, test, 8, 2, collection);
    }
    
    @Test
    void testStratifiedSplit() {        
        Set<String> collection = getCollection();
        TrainTestSplit<String> split = new TrainTestSplit<>(collection, 0.5, text->text.length());
        
        Set<String> train = new HashSet<>(split.getTrain());
        Set<String> test = new HashSet<>(split.getTest());
        
        assertTrainTest(train, test, 5, 5, collection);
        
        assertStratified(train, 2, 3, 5);
        assertStratified(test, 2, 3, 5);
    }
    
    @Test
    void testSeedRandom() {
        Set<String> collection = getCollection();
        TrainTestSplit<String> split = new TrainTestSplit<>(collection, 0.8, 1234);        
        Set<String> trainOne = new HashSet<>(split.getTrain());
        Set<String> testOne = new HashSet<>(split.getTest());
        
        split = new TrainTestSplit<>(collection, 0.8, 1234);        
        Set<String> trainTwo = new HashSet<>(split.getTrain());
        Set<String> testTwo = new HashSet<>(split.getTest());
        
        assertEquals(trainOne, trainTwo);
        assertEquals(testOne, testTwo);
        
        split = new TrainTestSplit<>(collection, 0.8, 4321);        
        Set<String> trainThree = new HashSet<>(split.getTrain());
        Set<String> testThree = new HashSet<>(split.getTest());
        
        assertNotEquals(trainOne, trainThree);
        assertNotEquals(testOne, testThree);
    }
    
    @Test
    void testSeedStratification() {
        Set<String> collection = getCollection();
        TrainTestSplit<String> split = new TrainTestSplit<>(collection, 0.8, 1234, text->text.length());        
        Set<String> trainOne = new HashSet<>(split.getTrain());
        Set<String> testOne = new HashSet<>(split.getTest());
        
        split = new TrainTestSplit<>(collection, 0.8, 1234, text->text.length());        
        Set<String> trainTwo = new HashSet<>(split.getTrain());
        Set<String> testTwo = new HashSet<>(split.getTest());
        
        assertEquals(trainOne, trainTwo);
        assertEquals(testOne, testTwo);
        
        split = new TrainTestSplit<>(collection, 0.8, 4321, text->text.length());        
        Set<String> trainThree = new HashSet<>(split.getTrain());
        Set<String> testThree = new HashSet<>(split.getTest());
        
        assertNotEquals(trainOne, trainThree);
        assertNotEquals(testOne, testThree);
    }
    
    @Test
    void testTooLessExamplesInOneClass() {
        Set<String> collection = new HashSet<>();        
        collection.add("aaaa");
        collection.add("bbbb");
        collection.add("aa");
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            TrainTestSplit<String> split = new TrainTestSplit<>(collection, 0.5, text->text.length()); 
        });
        assertTrue(exception.getMessage().contains("One class has to few examples"));
    }
    
    @Test
    void testTooLessExamples() {
        Set<String> collection = new HashSet<>();        
        collection.add("aaaa");
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            TrainTestSplit<String> split = new TrainTestSplit<>(collection, 0.5); 
        });
        assertTrue(exception.getMessage().contains("Cannot split the data into train and test"));
    }
    
    @Test
    void wrongTrainRatio() {
        Set<String> collection = getCollection(); 
        String exceptionText = "The variable train_ratio must be in range";
        List<Double> trainRatios = Arrays.asList(1.2, 1.0, 0.0, -0.5, 
                Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.MAX_VALUE);
        for(double trainRatio : trainRatios){
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                TrainTestSplit<String> split = new TrainTestSplit<>(collection, trainRatio); 
            });
            assertTrue(exception.getMessage().contains(exceptionText));
            
            exception = assertThrows(IllegalArgumentException.class, () -> {
                TrainTestSplit<String> split = new TrainTestSplit<>(collection, trainRatio, text->text.length()); 
            });
            assertTrue(exception.getMessage().contains(exceptionText));
        }
    }
    
    @Test
    void testEmptyTrainOrTest() {
        Set<String> collection = new HashSet<>();        
        collection.add("aaaa");
        collection.add("bbbb");
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            TrainTestSplit<String> split = new TrainTestSplit<>(collection, 0.00001); 
        });
        assertTrue(exception.getMessage().contains("The train set is empty"));
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
            TrainTestSplit<String> split = new TrainTestSplit<>(collection, 0.999999); 
        });
        assertTrue(exception.getMessage().contains("The test set is empty"));
        
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
            TrainTestSplit<String> split = new TrainTestSplit<>(collection, 0.00001, text->text.length()); 
        });
        assertTrue(exception.getMessage().contains("The train set is empty"));
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
            TrainTestSplit<String> split = new TrainTestSplit<>(collection, 0.999999, text->text.length()); 
        });
        assertTrue(exception.getMessage().contains("The test set is empty"));
    }
    
    @Test
    void testStratifyList() {
        List<String> collection = new ArrayList<>();
        collection.add("aaaa");
        collection.add("bbbb");
        collection.add("cccc");
        collection.add("dddd");
        collection.add("eeee");
        collection.add("ffff");
        
        collection.add("gg");
        collection.add("hh");
        collection.add("ii");
        collection.add("jj");
        
        List<Integer> stratify = new ArrayList<>();
        stratify.add(4);
        stratify.add(4);
        stratify.add(4);
        stratify.add(4);
        stratify.add(4);
        stratify.add(4);
        
        stratify.add(2);
        stratify.add(2);
        stratify.add(2);
        stratify.add(2);
        
        TrainTestSplit<String> split = new TrainTestSplit<>(collection, 0.8, stratify);
        
        Set<String> train = new HashSet<>(split.getTrain());
        Set<String> test = new HashSet<>(split.getTest());
        
        assertTrainTest(train, test, 8, 2, new HashSet<>(collection));
        
        //test seed
        split = new TrainTestSplit<>(collection, 0.8, 1234, stratify);
        
        train = new HashSet<>(split.getTrain());
        test = new HashSet<>(split.getTest());
        
        assertTrainTest(train, test, 8, 2, new HashSet<>(collection));
        
        
        //check different size
        stratify.add(2);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            TrainTestSplit<String> tmp = new TrainTestSplit<>(collection, 0.8, stratify); 
        });
        assertTrue(exception.getMessage().contains("The size of stratify collections is not the same"));
    }
    
    private void assertTrainTest(Set<String> train, Set<String> test, int sizeTrain, int sizeTest, Collection<String> all){
        assertEquals(sizeTrain, train.size());
        assertEquals(sizeTest, test.size());
        
        HashSet<String> union = new HashSet<>(train);
        union.addAll(test);
        
        assertEquals(all, union);        
    }
    
    private void assertStratified(Set<String> set, int expectedTextWithTwo, int expectedTextWithFour, int all){
        int countFour = 0;
        int countTwo = 0;
        for(String s : set){
            if(s.length() == 2){
                countTwo++;
            }else if(s.length() == 4){
                countFour++;
            }
        }
        assertEquals(all, set.size());
        assertEquals(expectedTextWithFour, countFour);
        assertEquals(expectedTextWithTwo, countTwo);
    }
    private Set<String> getCollection(){
        Set<String> collection = new HashSet<>();
        
        collection.add("aaaa");
        collection.add("bbbb");
        collection.add("cccc");
        collection.add("dddd");
        collection.add("eeee");
        collection.add("ffff");
        
        collection.add("gg");
        collection.add("hh");
        collection.add("ii");
        collection.add("jj");
        return collection;
    }
}
