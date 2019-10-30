package de.uni_mannheim.informatik.dws.ontmatching.validation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartialFileParsing {
    private static final Logger LOGGER = LoggerFactory.getLogger(PartialFileParsing.class);
    
    private static final String descText = "<rdf:Description ";
    private static final String descEndText = "</rdf:Description>";
    private static final String end = "</rdf:RDF>";
    private static final String newline = System.getProperty("line.separator");

    public static boolean analyzeAndParseFileInParts(File input, File errorParsing, SemanticWebLibrary webLibrary){
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"))){
            String start = "";
            String ln;
            while((ln = reader.readLine()) != null) {
                if(ln.contains(descText)){
                    break;
                }
                start+=ln + newline;                
            }
            String part = "";
            while(ln != null) {
                part += ln + newline;
                if(ln.contains(end)){
                    return true;
                } else if(ln.contains(descEndText)){
                    
                    String partialResource = start + part + end;
                    
                    OntologyValidationService parser = null;
                    if(webLibrary == SemanticWebLibrary.JENA){
                        parser = new JenaOntologyValidationService(partialResource);
                    }else{
                        parser = new OwlApiOntologyValidationService(partialResource);
                    }
                    
                    if(parser.isOntologyParseable() == false || parser.getNumberOfStatements() == 0){
                        LOGGER.error("Parsing error - please have a look at " + errorParsing.getAbsolutePath());
                        try(BufferedWriter writer = Files.newBufferedWriter(errorParsing.toPath(), StandardCharsets.UTF_8)){
                            writer.write(start + part + end);
                        }                
                        return false;
                    }
                    part = "";
                }
                ln = reader.readLine();
            }
        } catch (IOException ex) {
            LOGGER.error("Error when reading file.", ex);
            return false;
        }
        return true;
    }
    
    
    public static boolean analyzeAndParseFileCumulative(File input, File errorParsing, SemanticWebLibrary webLibrary){
        
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"))){
            String ln;
            //write beginning
            try(BufferedWriter writer = Files.newBufferedWriter(errorParsing.toPath(), StandardCharsets.UTF_8)){
                while((ln = reader.readLine()) != null) {
                    writer.write(ln + newline);
                    if(ln.contains(descText)){
                        writer.write(end + newline);
                        break;
                    }               
                }
            }
            
            String part = "";
            while((ln = reader.readLine()) != null) {
                part += ln + newline;
                if(ln.contains(descEndText)){
                    part += end + newline;
                    try(RandomAccessFile f = new RandomAccessFile(errorParsing, "rw")){                        
                        long length = f.length() - 1;
                        byte b = 0;
                        do {                     
                          length -= 1;
                          f.seek(length);
                          b = f.readByte();
                        } while(b != 10);
                        f.write(part.getBytes(StandardCharsets.UTF_8)); 
                    }
                    part = "";
                    
                    OntologyValidationService parser = null;
                    if(webLibrary == SemanticWebLibrary.JENA){
                        parser = new JenaOntologyValidationService(errorParsing);
                    }else{
                        parser = new OwlApiOntologyValidationService(errorParsing);
                    }
                    
                    if(parser.isOntologyParseable() == false || parser.getNumberOfStatements() == 0){
                        LOGGER.error("Parsing error - please have a look at the end of file " + errorParsing.getAbsolutePath());
                        return false;
                    }
                }               
            }
        } catch (IOException ex) {
            LOGGER.error("Error when reading file.", ex);
            return false;
        }
        return true;
            
    }
    
}
