package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Write array like objects to file which can be read in python with numpy.
 */
public class WriteNumpy {
    private static final Logger LOGGER = LoggerFactory.getLogger(WriteNumpy.class);
    private static final String NEWLINE = System.getProperty("line.separator");
    
    public static void writeArray(double[][] array, File file){
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))){
            writeArray(array, writer);
        } catch (IOException ex) {
            LOGGER.warn("Could not write array to disk.", ex);
        }
    }
    
    public static String writeArray(double[][] array){
        try(StringWriter writer = new StringWriter()){
            writeArray(array, writer);
            return writer.toString();
        } catch (IOException ex) {
            LOGGER.warn("Could not generate numpy representation", ex);
            return "";
        }
    }
    
    private static void writeArray(double[][] array, Writer writer) throws IOException{
        for (double[] eachRow : array) {
            for (double j : eachRow) {
                writer.append(Double.toString(j)).append(" ");
            }
            writer.write(NEWLINE);
        }
    }
    
}
