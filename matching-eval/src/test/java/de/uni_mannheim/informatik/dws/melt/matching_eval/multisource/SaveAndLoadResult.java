package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveAndLoadResult {
    private static Logger LOGGER = LoggerFactory.getLogger(SaveAndLoadResult.class);

    @Test
    void testConferenceIDExtractor() throws MalformedURLException {
        Alignment alignment = new Alignment();
        alignment.add("http://one.com/a", "http://two.com/a");
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("http://myid.com/parameterOne", "b");
        alignment.add("http://one.com/b", "http://two.com/b", 0.9, CorrespondenceRelation.EQUIVALENCE, extensions);
        
        Properties parameter = new Properties();
        parameter.put("hello", 15);
        parameter.put("foo", "bar");
        //parameter.put("x", 1.5); compare does not work because doubel is BigDecimal
        
        List<URL> urls = new ArrayList<>();
        urls.add(new File("./").toURI().toURL());
        
        List<TestCase> testCases = new ArrayList<>();
        testCases.add(TrackRepository.Conference.V1.getFirstTestCase());
        
        long runtime = 123;
        Partitioner partitioner = ExecutorMultiSource.getMostSpecificPartitioner(TrackRepository.Conference.V1);
        ExecutionResultMultiSource s = new ExecutionResultMultiSource(alignment, parameter, null, 
                "My matchername", urls, testCases, runtime, true, partitioner);
        
        ExecutionResultSetMultiSource results = new ExecutionResultSetMultiSource();
        results.add(s);
        
        EvaluatorMultiSourceBasic b = new EvaluatorMultiSourceBasic(results);
        File baseDirectory = new File("./testBaseDirectory");
        baseDirectory.mkdir();
        b.writeResultsToDirectory(baseDirectory);
        
        
        ExecutionResultSetMultiSource loadedResults = EvaluatorMultiSourceBasic.load(baseDirectory);
        assertEquals(1, loadedResults.size());
        ExecutionResultMultiSource oneResult = loadedResults.iterator().next();
        
        assertEquals(alignment, oneResult.getAlignment(Alignment.class));
        assertEquals(parameter, oneResult.getParameters(Properties.class));
        assertEquals("My matchername", oneResult.getMatcherName());
        assertEquals(urls, oneResult.getAllGraphs());
        assertEquals(testCases, oneResult.getTestCases());
        assertEquals(runtime, oneResult.getTotalRuntime());
        assertEquals(true, oneResult.isComputeTransitiveClosure());
        assertEquals(runtime, oneResult.getTotalRuntime());
        //assertEquals(partitioner, oneResult.getPartitioner());
        
        try {
            FileUtils.deleteDirectory(baseDirectory);
        } catch (IOException ioe){
            LOGGER.error("Could not clean up after test. Test directory 'testBaseDirectory' still exists on disk.", ioe);
        }
    }
}
