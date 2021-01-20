package de.uni_mannheim.informatik.dws.melt.matching_base.external.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the external matcher (especially the search url functions)
 */
public class ProcessOutputAlignmentCollectorTest {
    
    private String matcherresult = "2019-09-10 07:28:56 INFO  OntologyCacheJena:56 - Reading model into cache (http://repositories.seals-project.eu/tdrs/testdata/persistent/conference/conference-v1/suite/cmt-confof/component/source/)\n" +
" 2019-09-10 07:28:56 INFO  OntologyCacheJena:56 - Reading model into cache (http://repositories.seals-project.eu/tdrs/testdata/persistent/conference/conference-v1/suite/cmt-confof/component/target/)\n" +
" file:/tmp/alignment3572747689589156227.rdf\n"+
" Test";
    
    private List<String> possibleURIs = Arrays.asList(
            "file:/home/onetwothree.txt",
            "file:/tmp/alignment3572747689589156227.rdf",
            "file:/C:/Users/foobar/AppData/Local/Temp/alignment7123099207203017737.rdf",
            "file://localhost/etc/fstab",
            "file:///etc/fstab",
            "file://localhost/c$/WINDOWS/clock.avi",
            "file:///c:/WINDOWS/clock.avi",
            "file://hostname/path/to/the%20file.txt",
            "file:///c:/path/to/the%20file.txt",
            "file:////remotehost/share/dir/file.txt");
    
    @Test
    void getLastUrlTest() throws MalformedURLException {
        assertEquals(URI.create("file://foo.bar").toURL(), ProcessOutputAlignmentCollector.findLastURL("Test file://test.de\nand so on\nfile://foo.bar\nnext line with some content."));
        assertEquals(URI.create("file:/tmp/alignment3572747689589156227.rdf").toURL(), ProcessOutputAlignmentCollector.findLastURL(matcherresult));
        for(String uri : possibleURIs){
            assertEquals(URI.create(uri).toURL(), ProcessOutputAlignmentCollector.findLastURL("test\nFoo" + uri + " bar\n test\n"));
        }
    }
    
    @Test
    void testProcessWithAlignment() throws MalformedURLException, FileNotFoundException, IOException, TimeoutException, URISyntaxException {
        ProcessOutputAlignmentCollector alignment = new ProcessOutputAlignmentCollector();
        ExternalProcess process = new ExternalProcess();
        process.addArgumentLine("java PrintAlignment");
        process.addStdOutConsumer(alignment);        
        //process.addStdOutConsumer(l->System.out.println("Out:" + l));
        //process.addStdErrConsumer(l->System.out.println("Err:" +l));        
        process.setWorkingDirectory(new File("src/test/resources"));
        process.run();
        
        URL url = alignment.getURL();
        assertNotNull(url);
        
        File f = new File(url.toURI());
        assertTrue(f.exists());
        
        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            assertTrue(br.readLine().startsWith("<?xml"));
        }        
    }
    
    @Test
    void testProcessWithURL() throws MalformedURLException, FileNotFoundException, IOException, TimeoutException, URISyntaxException {
        ProcessOutputAlignmentCollector alignment = new ProcessOutputAlignmentCollector();
        ExternalProcess process = new ExternalProcess();
        process.addArgumentLine("java Main");
        process.addStdOutConsumer(alignment);
        process.setWorkingDirectory(new File("src/test/resources"));
        process.run();
        
        URL url = alignment.getURL();
        assertNotNull(url);
        
        File f = new File(url.toURI());
        assertTrue(f.exists());
        assertTrue(url.toString().endsWith("Main.java"));
    }
}
