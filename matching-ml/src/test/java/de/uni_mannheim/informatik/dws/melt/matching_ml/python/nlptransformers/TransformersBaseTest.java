package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TransformersBaseTest {
    
    //@Test
    public void testGetExamplesForBatchSizeOptimization() throws Exception {
        testGetExamplesForBatchSizeOptimizationWithList(Arrays.asList(10,20,30,40,50,60,70,80,90,100), 5);
        testGetExamplesForBatchSizeOptimizationWithList(Arrays.asList(100,90,80,70,60,50,40,30,20,10), 5);
        testGetExamplesForBatchSizeOptimizationWithList(Arrays.asList(100,10,90,20,80,30,70,40,60,50), 5);
        
        testGetExamplesForBatchSizeOptimizationWithList(Arrays.asList(10,10,10,10,10,100,100,100,100,100), 5);
        
        Random rnd = new Random(1234);
        testGetExamplesForBatchSizeOptimizationWithList(rnd.ints(50, 0, 100).boxed().collect(Collectors.toList()), 10);
        testGetExamplesForBatchSizeOptimizationWithList(rnd.ints(50, 0, 100).boxed().collect(Collectors.toList()), 10);
    }
    
    //@Test
    public void testGetExamplesForBatchSizeOptimizationWithNewline() throws Exception {
        File file = File.createTempFile("unitTestGetExamplesNewline", ".txt");
        try{
            String one = StringEscapeUtils.escapeCsv("aa\naa") + "," + StringEscapeUtils.escapeCsv("aa\naa");
            String two = StringEscapeUtils.escapeCsv("aaaa\naaaa") + "," + StringEscapeUtils.escapeCsv("aaaa\naaaa");
            String three = StringEscapeUtils.escapeCsv("aaaaaaaa\naaaaaaaa") + "," + StringEscapeUtils.escapeCsv("aaaaaaaa\naaaaaaaa");
            String four = StringEscapeUtils.escapeCsv("aaaaaaaaaa\naaaaaaaaaa") + "," + StringEscapeUtils.escapeCsv("aaaaaaaaaa\naaaaaaaaaa");
            String five = StringEscapeUtils.escapeCsv("aaaaaa\naaaaaa") + "," + StringEscapeUtils.escapeCsv("aaaaaa\naaaaaa");
            try(PrintWriter pw = new PrintWriter(new FileWriter(file))){
                pw.println(one);
                pw.println(two);                
                pw.println(three);                
                pw.println(four);
                pw.println(five);
            }
            TransformersBase b = new TransformersBase((TextExtractorMap)null, null) {
                @Override
                public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                    return null;
                }
            };

            List<String> actual = b.getExamplesForBatchSizeOptimization(file, 2, BatchSizeOptimization.USE_LONGEST_TEXTS);
            assertEquals(2, actual.size());
            assertTrue(actual.contains(three));
            assertTrue(actual.contains(four));
        }finally{
            file.delete();
        }
    }
    
    
    @Test
    public void testGetExamplesForBatchSizeWithDifferentOptimization() throws Exception {
        File file = File.createTempFile("unitTestGetExamplesNewline", ".txt");
        try{
            String one = StringEscapeUtils.escapeCsv("Veeeeeeeeeery loooooong teeeeeext") + "," + StringEscapeUtils.escapeCsv("Veeeeeeeeeery loooooong teeeeeext");
            String two = StringEscapeUtils.escapeCsv("He is 31 years old many tokens.") + "," + StringEscapeUtils.escapeCsv("He is 31 years old many tokens.");
            String three = StringEscapeUtils.escapeCsv("Loooooonnnnnnnngggg texxxxxxxxxxxtttttt") + "," + StringEscapeUtils.escapeCsv("Loooooonnnnnnnngggg texxxxxxxxxxxtttttt");
            String four = StringEscapeUtils.escapeCsv("Again many tokens in here") + "," + StringEscapeUtils.escapeCsv("Again many tokens in here");
            try(PrintWriter pw = new PrintWriter(new FileWriter(file))){
                pw.println(one);
                pw.println(two);                
                pw.println(three);                
                pw.println(four);
            }
            TransformersBase b = new TransformersBase((TextExtractorMap)null, null) {
                @Override
                public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                    return null;
                }
            };

            List<String> actual = b.getExamplesForBatchSizeOptimization(file, 2, BatchSizeOptimization.USE_LONGEST_TEXTS);
            assertEquals(2, actual.size());
            assertTrue(actual.contains(one));
            assertTrue(actual.contains(three));
            
            actual = b.getExamplesForBatchSizeOptimization(file, 2, BatchSizeOptimization.USE_MAX_WORDS);
            assertEquals(2, actual.size());
            assertTrue(actual.contains(two));
            assertTrue(actual.contains(four));
        }finally{
            file.delete();
        }
        
    }
    
    
    private void testGetExamplesForBatchSizeOptimizationWithList(List<Integer> list, int max) throws IOException{
        File file = File.createTempFile("unitTestGetExamples", ".txt");
        try{
            try(PrintWriter pw = new PrintWriter(new FileWriter(file))){
                writeLength(pw, list);
            }
            TransformersBase b = new TransformersBase((TextExtractorMap)null, null) {
                @Override
                public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                    return null;
                }
            };

            List<String> actual = b.getExamplesForBatchSizeOptimization(file, max, BatchSizeOptimization.USE_LONGEST_TEXTS);

            Collections.sort(list, Collections.reverseOrder());        
            List<Integer> expected = list.subList(0, max);

            assertTrue(SameStringLength(expected, actual));
            //assertEquals()
            //60,70,80,90,100
        }finally{
            file.delete();
        }
    }
    
    
    public boolean SameStringLength(List<Integer> expected, List<String> actual) {
        List<Integer> actualLength = actual.stream().map(String::length).collect(Collectors.toList());
        return expected.equals(actualLength);
    }
    
    private static void writeLength(PrintWriter pw, List<Integer> lengths){
        for(int length : lengths){
            int k = (length - 3 ) / 2;
            StringBuilder sbString = new StringBuilder();
            for(int i=0; i < k; i++){
                sbString.append("a");
            }
            sbString.append(",");
            for(int i=0; i < k; i++){
                sbString.append("a");
            }
            int diff = length - sbString.length();
            diff -= 2;
            if(diff > 0){
                for(int i=0; i < diff; i++){
                    sbString.append("a");
                }
            }
            sbString.append(",1");
            pw.println(sbString.toString());
        }
    }
}
