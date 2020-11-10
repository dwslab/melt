package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking.SameConfidenceRanking;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
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

class EvaluatorRankTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorRankTest.class);

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
            EvaluatorRank evaluatorRank = new EvaluatorRank(ers, SameConfidenceRanking.ALPHABETICALLY, SameConfidenceRanking.RANDOM);
            evaluatorRank.writeResultsToDirectory(tempDirFile);

            // make sure some file was written
            assertEquals(1, tempDirFile.listFiles().length);
            BufferedReader reader = new BufferedReader(new FileReader(new File(tempDirFile, EvaluatorRank.RESULT_FILE_NAME)));
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
                    assertTrue(line.contains("ALPHABETICALLY - F1@K"));
                    String[] tokenizedLine = line.split(",");
                    indexF1atKalphabetically = ArrayUtils.indexOf(tokenizedLine, "ALPHABETICALLY - F1@K");
                } else {
                    LOGGER.info(line);
                    assertTrue(line.contains("TestMatcher1") || line.contains("TestMatcher2"));
                    line = line.toLowerCase();
                    assertFalse(line.contains("random"));
                    assertFalse(line.contains("alphabetically"));
                    assertFalse(line.contains("test case"));
                    String[] tokenizedLine = line.split(",");
                    if(line.contains("TestMatcher2")){
                        assertEquals((2.0*(4.0 / 25.0))/(2*(2.0 / 5.0)), Double.parseDouble(tokenizedLine[indexF1atKalphabetically]));
                    }
                }
            }
            assertEquals(3, linesRead);
        } catch (IOException e) {
            fail("An IOException occurred.", e);
        }
    }

}