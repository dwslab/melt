package de.uni_mannheim.informatik.dws.melt.matching_external;

import de.uni_mannheim.informatik.dws.melt.matching_base.MatcherURL;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Matcher for running external matchers (require the subclass to create a command to execute).
 */
public abstract class MatcherExternal extends MatcherURL {
    private static final Pattern MULTIPLE_WHITESPACES = Pattern.compile(" +");
    
    private static final String RUNTIME_XMX = getRuntimeArgument("xmx");
    private static final String RUNTIME_XMS = getRuntimeArgument("xms");
    
    
    private ProcessOutputCollector resultOutputCollector; // usually std out
    private ProcessOutputRedirect logOutputCollector; // usually std err
    
    /**
     * Returns a runtime argument of the java virtual maschine like -Xmx (which specifies java heap size) and not main program arguments.
     * It is checked if the given argument(parameter) is contained in one of the arguments of the program.
     * If this is the case, it is returned (the full argument like "-Xmx100G" when "xmx" is the parameter of this function).
     * @param argument the runtime argument to check for (like "xmx")
     * @return the full argument like "-Xmx100G" (empty string if no argument is found)
     */
    private static String getRuntimeArgument(String argument){
        for(String s : ManagementFactory.getRuntimeMXBean().getInputArguments()){
            if(s.toLowerCase().contains(argument.toLowerCase())){
                return s;
            }
        }
        return "";
    }
    
    /**
     * if set to true, all logging should go to stderr and the result of the process (url or alignment api format) should go to stdout.
     * if set to false, all logging should go to stdout and the result of the process (url or alignment api format) should go to stderr.
     * @return true, all logging should go to stderr and the result of the process (url or alignment api format) should go to stdout, false otherwise
     */
    protected boolean isUsingStdOut(){
        return true;
    }
    
    /**
     * The command which should be executed. For example {@code new ArrayList(Arrays.asList("java", "-jar", "myjar.jar", source));}.
     * @param source Source URL
     * @param target Target URL
     * @param inputAlignment URL of input alignment.
     * @return The command as a list of strings.
     * @throws java.lang.Exception Exception
     */
    protected abstract List<String> getCommand(URL source, URL target, URL inputAlignment) throws Exception;
    
    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        
        Process process = startProcess(source, target, inputAlignment);
        if(isUsingStdOut()){
            this.resultOutputCollector = new ProcessOutputCollector(process.getInputStream(), "ExternalMatcher (OUT): ", System.out);
            this.logOutputCollector = new ProcessOutputRedirect(process.getErrorStream(), "ExternalMatcher (ERR): ", System.out);
        }else{
            this.resultOutputCollector = new ProcessOutputCollector(process.getErrorStream(), "ExternalMatcher (ERR): ", System.out);
            this.logOutputCollector = new ProcessOutputRedirect(process.getInputStream(), "ExternalMatcher (OUT): ", System.out);
        }
        this.resultOutputCollector.start();
        this.logOutputCollector.start();
        
        int errCode = process.waitFor(); // wait for the matcher to finish
        if(errCode != 0){
            System.err.println("External Matcher return with error code " + Integer.toString(errCode) + ". Continue....");
        }
        
