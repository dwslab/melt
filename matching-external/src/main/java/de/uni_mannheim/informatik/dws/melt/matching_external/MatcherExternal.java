package de.uni_mannheim.informatik.dws.melt.matching_external;

import de.uni_mannheim.informatik.dws.melt.matching_base.MatcherURL;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for running external matchers (require the subclass to create a command to execute).
 */
public abstract class MatcherExternal extends MatcherURL {
    private static final String NEWLINE = System.getProperty("line.separator");
    private static Pattern URL_PATTERN = Pattern.compile("(?:https?|ftp|file)://?[^\\s]*",Pattern.CASE_INSENSITIVE);
    
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
     * @throws java.lang.Exception Exception
     */
    protected abstract List<String> getCommand(URL source, URL target, URL inputAlignment) throws Exception;
    
    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        Process process = startProcess(source, target, inputAlignment);
        
        int errCode = process.waitFor(); // wait for the matcher to finish
        if(errCode != 0){
            System.err.println("External Matcher return with error code " + Integer.toString(errCode) + ". Continue....");
        }
        String resultOfProcess = getResultOfProcess(process);
        resultOfProcess = resultOfProcess.trim(); //remove all spaces and newline at the start or end of the string
        
        if(resultOfProcess.isEmpty())
            throw new IllegalArgumentException("The external matcher returned an empty result.");
        
        URL returnValue = null;
        try {
            returnValue = new URL(resultOfProcess);
        } catch (MalformedURLException ex) {
            System.err.println("The external matcher did not return solely a file URL. Probably configure your matcher to log all messages to std out or std err. Try now to find a URL in the result which is printed below:");
            System.err.println(resultOfProcess);//printed because log messages are probably contained therein
            returnValue = getLastUrlInString(resultOfProcess);
            if(returnValue == null){
                System.err.println("Did not find any URL in the result of the process. Backup is to use the result as file content. Be warned....");
                returnValue = getUrlOfTempFileWithContent(resultOfProcess);
            }else{
                System.err.println("Found following URL: " + returnValue);
            }
        }
        closeAllStreams(process);
        return returnValue;
    }
    
    protected URL getLastUrlInString(String text){        
        Matcher matcher = URL_PATTERN.matcher(text);
        String urlString = null;
        while (matcher.find()) {
            urlString = matcher.group();
        }        
        if(urlString == null){
            return null;
        }
        try {
            return new URL(urlString);
        } catch (MalformedURLException ex) {
            return null;
        }
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
