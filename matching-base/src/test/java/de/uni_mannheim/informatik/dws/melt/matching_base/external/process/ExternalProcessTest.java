package de.uni_mannheim.informatik.dws.melt.matching_base.external.process;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExternalProcessTest {
        
    /**
     * This tests if the output of the external process is handeled and no buffer overflow etc happens
     * @throws TimeoutException 
     */
    @Test
    public void testExternalProcess() throws TimeoutException{ 
        ExternalProcess process = new ExternalProcess();
        process.addArgumentLine("java Main 1000");
        //process.addStdOutConsumer(line -> System.out.println(line));
        process.setWorkingDirectory(new File("src/test/resources"));
        process.run();
    }    
    
    @Test
    public void checkArgumentParsing() throws TimeoutException{
        ArgumentCollector arguments = new ArgumentCollector();
        ExternalProcess process = new ExternalProcess();
        process.addArguments("java", "PrintArguments", "foo", "bar", "with whitespace", "\"with quotes\"");
        process.addStdOutConsumer(arguments);
        process.setWorkingDirectory(new File("src/test/resources"));
        process.run();
        assertEquals(Arrays.asList("foo", "bar", "with whitespace", "with quotes"), arguments.getOutput());
    }
    
    @Test
    public void checkArgumentParsingTwo() throws TimeoutException{
        ArgumentCollector arguments = new ArgumentCollector();
        ExternalProcess process = new ExternalProcess();
        process.addArgumentLine("java PrintArguments foo bar \"with whitespace\" last");
        process.addStdOutConsumer(arguments);
        process.setWorkingDirectory(new File("src/test/resources"));
        process.run();
        assertEquals(Arrays.asList("foo", "bar", "with whitespace", "last"), arguments.getOutput());
    }
    
    /*******************************
     * Test argument parsing
     *******************************/
    
    @Test
    void testReplaceSystemVariables() {
        System.getProperties().setProperty("foo", "bar");
        ExternalProcess p = new ExternalProcess();
        p.addArgumentLine("-i ${foo}");
        p.addSubstitutionDefaultLookups();
        assertEquals(Arrays.asList("-i", "bar"), p.getArguments());
    }
    
    @Test
    void testWhitespace() {
        ExternalProcess p = new ExternalProcess();
        p.addArgumentLine("-v      -k   \"test with whitespace\"");        
        assertEquals(Arrays.asList("-v", "-k", "\"test with whitespace\""), p.getArguments());
    }
    
    @Test
    void testArgumentScope() {
        ExternalProcess p = new ExternalProcess();
        p.addArgumentLine("-v $[ -i  ${input}]");
        assertEquals(Arrays.asList("-v"), p.getArguments());
        
        Map<String, Object> one = new HashMap();
        one.put("input", "x");
        p.addSubstitutionMap(one);
        
        assertEquals(Arrays.asList("-v", "-i", "x"), p.getArguments());
    }
    
    @Test
    void testArgumentScopeNotClosed() {
        ExternalProcess p = new ExternalProcess();
        assertThrows(IllegalArgumentException.class, ()-> p.addArgumentLine("-v $[ -i  ${input}"));
    }
    
    @Test
    void testArgumentVariableNotClosed() {
        ExternalProcess p = new ExternalProcess();
        p.addArgumentLine("-i  ${input");         
        assertThrows(IllegalArgumentException.class, ()-> p.getArguments());
    }
    
    
    @Test
    void testSubstitutionMap() {
        ExternalProcess p = new ExternalProcess();
        Map<String, Object> one = new HashMap();
        one.put("one", "x");
        Map<String, Object> two = new HashMap();
        two.put("two", "y");
        p.addSubstitutionMap(one);
        p.addSubstitutionMap(two);
        p.addArgumentLine("hello ${one} ${two}");
        assertEquals(Arrays.asList("hello", "x", "y"), p.getArguments());
    }
}

class ArgumentCollector implements ProcessOutputConsumer{
    private List<String> output = new ArrayList(); 
    
    @Override
    public void processOutput(String line) {
        output.add(line);
    }
    
    public List<String> getOutput() {
        return output;
    }
}