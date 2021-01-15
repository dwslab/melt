package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;


/**
 * Providing basic input/output operations.
 */
public class IOoperations {


    private static Logger LOG = LoggerFactory.getLogger(IOoperations.class);


    /**
     * Prints overlapping parts of two string arrays..
     * @param s1 Array 1.
     * @param s2 Array 2.
     * @param <T> The type.
     */
    public static <T> void printOverlapOfSet (Iterable<T> s1, Iterable<T> s2){
        Set<T> set1 = new HashSet<>();
        Set<T> set2 = new HashSet<>();
        Set<T> result = new HashSet<>();

        s1.forEach(set1::add);
        s2.forEach(set2::add);

        set1.forEach(x -> {
            if(set2.contains(x)){
                result.add(x);
            }
        });
        result.stream().forEach(System.out::println);
    }


    /**
     * Prints the content of a HashMap
     * @param hashMapToPrint HashMap which shall be printed.
     * @param <K> Key Type.
     * @param <V> Value Type.
     */
    public static <K,V> void printHashMap(HashMap<K,V> hashMapToPrint){
        hashMapToPrint.forEach( (x,y) -> {
            System.out.println(x.toString() + "   " + y.toString());
        });
    }


    /**
     * Returns the elements of a HashSet in one line without a line break.
     * @param set Set to be converted.
     * @return Result String.
     */
    public static String convertHashSetToStringPipeSeparated(HashSet<String> set){
        String result = "";
        for(String s : set){
            result = result + s + " | ";
        }
        return result.substring(0, result.length() -3);
    }


    /**
     * Reads a tab separated file.
     * @param file File to read.
     * @return ArrayList with String[].
     */
    public static ArrayList<String[]> readTabSeparatedFile(File file){
        ArrayList<String[]> result = new ArrayList<>();

        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String readLine;

            while((readLine = reader.readLine()) != null){
                result.add(readLine.split("\t"));
            }

            reader.close();

        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }


    /**
     * Writes the given set to a file in {@code ./output/<filename>}.
     * @param set Set to write.
     * @param fileName Filename (not path).
     */
    public static void writeSetToFileInOutputDirectory(Set set, String fileName){
        File outputDirectory = new File("./output");
        if(!outputDirectory.exists()){
            outputDirectory.mkdir();
            LOG.debug("Directory 'output' created.");
        }
        if(!fileName.endsWith(".txt")){
            fileName = fileName + ".txt";
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("./output/" + fileName)));
            set.stream().forEach(x -> {
                try {
                    writer.write(x.toString() + "\n");
                } catch (IOException ioe){
                    ioe.printStackTrace();
                }
            });
            LOG.info("Writing file " + fileName);
            writer.flush();
            writer.close();
        } catch (IOException ioe){
            LOG.error("Problem writing file " + fileName);
            ioe.printStackTrace();
        }
    }

    /**
     * A very simple file writer.
     *
     * @param file    The file in which shall be written.
     * @param content The content that shall be written.
     */
    public static void writeContentToFile(File file, String content) {
        try {
            LOG.info("Writing File: " + file.getCanonicalPath());
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.flush();
            writer.close();
            LOG.info("File successfully written.");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    /**
     * A simple file writer which writes a file with the specified file name and the specified content into the
     * output directory.
     * @param fileNameWithinOutputDirectory Name of the file within the output directory.
     * @param content Output to write.
     */
    public static void writeContentToFile(String fileNameWithinOutputDirectory, String content) {
        File outputDirectory = new File("./output");
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
            LOG.debug("Directory 'output' created.");
        }
        if (!fileNameWithinOutputDirectory.contains(".")) {
            // implicit assumption: file has no file type → add .txt
            fileNameWithinOutputDirectory = fileNameWithinOutputDirectory + ".txt";
        }
        File fileToWrite = new File("./output/" + fileNameWithinOutputDirectory);
        if (fileToWrite.exists()) {
            // file exists → change name to <filename>_2.<fileending>
            String newFileNameWithoutType = fileNameWithinOutputDirectory.substring(0, fileNameWithinOutputDirectory.indexOf('.')) + "_2";
            String fileTypeEnding = fileNameWithinOutputDirectory.substring(fileNameWithinOutputDirectory.indexOf('.'), fileNameWithinOutputDirectory.length());
            String newFileName = "./output/" + newFileNameWithoutType + fileTypeEnding;
            LOG.info("File already exists. Saving file under new name: " + newFileName);
            fileToWrite = new File(newFileName);
            writeContentToFile(fileToWrite, content);
        } else {
            writeContentToFile(fileToWrite, content);
        }
    }
    
	/**
	 * Checks whether half of the word consists of numbers.
	 * @param word word to be checked
	 * @return true if numeric, else false
	 */
	public static boolean isNumeric(String word) {
		char[] charArray = word.toCharArray();
		int numericCount = 0;
		int alphabeticCount = 0;
		for (char c : charArray) {
			if(c >= 48 && c <= 57) {
				numericCount++;
			} else {
				alphabeticCount++;
			}
		}
		if(numericCount >= alphabeticCount) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 * Outputs the first view lines of a gzipped file to the console .
	 * @param filePath Path to the file.
	 * @param numberOfLines Number of lines to be printed.
	 */
	public static void printFirstLinesOfGzippedFile(String filePath, int numberOfLines) {
		try {
			GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(filePath));
			BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
			String readLine;
			long lineNumber = 0;
			while ((readLine = br.readLine()) != null) {
				lineNumber++;
				System.out.println(readLine);
				if(lineNumber > numberOfLines){
					break;
				}
			}
			br.close();
			System.out.println("done");
		} catch (Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * Outputs the first view lines of a file to the console. This can be useful when a very large file cannot be opened in
	 * a text editor.
	 * @param filePath Path to file.
	 * @param numberOfLines Number of lines to be printed.
	 */
	public static void printFirstLinesOfFile(String filePath, int numberOfLines) {
		System.out.println("START\n\n");
		File f = new File(filePath);
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String readLine;
			int linesRead = 0;
			while((readLine = br.readLine()) != null){
				System.out.println(readLine);
				linesRead++;
				if(linesRead == numberOfLines){
					break;
				}
			}
			br.close();
		} catch (FileNotFoundException fnfe){
			fnfe.printStackTrace();
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		System.out.println("DONE\n\n");
	}

}
