package de.uni_mannheim.informatik.dws.melt.matching_eval;

import de.uni_mannheim.informatik.dws.melt.matching_base.MatcherURL;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParallelExecution {


    class LongTimeMatcher extends MatcherURL{
        private URL url;
        public LongTimeMatcher(URL url){
            this.url = url;
        }
        @Override
        public URL match(URL source, URL target, URL inputAlignment) throws Exception {
            Thread.sleep(2000);
            return url;
        }        
    }

    /**
     * There will be error log messages stating that the URL does not point to a file.
     * This is intended, the test will still be successful (something else is tested here).
     * @throws MalformedURLException Should not be thrown.
     */
    @Test
    //@EnabledOnOs({ MAC })
    public void testParallelExecution() throws MalformedURLException{
        URL one = new URL("http", "one", "");
        URL two = new URL("http", "two", "");
        
        Map<String, Object> matchers = new HashMap<>();
        matchers.put("Test", new LongTimeMatcher(one));
        matchers.put("Test2", new LongTimeMatcher(two));
        ExecutionResultSet er = new ExecutorParallel().run(TrackRepository.Anatomy.Default.getTestCases(), matchers);
        Set<URL> urls = er.stream().map(result->result.getOriginalSystemAlignment()).collect(Collectors.toSet());
        assertTrue(urls.contains(one));
        assertTrue(urls.contains(two));
    }
    
    
    
}
