package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Test for multiple extractors
 */
public class TestExtractors {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestExtractors.class);
    
    /**
     * Found in Ontology Matching (Book) - Euzenat, Jérôme, Shvaiko, Pavel - section 7.7 Alignment extraction page 191
     */
    private Correspondence a1 = new Correspondence("Product", "Book", 0.84);
    private Correspondence a2 = new Correspondence("Product", "Publisher", 0.9);
    private Correspondence a3 = new Correspondence("Product", "Writer", 0.12);
    private Correspondence a4 = new Correspondence("Provider", "Book", 0.12);
    private Correspondence a5 = new Correspondence("Provider", "Publisher", 0.84);
    private Correspondence a6 = new Correspondence("Provider", "Writer", 0.6);
    private Correspondence a7 = new Correspondence("Creator", "Book", 0.6);
    private Correspondence a8 = new Correspondence("Creator", "Translator", 0.5);
    private Correspondence a9 = new Correspondence("Creator", "Publisher", 0.12);
    private Correspondence a10 = new Correspondence("Creator", "Writer", 0.84);
    private Alignment caseA = new Alignment(Arrays.asList(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10));
    
    /**
     * All same confidence (just to check)
     */    
    private Correspondence b1 = new Correspondence("A", "X", 1.0);
    private Correspondence b2 = new Correspondence("A", "Y", 1.0);
    private Correspondence b3 = new Correspondence("B", "X", 1.0);
    private Correspondence b4 = new Correspondence("B", "Y", 1.0);    
    
    private Alignment caseB = new Alignment(Arrays.asList(b1, b2, b3, b4));
    
    /**
     * Found in Analyzing Mapping Extraction Approaches - C. Meilicke, H. Stuckenschmidt - Ontology Matchign Workshop 2007
     */    
    private Correspondence c1 = new Correspondence("A", "X", 0.9);
    private Correspondence c2 = new Correspondence("A", "Y", 0.8);
    private Correspondence c3 = new Correspondence("B", "X", 0.7);
    private Correspondence c4 = new Correspondence("B", "Y", 0.5);
    private Alignment caseC = new Alignment(Arrays.asList(c1, c2, c3, c4));
    
    
    @Test
    void testNativeDescending() throws Exception {
        Alignment aFiltered = NaiveDescendingExtractor.filter(new Alignment(caseA)); //make a copy because it is filtered on input alignment
        assertEquals(3, aFiltered.size());
        assertTrue(aFiltered.containsAll(Arrays.asList(a2, a10, a4)));
        
        Alignment cFiltered = NaiveDescendingExtractor.filter(new Alignment(caseC)); 
        assertEquals(2, cFiltered.size());
        assertTrue(cFiltered.containsAll(Arrays.asList(c1, c4)));
    }
    
    @Test
    void testNativeAscending() throws Exception {
        Alignment aFiltered = NaiveAscendingExtractor.filter(new Alignment(caseA));
        assertEquals(2, aFiltered.size());
        assertTrue(aFiltered.containsAll(Arrays.asList(a2, a10)));
        
        Alignment cFiltered = NaiveAscendingExtractor.filter(new Alignment(caseC)); 
        assertEquals(1, cFiltered.size());
        assertTrue(cFiltered.containsAll(Arrays.asList(c1)));
    }
    
    
    @Test
    void testHungarianExtractor() throws Exception {
        Alignment aFiltered = HungarianExtractor.filter(new Alignment(caseA));
        assertEquals(3, aFiltered.size());
        assertTrue(aFiltered.containsAll(Arrays.asList(a1, a5, a10)));
        
        Alignment bFiltered = HungarianExtractor.filter(new Alignment(caseB));
        assertEquals(2, bFiltered.size());//just check than it contains only two correspondences
        
        Alignment cFiltered = HungarianExtractor.filter(new Alignment(caseC));
        assertEquals(2, cFiltered.size());
        assertTrue(cFiltered.containsAll(Arrays.asList(c2, c3)));
    }
    
    
    @Test
    void testMaxWeightBipartiteExtractor() throws Exception {
        //same checks as in hungarian
        for(MwbInitHeuristic init : Arrays.asList(MwbInitHeuristic.NAIVE, MwbInitHeuristic.SIMPLE)){
            Alignment aFiltered = MaxWeightBipartiteExtractor.filter(new Alignment(caseA), init);
            assertEquals(3, aFiltered.size());
            assertTrue(aFiltered.containsAll(Arrays.asList(a1, a5, a10)));

            Alignment bFiltered = MaxWeightBipartiteExtractor.filter(new Alignment(caseB), init);
            assertEquals(2, bFiltered.size());//just check than it contains only two correspondences

            Alignment cFiltered = MaxWeightBipartiteExtractor.filter(new Alignment(caseC), init);
            assertEquals(2, cFiltered.size());
            assertTrue(cFiltered.containsAll(Arrays.asList(c2, c3)));
        }
    }
    
    @Test
    void testRandomAlignment() throws SAXException, IOException{
        Alignment caseD = new Alignment(TestExtractors.class.getClassLoader().getResourceAsStream("randomAlignmentForTestingExtractors.xml"));
        Alignment hungarian = HungarianExtractor.filter(new Alignment(caseD));         
        for(MwbInitHeuristic init : Arrays.asList(MwbInitHeuristic.NAIVE, MwbInitHeuristic.SIMPLE)){
            Alignment mwbge = MaxWeightBipartiteExtractor.filter(new Alignment(caseD), init);
            assertEquals(hungarian, mwbge);
        }
        
                
        //following alignment generated with
        //Alignment random = randomAlignment(10000,20,1000);
        //random.serialize(new File("targetSmallerThanSource.xml"));
        Alignment caseE = new Alignment(TestExtractors.class.getClassLoader().getResourceAsStream("targetSmallerThanSource.xml"));
        hungarian = HungarianExtractor.filter(new Alignment(caseE));
        for(MwbInitHeuristic init : Arrays.asList(MwbInitHeuristic.NAIVE, MwbInitHeuristic.SIMPLE)){
            Alignment mwbge = MaxWeightBipartiteExtractor.filter(new Alignment(caseE), init);
            assertEquals(hungarian, mwbge);
        }        
        
        // following NOT suitable for general test, but good to see if it works...
        /*
        for(int i=0; i<10; i++){
            Alignment random = randomAlignment(500,500,1000);
            Alignment hungarianRandom = HungarianExtractor.filter(new Alignment(random)); 
            for(MwbInitHeuristic init : Arrays.asList(MwbInitHeuristic.NAIVE, MwbInitHeuristic.SIMPLE)){
                Alignment mwbgeRandom = MaxWeightBipartiteExtractor.filter(new Alignment(random), init);
                assertEquals(hungarianRandom, mwbgeRandom);
            }
        }
        */
    }
    
    
    private static Alignment randomAlignment(int conceptsSource, int conceptsTargets, int correspondences){
        Random r = new Random();
        Alignment a = new Alignment();
        for(int i=0; i< correspondences; i++){
            a.add("http://source.com/resource/" + r.nextInt(conceptsSource), "http://target.com/resource/" + r.nextInt(conceptsTargets), r.nextDouble());
        }
        return a;
    }
    
    
