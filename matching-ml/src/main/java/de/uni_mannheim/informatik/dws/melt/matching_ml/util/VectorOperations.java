package de.uni_mannheim.informatik.dws.melt.matching_ml.util;

import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Some utility provided in the form of static methods for vector operations.
 */
public class VectorOperations {


    private static final Logger LOGGER = LoggerFactory.getLogger(VectorOperations.class);

    /**
     * Read the provided text file.
     * @param textModelFilePath Path to the text file. Each line represents a concept vector. All elements are space-separated.
     *                          The first element is the concept string. Afterwards, the vector elements are named. The file
     *                          must be UTF-8 encoded.
     * @return Parsed text model. Null in the case of an error.
     */
    public static Map<String, Double[]> readVectorFile(String textModelFilePath){
        return readVectorFile(new File(textModelFilePath));
    }

    /**
     * Read the provided text file.
     * @param textModelFilePath Path to the text file. Each line represents a concept vector. All elements are space-separated.
     *                          The first element is the concept string. Afterwards, the vector elements are named. The file
     *                          must be UTF-8 encoded.
     * @return Parsed text model. Null in the case of an error.
     */
    public static Map<String, Float[]> readVectorFileAsFloat(String textModelFilePath){
        return readVectorFileAsFloat(new File(textModelFilePath));
    }

    /**
     * Read the provided text file.
     * @param textModelFile The text file. Each line represents a concept vector. All elements are space-separated.
     *                      The first element is the concept string. Afterwards, the vector elements are named. The file
     *                      must be UTF-8 encoded.
     * @return Parsed text model. Null in the case of an error.
     */
    public static Map<String, Float[]> readVectorFileAsFloat(File textModelFile){
        if(!isFileOk(textModelFile)) return null;
        Map<String, Float[]> result = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textModelFile), StandardCharsets.UTF_8));
            String line;
            while((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");
                String key = tokens[0];
                if (result.containsKey(key)) {
                    LOGGER.warn("WARNING: The specified file (" + textModelFile + ") contains a duplicate key: " + key);
                    continue;
                }
                Float[] vector = new Float[tokens.length - 1];
                try {
                    for (int i = 1; i <= vector.length; i++) {
                        vector[i - 1] = Float.parseFloat(tokens[i]);
                    }
                } catch (NumberFormatException nfe){
                    LOGGER.warn("WARNING: There was a number format exception. Will not add vector for key " + key);
                    LOGGER.warn("The problem occurred while reading the following line: " + line);
                    continue;
                }
                result.put(key, vector);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Error while trying to read text model file: FileNotFoundException", e);
        } catch (IOException e) {
            LOGGER.error("Error while trying to read text model file.", e);
        }
        return result;
    }

    /**
     * Checks the provided file.
     * @param textModelFile File to be checked.
     * @return True if ok, else false.
     */
    private static boolean isFileOk(File textModelFile){
        if(textModelFile == null) {
            LOGGER.error("The specified file is null. Cannot read the vector file.");
            return false;
        }
        if(textModelFile.isDirectory()){
            LOGGER.error("The specified file is actually a directory. Cannot read the vector file.");
            return false;
        }
        if(!textModelFile.exists()){
            LOGGER.error("The specified file does not exist. Cannot read the vector file.");
            return false;
        }
        return true;
    }

    /**
     * Read the provided text file.
     * @param textModelFile The text file. Each line represents a concept vector. All elements are space-separated.
     *                      The first element is the concept string. Afterwards, the vector elements are named. The file
     *                      must be UTF-8 encoded.
     * @return Parsed text model. Null in the case of an error.
     */
    public static Map<String, Double[]> readVectorFile(File textModelFile){
        if(!isFileOk(textModelFile)) return null;


        Map<String, Double[]> result = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textModelFile), StandardCharsets.UTF_8));
            String line;
            while((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");
                String key = tokens[0];
                if (result.containsKey(key)) {
                    LOGGER.warn("WARNING: The specified file (" + textModelFile + ") contains a duplicate key: " + key);
                    continue;
                }
                Double[] vector = new Double[tokens.length - 1];
                try {
                    for (int i = 1; i <= vector.length; i++) {
                        vector[i - 1] = Double.parseDouble(tokens[i]);
                    }
                } catch (NumberFormatException nfe){
                    LOGGER.warn("WARNING: There was a number format exception. Will not add vector for key " + key);
                    LOGGER.warn("The problem occurred while reading the following line: " + line);
                    continue;
                }
                result.put(key, vector);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Error while trying to read text model file: FileNotFoundException", e);
        } catch (IOException e) {
            LOGGER.error("Error while trying to read text model file.", e);
        }
        return result;
    }

    /**
     * Parses the concepts (not the vectors) from a text model file, i.e. a UTF-8 file where the concept is followed by its vector.
     * One concept appears per line. Everything is space separated.
     * @param textModelFile The file the shall be parsed.
     * @return Triplet with: <br>
     *              [0] A set with the full vocabulary.<br>
     *              [1] Dimension<br>
     *              [2] True if dimension is consistent, else false.<br>
     */
    static Triplet<Set<String>, Integer, Boolean> analyzeVectorTextFile(File textModelFile){
        int dimension = -1;
        boolean isDimensionConsistent = true;
        HashSet<String> concepts = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textModelFile), StandardCharsets.UTF_8));
            String line;
            while((line = reader.readLine()) != null){
                String[] tokens = line.split(" ");
                if(tokens.length > 1) {
                    concepts.add(tokens[0]);
                    if(dimension == -1){
                        dimension = tokens.length - 1;
                    } else {
                        if(dimension != tokens.length - 1){
                            isDimensionConsistent = false;
                        }
                    }
                } else {
                    LOGGER.warn("Problem while reading the following line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Error while trying to read text model file: FileNotFoundException", e);
        } catch (IOException e) {
            LOGGER.error("Error while trying to read text model file.", e);
        } finally {
            return new Triplet<>(concepts, dimension, isDimensionConsistent);
        }
    }

}
