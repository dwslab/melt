package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.addnegatives;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class RandomSampleSetTest {
    @Test
    void testRandomSampleSet(){
        Set<Integer> s = new HashSet<>();
        for(int i = 0; i < 100_000; i++){
            s.add(i);
        }
        RandomSampleSet<Integer> randSet = new RandomSampleSet<>(s);
        
        Set<Integer> exclude = new HashSet<>();
        for(int i = 0; i < 100_000; i++){
            if(i==55100)
                continue;
            exclude.add(i);
        }
        
        assertEquals(55100, randSet.getRandomElement(exclude));
        
        List<Integer> l = randSet.getRandomElementsWithRepetition(100_000);
        assertEquals(100_000, l.size());
        
        l = randSet.getRandomElementsWithRepetition(100_001);
        assertEquals(100_001, l.size());
        
        assertThrows(NoSuchElementException.class, ()-> randSet.getRandomElementsWithoutRepetition(100_001));
        
        l = randSet.getRandomElementsWithoutRepetition(1, exclude);
        assertEquals(1, l.size());
        assertThrows(NoSuchElementException.class, ()-> randSet.getRandomElementsWithoutRepetition(2, exclude));
        
        l = randSet.getRandomElementsWithRepetition(20_000, exclude);
        assertEquals(20_000, l.size());
        for(Integer i : l){
            assertEquals(55100, i);
        }
        
    }
    
    
    /*
    public static void main(String[] args){
        //small runtime test
        int elementsOverall = 500_000;
        Set<Integer> s = new HashSet<>();
        for(int i = 0; i < elementsOverall; i++){
            s.add(i);
        }
        
        Set<Integer> initExclude = new HashSet<>();
        Random r = new Random();
        for(int j = 0; j < 499_000; j++){            
            initExclude.add(j);//r.nextInt(elementsOverall));
        }
        
        RandomSampleSet<Integer> randSet = new RandomSampleSet<>(s);

        System.out.println("start measure");
        long start;
        long diffOne;
        long diffTwo = 0;
        long diffThree;
        long diffFour;
        long diffFive;
        System.out.println("OverallSize,Selected,TimeOne,TimeTwo");
        for(int i = 400_000; i < elementsOverall; i+=1000){
            
            Set<Integer> exclude = new HashSet<>();
            //Random r = new Random();
            for(int j = 0; j < i; j++){            
                exclude.add(j);//r.nextInt(elementsOverall));
            }
            
            
            
            start = System.nanoTime();
            randSet.A(5000, exclude);
            diffOne = System.nanoTime() - start;

            start = System.nanoTime();
            randSet.B(5000, exclude);
            diffTwo = System.nanoTime() - start;
            
            start = System.nanoTime();
            randSet.C(5000, exclude);
            diffThree = System.nanoTime() - start;
            
            start = System.nanoTime();
            randSet.D(5000, exclude);
            diffFour = System.nanoTime() - start;
            
            start = System.nanoTime();
            randSet.getRandomElementsWithoutRepetitionFinal(5000, exclude);
            diffFive = System.nanoTime() - start;
            
            
            //System.out.println(((double)exclude.size() / elementsOverall) + "," + diffOne/1_000_000 + "," + diffTwo/1_000_000 + "," + diffThree/1_000_000);
                        
            System.out.println(exclude.size() + "," + diffOne/1_000_000 + "," + diffTwo/1_000_000 + "," + diffThree/1_000_000 + "," + diffFour/1_000_000 + "," + diffFive/1_000_000);
        }
    }
    */
}
