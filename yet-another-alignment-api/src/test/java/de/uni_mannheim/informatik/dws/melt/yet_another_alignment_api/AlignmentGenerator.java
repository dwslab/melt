package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.util.Random;

/**
 * Test util class to generate different types of alignment.
 */
public class AlignmentGenerator {
    
    private static Random randomObj = new Random(1990);
    
    public static Alignment generateRandomAlignment(int numberOfCorrespondences, boolean indexSource, boolean indexTarget, boolean indexRelation, boolean indexConfidence){
        Alignment m = new Alignment(indexSource, indexTarget,indexRelation, indexConfidence);
        for(int i = 0; i < numberOfCorrespondences; i++){
            int left = randomObj.nextInt(Integer.MAX_VALUE);
            int right = randomObj.nextInt(Integer.MAX_VALUE);
            m.add("http://exampleLeftWithALongURI/" + left, "http://exampleRightWithALongURI/" + right, randomObj.nextDouble());
        }     
        return m;
    }
    public static Alignment generateRandomAlignment(int numberOfCorrespondences){
        return generateRandomAlignment(numberOfCorrespondences, true, true, true, true);
    }
}
