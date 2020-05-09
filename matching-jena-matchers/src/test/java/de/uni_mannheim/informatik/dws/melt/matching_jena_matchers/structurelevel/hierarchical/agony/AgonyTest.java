package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel.hierarchical.agony;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Testing the Agony class.
 */
public class AgonyTest {
   
    private static final Logger LOGGER = LoggerFactory.getLogger(AgonyTest.class);
    
    @Test
    void testWikiVote() {
        Map<String, Integer> wikivoteResult = readResult(getResourceFile("Wiki-Vote_result.txt"));
        Map<String, Integer> wikiVoteAgony = new Agony(Agony.readEdges(getResourceFile("Wiki-Vote.txt"))).computeAgony();
        assertEquals(wikivoteResult, wikiVoteAgony);
    }
    
    //Util methods:
    
    private File getResourceFile(String resource){
        try {
            return Paths.get(getClass().getClassLoader().getResource(resource).toURI()).toFile();
        } catch (URISyntaxException ex) {return null;}
    }
    
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[ \t]");
    private static Map<String, Integer> readResult(File file){
        Map<String, Integer> result = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line = reader.readLine();
            int lineNumber = 1;
            while(line != null){
                String[] parts = SPLIT_PATTERN.split(line);
                if(parts.length < 2){
                    LOGGER.warn("Found line {} which splitted by whitespace has no engough parts", lineNumber);
                    line = reader.readLine();
                    lineNumber++;
                    continue;
                }
                result.put(parts[0], Integer.parseInt(parts[1]));                
                line = reader.readLine();
                lineNumber++;
            }
        } catch(IOException e) {
            LOGGER.warn("Could not read file", e);
        }
        return result;
    }
}
