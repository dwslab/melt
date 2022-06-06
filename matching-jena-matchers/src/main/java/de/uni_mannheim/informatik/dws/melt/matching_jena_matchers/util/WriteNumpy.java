package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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
    
    
    
    public static void writeList(List<Double> doubles, File file){
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))){
            writeList(doubles, writer);
        } catch (IOException ex) {
            LOGGER.warn("Could not write array to disk.", ex);
        }
    }
    
    public static String writeList(List<Double> doubles){
        try(StringWriter writer = new StringWriter()){
            writeList(doubles, writer);
            return writer.toString();
        } catch (IOException ex) {
            LOGGER.warn("Could not generate numpy representation", ex);
            return "";
        }
    }
    
    
    private static void writeList(List<Double> doubles, Writer writer) throws IOException{
        for (double j : doubles) {
            writer.append(Double.toString(j));
            writer.write(NEWLINE);
        }
    }
    
    
    public static List<Double> readList(File file){
        try {
            return readList(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            LOGGER.warn("Could not find file to read doubles. Returning empty list.", ex);
            return new ArrayList<>();
        }
    }
    
    public static List<Double> readList(InputStream inputStream){
        List<Double> list = new ArrayList<>();        
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))){
            String line;
            while ((line = reader.readLine()) != null) {
               list.add(Double.parseDouble(line));
            }
            return list;
        } catch (IOException ex) {
            LOGGER.warn("Could not read numpy file. Returning empty list.", ex);
            return new ArrayList<>();
        }
    }
    
    public static double[] readArray(InputStream inputStream){
        return readList(inputStream).stream().mapToDouble(Double::doubleValue).toArray();
    }
}
