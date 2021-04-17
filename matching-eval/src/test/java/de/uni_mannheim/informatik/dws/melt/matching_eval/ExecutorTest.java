package de.uni_mannheim.informatik.dws.melt.matching_eval;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.SimpleStringMatcher;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExecutorTest {


    @AfterAll
    static void tearDown(){
        deleteDirectory("melt_csv_loader_test");
    }

    static void deleteDirectory(String directoryName){
        try {
            FileUtils.deleteDirectory(directoryName);
        } catch (IOException e) {
            // we do not fail here...
        }
    }

    @Test
    void run(){
        SimpleStringMatcher matcher1 = new SimpleStringMatcher();
        SimpleStringMatcher matcher2 = new SimpleStringMatcher();

        Map<String, IOntologyMatchingToolBridge> matchers = new HashMap<>();
        matchers.put("M1", matcher1);
        matchers.put("M2", matcher2);

        List<TestCase> testCaseList = new ArrayList<>();
        testCaseList.add(TrackRepository.Conference.V1.getTestCase(0));
        testCaseList.add(TrackRepository.Conference.V1.getTestCase(1));
        testCaseList.add(TrackRepository.Conference.V1.getTestCase(2));

        ExecutionResultSet ers = Executor.run(testCaseList, matchers);
        assertTrue(ers.size() == 6);
    }

    @Test
    void loadFromMeltResultsFolder(){
        SimpleStringMatcher matcher1 = new SimpleStringMatcher();
        SimpleStringMatcher matcher2 = new SimpleStringMatcher();

        Map<String, IOntologyMatchingToolBridge> matchers = new HashMap<>();
        matchers.put("M1", matcher1);
        matchers.put("M2", matcher2);

        List<TestCase> testCaseList = new ArrayList<>();
        testCaseList.add(TrackRepository.Conference.V1.getTestCase(0));
        testCaseList.add(TrackRepository.Conference.V1.getTestCase(1));
        testCaseList.add(TrackRepository.Conference.V1.getTestCase(2));

        File resultsFolder = new File("melt_csv_loader_test");
        resultsFolder.deleteOnExit();
        ExecutionResultSet ers = Executor.run(testCaseList, matchers);
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(new ExecutionResultSet(ers));
        evaluatorCSV.writeResultsToDirectory(resultsFolder);
        assertTrue(resultsFolder.exists());

        ExecutionResultSet ersLoaded = Executor.loadFromEvaluatorCsvResultsFolder(resultsFolder);
        Set<String> matcherNames = new HashSet<>();

        Iterator<String> iterator = ersLoaded.getDistinctMatchers().iterator();
        while(iterator.hasNext()){
            matcherNames.add(iterator.next());
        }
        assertTrue(matcherNames.contains("M1"));
        assertTrue(matcherNames.contains("M2"));
        assertTrue(ersLoaded.size() >= 6);
    }

}
