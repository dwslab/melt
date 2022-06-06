package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.WriteNumpy;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MeltCurvatureTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MeltCurvatureTest.class);

    @Test
    void testFilter() {
        //Alignment m = new Alignment();
        
        double[] confidenceValues = WriteNumpy.readArray(MeltCurvatureTest.class.getClassLoader().getResourceAsStream("confidences.txt"));
        
        Arrays.sort(confidenceValues);        
        
        assertEquals(0.5191471576690674, MeltCurvature.LONGEST_DISTANCE_TO_STRAIT_LINE_ELBOW.computeCurvature(confidenceValues), 0.01);
        assertEquals(0.5173575282096863, MeltCurvature.LONGEST_DISTANCE_TO_ADJUSTED_STRAIT_LINE_ELBOW.computeCurvature(confidenceValues), 0.01);
        
        assertEquals(0.5283073782920837, MeltCurvature.KNEEDLE_ELBOW.computeCurvature(confidenceValues), 0.01);
        assertEquals(0.29547786712646484, MeltCurvature.MENGER_ELBOW.computeCurvature(confidenceValues), 0.01);
    }
}