//    test runtime of hungarian:
//    public static void main(String[] args) throws IOException, SAXException{
//        System.out.println("size;runtime");
//        for(int i = 1; i < 5000; i+=100){
//            
//            Alignment random = randomAlignment(i,10000,1000);
//            long startTime = System.currentTimeMillis();
//            Alignment aFiltered = HungarianExtractor.filter(random);
//            double seconds = (System.currentTimeMillis()-startTime)/(double)1000;
//            System.out.println(i + ";" + Double.toString(seconds).replace(".", ","));
//        }
//     }
    
//    test runtime and correctness of all extractors  
//    public static void main(String[] args) throws IOException, SAXException {
//        System.out.println("size;runtimeHungarian;runtimeBipartiteNaive;runtimeBipartiteSimple;HungarianBipartiteNaiveSame;HungarianBipartiteSimpleSame");
//        for (int i = 100; i < 5000; i += 100) {
//            Alignment random = randomAlignment(i, 10000, 1000);
//            long startTime = 0;
//
//            startTime = System.currentTimeMillis();
//            Alignment filteredHungarian = HungarianExtractor.filter(random);
//            double secondsFilteredHungarian = (System.currentTimeMillis() - startTime) / (double) 1000;
//
//            startTime = System.currentTimeMillis();
//            Alignment filteredBipartiteNaive = MaxWeightBipartiteExtractor.filter(random, MwbInitHeuristic.NAIVE);
//            double secondsFilteredBipartiteNaive = (System.currentTimeMillis() - startTime) / (double) 1000;
//
//            startTime = System.currentTimeMillis();
//            Alignment filteredBipartiteSimple = MaxWeightBipartiteExtractor.filter(random, MwbInitHeuristic.SIMPLE);
//            double secondsFilteredBipartiteSimple = (System.currentTimeMillis() - startTime) / (double) 1000;
//
//            System.out.println(i + ";"
//                    + Double.toString(secondsFilteredHungarian).replace(".", ",") + ";"
//                    + Double.toString(secondsFilteredBipartiteNaive).replace(".", ",") + ";"
//                    + Double.toString(secondsFilteredBipartiteSimple).replace(".", ",") + ";"
//                    + filteredHungarian.equals(filteredBipartiteNaive) + ";"
//                    + filteredHungarian.equals(filteredBipartiteSimple));
//        }
//    }
}
