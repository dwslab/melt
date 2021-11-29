package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.io.File;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class WriteNumpyTest {
    private static final String NEWLINE = System.getProperty("line.separator");
    @Test
    void testConvert() {
        double[][] array = new double[][]{
            { 0.1, 0.2, 0.3},
            { Double.MAX_VALUE, Double.MIN_NORMAL, Double.MIN_VALUE },
            { Double.NEGATIVE_INFINITY, Double.NaN, Double.POSITIVE_INFINITY },
            { 132456.123, 0.000001, 0.99999999}
        };
        
        
        String expected = "0.1 0.2 0.3 " + NEWLINE +
        "1.7976931348623157E308 2.2250738585072014E-308 4.9E-324 " + NEWLINE +
        "-Infinity NaN Infinity " + NEWLINE +
        "132456.123 1.0E-6 0.99999999 " + NEWLINE;
        String actual = WriteNumpy.writeArray(array);        
        assertEquals(expected, actual);
    }
    
}