        URL lastURL = this.resultOutputCollector.getURL();
        if(lastURL == null){
            if(isUsingStdOut()){
                System.err.println("Could not find a (file) URL in standard output of external matcher. Check log messages prefixed with ExternalMatcher (OUT).");
                throw new IllegalArgumentException("Could not find a (file) URL in standard output of external matcher.");
            }else{
                System.err.println("Could not find a (file) URL in error output of external matcher. Check log messages prefixed with ExternalMatcher (ERR).");
                throw new IllegalArgumentException("Could not find a (file) URL in error output of external matcher.");
            }
        }
        System.err.println("Found following URL: " + lastURL);
        closeAllStreams(process);
        return lastURL;
    }
    
    protected URL getUrlOfTempFileWithContent(String content) throws IOException{
        File alignmentFile = File.createTempFile("alignment", ".rdf");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(alignmentFile))) {
            out.write(content);
        }
        return alignmentFile.toURI().toURL();
    }
    
    protected Process startProcess(URL source, URL target, URL inputAlignment) throws Exception{
        //https://examples.javacodegeeks.com/core-java/lang/processbuilder/java-lang-processbuilder-example/
        List<String> command = getCommand(source, target, inputAlignment);
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(System.getProperty("user.dir")));
        System.err.println("Start external matcher in folder " + pb.directory().toString() + " with command: " + String.join(" ", command));
        return pb.start();
    }
    
    protected void closeAllStreams(Process p){
        try { p.getErrorStream().close(); } catch (IOException ex) {}
        try { p.getInputStream().close(); } catch (IOException ex) {}
        try { p.getOutputStream().close(); } catch (IOException ex) {}
    }
    
    /**
     * Replaces a string with the following mapping:
     * <ul>
     * <li>"{File.separator}" with the os dependent file separator. On UNIX systems the value is <code>'/'</code>; on Microsoft Windows systems it is <code>'\\'</code>. </li>
     *  <li>"{File.pathSeparator}" with the os dependent path separator. It is used to separate filenames in a sequence of files given as a <em>path list</em>.
     * On UNIX systems, this character is <code>':'</code>; on Microsoft Windows systems it is <code>';'</code>.</li>
     * <li>"{xmx}" with the value xmx value the java process is started with (e.g. "{xmx}" is replaced with "-Xmx100G")</li>
     * <li>"{xms}" with the value xms value the java process is started with (e.g. "{xms}" is replaced with "-Xms10G")</li>
     * </ul>
     * @param s the string which should be replaced.
     * @return the replaced string
     */
    protected String replaceString(String s){
        String text = s.replace("\r", "").replace("\n", "")
                .replace("{File.pathSeparator}", File.pathSeparator)
                .replace("{File.separator}", File.separator)
                .replace("{xmx}", RUNTIME_XMX)
                .replace("{xms}", RUNTIME_XMS)
                .trim();
        //replace multiple whitespaces by one
        return MULTIPLE_WHITESPACES.matcher(text).replaceAll(" ");
    }
    
    /**
     * Replaces a string with the following mapping:
     * <ul>
     * <li>"{File.separator}" with the os dependent file separator.On UNIX systems the value is <code>'/'</code>; on Microsoft Windows systems it is <code>'\\'</code>.</li>
     * <li>"{File.pathSeparator}" with the os dependent path separator.It is used to separate filenames in a sequence of files given as a <em>path list</em>.On UNIX systems, this character is <code>':'</code>; on Microsoft Windows systems it is <code>';'</code>.</li>
     * <li>"{xmx}" with the value xmx value the java process is started with (e.g. "{xmx}" is replaced with "-Xmx100G")</li>
     * <li>"{xms}" with the value xms value the java process is started with (e.g. "{xms}" is replaced with "-Xms10G")</li>
     * <li>"{source}" with the URL of the source ontology</li>
     * <li>"{target}" with the URL of the target ontology</li>
     * <li>"{inputAlignment}" with the URL of the source ontology</li>
     * </ul>
     * @param s the string which should be replaced.
     * @param source the source URL - should not be null
     * @param target the target URL - should not be null
     * @param inputAlignment the input alignment URL - can be null
     * @return the replaced string
     */
    protected String replaceString(String s, URL source, URL target, URL inputAlignment){
        String text = s.replace("\r", "").replace("\n", "")
                .replace("{File.pathSeparator}", File.pathSeparator)
                .replace("{File.separator}", File.separator)
                .replace("{xmx}", RUNTIME_XMX)
                .replace("{xms}", RUNTIME_XMS)
                .replace("{source}", source.toString())
                .replace("{target}", target.toString())
                .replace("{inputAlignment}", inputAlignment == null ? "" : inputAlignment.toString())
                .trim();
        //replace multiple whitespaces by one
        return MULTIPLE_WHITESPACES.matcher(text).replaceAll(" ");
    }
}
