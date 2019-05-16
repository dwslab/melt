
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;

import java.io.IOException;
import java.util.Random;

public class HugeMappingTest {
    
    private static Random randomObj = new Random(1990);
    
   
    private static Alignment generateMapping(int numberConceptsLeft, int numberConceptsRight, int numberOfMappings, boolean indexSource, boolean indexTarget, boolean indexRelation, boolean indexConfidence){
        Alignment m = new Alignment(indexSource, indexTarget, indexRelation, indexConfidence);
        for(int i = 0; i < numberOfMappings; i++){
            int left = randomObj.nextInt(numberConceptsLeft);
            int right = randomObj.nextInt(numberConceptsRight);            
            m.add("http://exampleLeftWithALongURI/" + left, "http://exampleRightWithALongURI/" + right);
        }     
        return m;
    }
    
    
    public static void main(String[] args) throws IOException {
        int numberOfConcepts = 800000;
        
        long startTime = System.nanoTime();
        Alignment test = generateMapping(numberOfConcepts,numberOfConcepts,numberOfConcepts*2, true, false, false, false);
        //Alignment m = generateMapping(numberOfConcepts,numberOfConcepts,numberOfConcepts*2);
        long endTime = System.nanoTime();
        System.out.println("TIME: " + (endTime - startTime) / 1000000 + " ms");
        System.out.println("Used memory: " + usedMemoryInMB() + "MB");
        
        //false, false, false:
        //TIME: 5307 ms
        //Used memory: 490MB
        
        //true, false, false:
        //TIME: 9038 ms
        //Used memory: 707MB
        
        //true, true, false:
        //TIME: 12975 ms
        //Used memory: 923MB
        
        //old:
        //TIME: 9898 ms
        //Used memory: 828MB
        
        
        //test.serialize(new File("test.txt"));
        /*
        for(Correspondence c : test.getCorrespondencesSource("http://exampleLeftWithALongURI/1255")){
            System.out.println(c);
        }
        
        for(Correspondence c : test.getCorrespondencesTarget("http://exampleRightWithALongURI/1255")){
            System.out.println(c);
        }
*/
        //startTime = System.nanoTime();
        //testRuntimeSource(test, 20, 500000);
        //endTime = System.nanoTime();

        //System.out.println("TIME: " + (endTime - startTime) / 1000000 + " ms");
        
        
        
        
        //String s = test.serialize();
        //try (FileOutputStream out = new FileOutputStream(new File("test.txt"))) {
        //    out.write(s.getBytes(StandardCharsets.UTF_8));
        //} 
        
    }
    
    private static void testRuntimeSource(Alignment test, int repetion, int maxValue){
        for(int i=0; i < repetion; i++){
            int searchValue = randomObj.nextInt(maxValue);
            //System.out.println(searchValue + "--> ");
            for(Correspondence c : test.getCorrespondencesSource("http://exampleLeftWithALongURI/" + searchValue)){
                //System.out.println(c);
                double conf = c.getConfidence();
            }
        }
    }
    
    private static final long MEGABYTE = 1024L * 1024L;
    private static long usedMemoryInMB(){
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        return memory/MEGABYTE;        
    }
}
