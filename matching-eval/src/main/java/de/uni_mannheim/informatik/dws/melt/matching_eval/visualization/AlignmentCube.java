
package de.uni_mannheim.informatik.dws.melt.matching_eval.visualization;
/*
import cubix.CubixJavaAPI;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlignmentCube {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlignmentCube.class);
    
    public static void runAlignmentCube(ExecutionResultSet results){        
        Iterator<ExecutionResult> i = results.iterator();
        if(i.hasNext() == false){
            LOGGER.warn("ExecutionResultSet has no results to visulize.");
        }
        TestCase testCase = i.next().getTestCase();
        LOGGER.info("Using arbitrary testcase {}", testCase);
        runAlignmentCube(results, testCase);
    }
    
    public static void runAlignmentCube(ExecutionResultSet results, TestCase testcase){
        Set<ExecutionResult> r = results.getGroup(testcase);
        List<File> alignmentFiles = getSystemResults(r);
        
        CubixJavaAPI.run(
                new File(testcase.getSource()),
                new File(testcase.getTarget()),
                alignmentFiles,
                null,
                true);
    }
    
    public static void runAlignmentCube(ExecutionResult result){
        CubixJavaAPI.run(
                new File(result.getTestCase().getSource()),
                new File(result.getTestCase().getTarget()),
                Arrays.asList(urlToFile(result.getOriginalSystemAlignment())),
                null,
                true);
    }
    
    
    
    private static List<File> getSystemResults(Set<ExecutionResult> results){
        List<File> resultFiles = new LinkedList();
        for(ExecutionResult result : results){
            if(result.getOriginalSystemAlignment()== null){
                LOGGER.warn("OriginalSystemAlignment in Execution Result is null. Probably due to a refined Executuion result. It is ignored and the programs continues.");
                continue;
            }
            resultFiles.add(urlToFile(result.getOriginalSystemAlignment()));
        }
        return resultFiles;
    }
    
    private static File urlToFile(URL url){
        try {
          return new File(url.toURI());          
        } catch(URISyntaxException e) {
          return new File(url.getPath());
        }
    }
}
*/