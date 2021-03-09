package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking.SameConfidenceRanking;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorRankGroupTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorRankGroupTest.class);

    private static Alignment system1 = createSystemAlignment1();
    private static Alignment createSystemAlignment1(){
        Alignment a = new Alignment();
        a.add("a", "a");
        a.add("a", "b");
        a.add("b", "b");
        a.add("b", "c");
        a.add("b", "d");
        return a;
    }

    private static Alignment reference1 = createReferenceAlignment1();
    private static Alignment createReferenceAlignment1(){
        Alignment a = new Alignment();
        a.add("a", "b");
        a.add("b", "c");
        a.add("b", "d");
        return a;
    }

    private static Alignment system2 = createSystemAlignment2();
    private static Alignment createSystemAlignment2(){
        Alignment system = new Alignment();
        system.add(new Correspondence("a", "b"));
        system.add(new Correspondence("c", "d"));
        system.add(new Correspondence("e", "f"));
        system.add(new Correspondence("g", "h"));
        system.add(new Correspondence("i", "j"));
        system.add(new Correspondence("k", "l"));
        system.add(new Correspondence("m", "n"));
        system.add(new Correspondence("o", "p"));
        system.add(new Correspondence("q", "r"));
        system.add(new Correspondence("s", "t"));
        return system;
    }

    private static Alignment reference2 = createReferenceAlignment2();
    private static Alignment createReferenceAlignment2(){
        Alignment reference = new Alignment();
        reference.add(new Correspondence("a", "b"));
        reference.add(new Correspondence("e", "f"));
        reference.add(new Correspondence("k", "l"));
        reference.add(new Correspondence("q", "r"));
        reference.add(new Correspondence("s", "t"));
        return reference;
    }


    @Test
    void writeResultsToDirectory(){
        try {
            Path tempDirWithPrefix = Files.createTempDirectory(null);
            File tempDirFile = tempDirWithPrefix.toFile();
            tempDirFile.deleteOnExit();
            ExecutionResultSet ers = new ExecutionResultSet();
            ExecutionResult er1 = new ExecutionResult(TrackRepository.Anatomy.Default.getFirstTestCase(), "TestMatcher1", system1, reference1);
            ExecutionResult er2 = new ExecutionResult(TrackRepository.Anatomy.Default.getFirstTestCase(), "TestMatcher2", system2, reference2);
            ers.add(er1);
            ers.add(er2);
            EvaluatorRankGroup evaluatorRankGroup = new EvaluatorRankGroup(ers, 2, SameConfidenceRanking.ALPHABETICALLY, SameConfidenceRanking.RANDOM);
            evaluatorRankGroup.writeResultsToDirectory(tempDirFile);

            // make sure some file was written
            assertEquals(1, tempDirFile.listFiles().length);
            BufferedReader reader = new BufferedReader(new FileReader(new File(tempDirFile, EvaluatorRankGroup.RESULT_FILE_NAME)));
            String line = "";
            int linesRead = 0;

            int indexF1atKalphabetically = 0;

            // check the file contents
            while((line = reader.readLine()) != null){
                linesRead++;
                if(linesRead == 1){
                    LOGGER.info(line);
                    String lineLowercased = line.toLowerCase();
                    assertTrue(lineLowercased.contains("track"));
                    assertTrue(lineLowercased.contains("test case"));
                    assertTrue(lineLowercased.contains("random"));
                    assertTrue(lineLowercased.contains("alphabetically"));
                    String[] tokenizedLine = line.split(",");
                    assertTrue(line.contains("ALPHABETICALLY - F1@K"));
                    indexF1atKalphabetically = ArrayUtils.indexOf(tokenizedLine, "ALPHABETICALLY - F1@K");
                } else {
                    LOGGER.info(line);
                    assertTrue(line.contains("TestMatcher1") || line.contains("TestMatcher2"));
                    String lineLowercased = line.toLowerCase();
                    assertFalse(lineLowercased.contains("random"));
                    assertFalse(lineLowercased.contains("alphabetically"));
                    assertFalse(lineLowercased.contains("test case"));
                    String[] tokenizedLine = line.split(",");
                    assertEquals("2", tokenizedLine[tokenizedLine.length-1]);
                    if(line.contains("TestMatcher1")){
                        assertEquals((2.0 * 0.75 * 0.5)/(0.5 + 0.75), Double.parseDouble(tokenizedLine[indexF1atKalphabetically]));
                    }
                }
            }
            assertEquals(3, linesRead);
        } catch (IOException e) {
            fail("An IOException occurred.", e);
        }
    }

}