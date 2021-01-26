package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;

/**
 * This class reads lines of documents from a directory and saves them in a HashSet.
 * All files have to be saved in one directory as .txt file.
 * The file format is just a list with a line break after every resource name.
 */
public class TermFromFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TermFromFileReader.class);
    private HashSet<String> readLines =  new HashSet<String>();
    private File source; // file or directory


    /**
     * Constructor
     * @param source txt file or directory with txt files.
     */
    public TermFromFileReader(File source){
        this.source = source;

        if(source.isDirectory()) {
            for (File f : source.listFiles()) {
                if (f.getName().endsWith(".txt")) {
                    readResources(f);
                }
            }
        } else if(source.isFile() && source.getName().endsWith(".txt")){
            readResources(source);
        } else {
            System.out.println("directory path: " + source);
            System.out.println("ERROR: No directory.");
        }
    }

    /**
     * Constructor
     * @param pathToDirectory Path to source or txt file.
     */
    public TermFromFileReader(String pathToDirectory){
        this(new File(pathToDirectory));
    }


    /**
     * Constructor
     * @param inputStream Input stream to the file.
     */
    public TermFromFileReader(InputStream inputStream){
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String readLine = "";
            while((readLine = bufferedReader.readLine()) != null){
                if(!readLine.equals("")) {
                    readLines.add(readLine.trim());
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found exception. Could not read terms from file.", e);
        } catch (IOException ioe){
            LOGGER.error("IO exception. Could not read terms from file.", ioe);
        }
    }


    /**
     * This method can be called to read additional files not in the files directory.
     * @param fileToReadFrom The file from which shall be read.
     */
    public void readResources(File fileToReadFrom){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileToReadFrom));
            String readLine = "";
            while((readLine = bufferedReader.readLine()) != null){
                if(!readLine.equals("")) {
                    readLines.add(readLine.trim());
                }
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public void addLine(String stringToAdd){
        readLines.add(stringToAdd);
    }

    public HashSet<String> getReadLines() {
        return readLines;
    }

    public File getSource() {
        return source;
    }
}
