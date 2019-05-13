package de.uni_mannheim.informatik.dws.ontmatching.matchingexternal;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.MatcherURL;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Matcher for running external matchers (require the subclass to create a command to execute).
 */
public abstract class MatcherExternal extends MatcherURL {
    private static final String NEWLINE = System.getProperty("line.separator");
    
    /**
     * if set to true, all logging should go to stderr and the result of the process (url or alignment api format) should go to stdout.
     * if set to false, all logging should go to stdout and the result of the process (url or alignment api format) should go to stderr.
     * @return true, all logging should go to stderr and the result of the process (url or alignment api format) should go to stdout, false otherwise
     */
    protected boolean isUsingStdOut(){
        return true;
    }
    
    /**
     * The command which should be executed. For example new ArrayList(Arrays.asList("java", "-jar", "myjar.jar", source));.
     * @param source source url
     * @param target target url
     * @param inputAlignment url of input alignment
     * @return the command as a list of strings
     * @throws java.lang.Exception
     */
    protected abstract List<String> getCommand(URL source, URL target, URL inputAlignment) throws Exception;
    
    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        Process process = startProcess(source, target, inputAlignment);
        
        int errCode = process.waitFor(); // wait for the matcher to finish
        if(errCode != 0){
            System.err.println("Error code of external matcher is not equal to 0.");
        }
        String resultOfProcess = getResultOfProcess(process);
        resultOfProcess = resultOfProcess.trim(); //remove all spaces and newline at the start or end of the string
        
        if(resultOfProcess.isEmpty())
            throw new IllegalArgumentException("The external matcher returned an empty result.");
        
        URL returnValue = null;
        try {
            returnValue = new URL(resultOfProcess);
        } catch (MalformedURLException ex) {
            System.err.println("Output of external matcher is not a URL try to use it as file content...");
            returnValue = getUrlOfTempFileWithContent(resultOfProcess);
        }
        closeAllStreams(process);
        return returnValue;
    }
    
    protected URL getUrlOfTempFileWithContent(String content) throws IOException{
        File alignmentFile = File.createTempFile("alignment", ".rdf");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(alignmentFile))) {
            out.write(content);
        }
        return alignmentFile.toURI().toURL();
    }
    
    private Process startProcess(URL source, URL target, URL inputAlignment) throws Exception{
        //https://examples.javacodegeeks.com/core-java/lang/processbuilder/java-lang-processbuilder-example/
        List<String> command = getCommand(source, target, inputAlignment);
        ProcessBuilder pb = new ProcessBuilder(command);//"python", "C:\\dev\\OntMatching\\ontMatching\\test.py", source.toString(), target.toString(), inputAlignment.toString());
        //pb.redirectInput(Redirect.INHERIT); // no need because the process gets no further input than the process parameters
        //pb.redirectOutput(Redirect.INHERIT); // no need because we want to collect it
        //pb.redirectError(Redirect.INHERIT); // redirect err pipe because of all logging etc
        if(isUsingStdOut()){
            pb.redirectError(Redirect.INHERIT);
        }
        else{
            pb.redirectInput(Redirect.INHERIT);
        }
        System.err.println("Start external matcher with command: " + String.join(" ", command));
        return pb.start();
    }
    
    private String getResultOfProcess(Process process) throws IOException{
        if(isUsingStdOut()){
            return streamToString(process.getInputStream());
        }
        else{
            return streamToString(process.getErrorStream());
        }
    }
    
    private static String streamToString(InputStream stream) throws IOException {        
        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(NEWLINE);
            }
        }
        return sb.toString();
    }
    private static void closeAllStreams(Process p){
        try { p.getErrorStream().close(); } catch (IOException ex) {}
        try { p.getInputStream().close(); } catch (IOException ex) {}
        try { p.getOutputStream().close(); } catch (IOException ex) {}
    }
}
