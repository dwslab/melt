package de.uni_mannheim.informatik.dws.melt.matching_base.external.cli.process;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles everything with external process
 * 
 * When no ProcessOutputConsumer is added, the default is to discard it.
 */
public class ExternalProcess {


    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalProcess.class);
    
    private static final String OS_NAME = System.getProperty("os.name");
    private static final boolean IS_LINUX = OS_NAME.startsWith("Linux") || OS_NAME.startsWith("LINUX");
    private static final boolean IS_WINDOWS = OS_NAME.startsWith("Windows");
    
    /**
     * The working directory to start the external process.
     */
    private File workingDirectory;
    
    /**
     * The variables in the environment which are used when starting an external process.
     */
    private Map<String, String> environment;
    
    /**
     * The arguments to start the external process
     */
    private List<ArgumentScope> arguments;
    
    /**
     * the list of function which can substitute the variables in the command line/ arguments
     */
    private List<Function<String, String>> substitutionLookups;
    
    /**
     * Time out for the external process.
     */
    private long timeout;

    /**
     * Time unit for the process time out.
     */
    private TimeUnit timeoutTimeUnit;
    
    /**
     * processes the strings which are printed on the std::out.
     */
    private List<ProcessOutputConsumer> outConsumer;
    
    /**
     * processes the strings which are printed on the std::err.
     */
    private List<ProcessOutputConsumer> errConsumer;
    
    /**
     * The time to wait between 
     */
    private long milliSecondsBetweenSigtermAndSigkill;
    
    /**
     * The time to wait to join the reading thread for stdOut and stdErr.
     * Zero to wait forever.
     */
    private long timeoutForReadingThreadJoin;

    public ExternalProcess(){
        this.workingDirectory = null;
        this.environment = new HashMap<>();        
        this.arguments = new ArrayList<>();
        this.substitutionLookups = new ArrayList<>();
        this.timeout = 0;
        this.timeoutTimeUnit = TimeUnit.SECONDS;
        this.outConsumer = new ArrayList<>();
        this.errConsumer = new ArrayList<>();
        this.milliSecondsBetweenSigtermAndSigkill = 3000;
        this.timeoutForReadingThreadJoin = 0;
    }

    public ExternalProcess(List<String> arguments){
        this();
        this.arguments.add(new ArgumentScope(arguments, false));
    }
    
    /**
     * Run the specified process in a synchronous manner. This means it blocks until the timeout is over or the
     * process is finished.
     * @return the exit status of the process
     * @throws TimeoutException in case the timeout was hit
     */
    public int run() throws TimeoutException {
        List<String> commands = new ArrayList<>();
        
        //TODO: check quoting
        //don't quote anything here; ProcessBuilder will already take care of this.
        //see https://blog.krecan.net/2008/02/09/processbuilder-and-quotes/
        if(IS_LINUX){
            commands.add("setsid");
        }
        List<String> substitutedArguments = this.getArguments();
        if(substitutedArguments.isEmpty()){
            throw new IllegalArgumentException("No arguments to start an external process");
        }
        if(substitutedArguments.get(0).contains("python")){
            //just to be sure...
            this.addEnvironmentVariableFromCondaActivate(substitutedArguments.get(0));
        }
        commands.addAll(substitutedArguments);
                        
        ProcessBuilder pb = new ProcessBuilder(commands);
        if(this.workingDirectory != null)
            pb.directory(this.workingDirectory);
        if(this.environment != null && !this.environment.isEmpty())
            pb.environment().putAll(this.environment);

        
        //LOGGER.info("Execute now following external process:\ncommand: {}\ndirectory: {}\ncustom environment variables: {}",
        //        String.join(" ", substitutedArguments),
        //        this.workingDirectory == null ? new File("./") : this.workingDirectory,
        //        this.environment == null ? "no" : this.environment);
        LOGGER.info("Execute now following external process (command, directory, custom environment variables):");
        LOGGER.info("command        : {}", String.join(" ", substitutedArguments));
        LOGGER.debug("cmd arguments  : {}", substitutedArguments);
        LOGGER.info("directory      : {}", this.workingDirectory == null ? FileUtil.getCanonicalPathIfPossible(new File("./")) : FileUtil.getCanonicalPathIfPossible(this.workingDirectory));
        LOGGER.info("environmentVars: {}", this.environment == null ? "no custom variables" : this.environment);
        
        Process process;
        try {
            process = pb.start();
        } catch (IOException ex) {
            LOGGER.error("IOException happened when starting the external process. The process is maybe started but " +
                    "no further stopping is executed.", ex);
            return 5;
        }
        
        try {
            Thread outCollectorThread = startReadingThread(process.getInputStream(), "ProcessStdOut", this.outConsumer);
            Thread errCollectorThread = startReadingThread(process.getErrorStream(), "ProcessStdErr", this.errConsumer);
            
            boolean matcherFinishesInTime = true;
            try {
                if(this.timeout > 0) {
                    matcherFinishesInTime = process.waitFor(this.timeout, this.timeoutTimeUnit);
                } else {
                    process.waitFor();
                }
            } catch (InterruptedException ex) {
                LOGGER.error("Interruption while waiting for matcher completion.", ex);
            }
            
            stopReadingThread(outCollectorThread);
            stopReadingThread(errCollectorThread);
                        
            closeAllProcessStreams(process);

            if(matcherFinishesInTime == false){
                throw new TimeoutException("External process did not finish within the given timeout of " + getTimeoutAsText());
            }
            
            return process.exitValue();
        }finally{
            terminateProcess(process);
        }
    }

    private static Thread startReadingThread(InputStream streamToCollect, String threadName, List<ProcessOutputConsumer> consumer){
        Thread thread;
        if(consumer.isEmpty()){
            thread = new OutputDiscardThread(streamToCollect);
        }else{
            thread = new OutputCollectorThread(streamToCollect, consumer);
        }
        thread.setName(threadName);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
    
    
    private void stopReadingThread(Thread thread){
        try {
            if (this.timeoutForReadingThreadJoin == 0) {
                thread.join();
            } else {
                long startTime = System.currentTimeMillis();
                thread.join(this.timeoutForReadingThreadJoin);
                if (System.currentTimeMillis() >= (startTime + this.timeoutForReadingThreadJoin)) {
                    LOGGER.error("Timeout of {} milliseconds for joining the read thread was not enough. Maybe the ProcessOutpuConsumer classes do not dprocessed all content.", this.timeoutForReadingThreadJoin);
                }
            }
        } catch (final InterruptedException e) {
            thread.interrupt();
        }
    }
    
    private void closeAllProcessStreams(Process p){
        try { p.getErrorStream().close(); } 
        catch (IOException ex) { LOGGER.warn("Could not close error stream of external process", ex); }
        try { p.getInputStream().close(); }
        catch (IOException ex) { LOGGER.warn("Could not close out stream of external process", ex); }
        try { p.getOutputStream().close(); }
        catch (IOException ex) { LOGGER.warn("Could not close in stream of external process", ex); }
    }
    
    /***************************************
     * timeout setter getter 
     * ***********************************/

    /**
     * Sets the timeout of a process
     * @param timeout the value of the timeout
     * @param timeoutTimeUnit the time unit of the value
     */
    public void setTimeout(long timeout, TimeUnit timeoutTimeUnit) {
        this.timeout = timeout;
        this.timeoutTimeUnit = timeoutTimeUnit;
    }

    /**
     * Rteurns the timput as long. The corresponding time unit can be retrived with {@link ExternalProcess#getTimeoutTimeUnit()}.
     * @return the timeout value
     */
    public long getTimeout() {
        return timeout;
    }
    
    /**
     * Return the timput unit
     * @return the timput unit
     */
    public TimeUnit getTimeoutTimeUnit() {
        return timeoutTimeUnit;
    }
    
    /**
     * Returns the timeout as text which includes the timeout value and timeout unit.
     * @return The timeout as text.
     */
    public String getTimeoutAsText() {
        return this.timeout + " " + this.timeoutTimeUnit.toString().toLowerCase();
    }

    
    /**
     * Gets the timeout to wait for joining the thread which reads the standard out and standard error of the external process.
     * @return  the timeout in milliseconds 
     */
    public long getTimeoutForReadingThreadJoin() {
        return timeoutForReadingThreadJoin;
    }

    /**
     * Sets the timeout to wait for joining the thread which reads the standard out and standard error of the external process.
     * Set it to zero to wait forever.
     * @param timeoutForReadingThreadJoin the timeout in milliseconds 
     */
    public void setTimeoutForReadingThreadJoin(long timeoutForReadingThreadJoin) {
        this.timeoutForReadingThreadJoin = timeoutForReadingThreadJoin;
    }

    /***************************************
     * Process Termination
     * ***********************************/

    /**
     * Terminates the process.
     * @param process the process to terminate
     */
    private void terminateProcess(Process process){
        if(process == null)
            return;
        if(process.isAlive()){
            LOGGER.info("External process is still alive - try to kill it now.");
            if(IS_LINUX){
                Long pid = getPid(process);
                if(pid == null){
                    killProcessWithJava(process);
                } else {
                    killAllProcessesWithSameSessionId(pid);
                    if(process.isAlive())
                        killProcessWithJava(process);
                }
            } else {
                killProcessWithJava(process);
            }
        }
    }
    
    private void killProcessWithJava(Process p){
        LOGGER.info("External process is now killed with vanilla java which might introduce an orphan process (in case the external process started a new subprocess).");
        try {
            p.destroyForcibly().waitFor(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            LOGGER.error("Interruption while forcibly terminating external process.", ex);
        }
    }
    
    private void killAllProcessesWithSameSessionId(Long pid){
        //kill $(ps -s 12345 -o pid=)
        //see https://unix.stackexchange.com/questions/124127/kill-all-descendant-processes
        LOGGER.info("The external process tree with the following session id (sid) is killed: {}", pid);
        try {
            LOGGER.info("Send SIGTERM to all processes with SID={}", pid);
            //Send SIGTERM to all matcher processes
            Process killProcess = new ProcessBuilder("/bin/bash", "-c", String.format("kill $(ps -s %s -o pid=)", pid)).start();
            killProcess.waitFor(10, TimeUnit.SECONDS);
            if(killProcess.isAlive())
                killProcess.destroyForcibly();
            
            // give some time to terminate the processes
            Thread.sleep(milliSecondsBetweenSigtermAndSigkill);
            
            //send SIGKILL to all matcher processes
            LOGGER.info("Send SIGKILL to all processes with SID={}", pid);
            killProcess = new ProcessBuilder("/bin/bash", "-c", String.format("kill -9 $(ps -s %s -o pid=)", pid)).start();
            killProcess.waitFor(10, TimeUnit.SECONDS);
            if(killProcess.isAlive())
                killProcess.destroyForcibly();
            //1 second to ensure termination really happend
            //usually not needed
            //Thread.sleep(1000);
        } catch (IOException | InterruptedException ex) {
            LOGGER.error("Could not destroy child processes", ex);
        }
    }
    
    /**
     * Obtains the process ID given a process.
     * @param process The process for which the ID shall be determined.
     * @return Process ID as Long of a given process.
     */
    private static Long getPid(Process process){
        Class<?> clazz = process.getClass();
        if (clazz.getName().equals("java.lang.UNIXProcess")) {
            try {
                Field pidField = clazz.getDeclaredField("pid");
                pidField.setAccessible(true);
                Object value = pidField.get(process);
                if (value instanceof Integer) {
                    return ((Integer) value).longValue();
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                LOGGER.error("Cannot get the PID of a Unix Process.", ex);
            }
        }
        return null;
    }

    /**
     * Returns the milli seconds between a sigterm and a sigkill when the process is started on linux.
     * @return the milli seconds between a sigterm and a sigkill
     */
    public long getMilliSecondsBetweenSigtermAndSigkill() {
        return milliSecondsBetweenSigtermAndSigkill;
    }

    /**
     * Sets the milli seconds between a sigterm and a sigkill when the process is started on linux.
     * @param milliSecondsBetweenSigtermAndSigkill  the milli seconds between a sigterm and a sigkill
     */
    public void setMilliSecondsBetweenSigtermAndSigkill(long milliSecondsBetweenSigtermAndSigkill) {
        this.milliSecondsBetweenSigtermAndSigkill = milliSecondsBetweenSigtermAndSigkill;
    }


    /****************************
      * OutputConsumer section  
      ****************************/
    
    /**
     * Add the consumer for the std:out processing. The consumer gets a new line whenever the process prints something on std:out.
     * @param consumer the consumer to use for std:out
     */
    public void addStdOutConsumer(ProcessOutputConsumer consumer){
        this.outConsumer.add(consumer);
    }
    
    /**
     * Removes all stdout consumers.
     */
    public void clearStdOutConsumers(){
        this.outConsumer.clear();
    }
    
    /**
     * Set the consumer for the std:err processing. The consumer gets a new line whenever the process prints something on std:err.
     * @param consumer the consumer to use for std:err
     */
    public void addStdErrConsumer(ProcessOutputConsumer consumer){
        this.errConsumer.add(consumer);
    }
    
    /**
     * Removes all stdout consumers.
     */
    public void clearStdErrConsumers(){
        this.errConsumer.clear();
    }
    
    
    /****************************
      * Environment Section
      ****************************/
    
    /**
     * Removes the user defined environment variables.
     */
    public void clearEnvironment(){
        this.environment.clear();
    }
    
    /**
     * Adds an environemtn varibale to the environment of the started process.
     * @param key the key (environment variable name)
     * @param value the value (environment variable value)
     */
    public void addEnvironmentVariable(String key, String value){
        this.environment.put(key, value);
    }
    
    /**
     * Adds multiple environment variables which are stored in a map.
     * @param map the map with additional environment variables
     */
    public void addEnvironmentVariableMap(Map<String, String> map){
        this.environment.putAll(map);
    }
    
    /****************************
     * EnvironmentPythonPathVariable section
     ****************************/
    
    /**
     * When starting a python process within a conda virtual environment, it is usually necessary to run conda activate env-name.
     * But since we only have the python executable, we have to modify the path variable as the activate command would do.
     * Call it after all arguments are specified.
     * @param firstArgument the first argument which should be the path to the python executable 
     * @see <a href="https://github.com/conda/conda/blob/7cb5f66dd46727ce8f16b969e084555e6221cfc5/conda/activate.py#L396">https://github.com/conda/conda/blob/7cb5f66dd46727ce8f16b969e084555e6221cfc5/conda/activate.py#L396</a>
     * @see <a href="https://stackoverflow.com/a/56479886/11951900">https://stackoverflow.com/a/56479886/11951900</a>
     */
    private void addEnvironmentVariableFromCondaActivate(String firstArgument){
        File executableFile = new File(firstArgument);
        if(!executableFile.exists()){
            return;
        }
        String prefix = executableFile.getParent();
        if(prefix == null)
            return;
        
        List<String> env = new ArrayList<>();
        //make same environment variables like activate:
        //https://github.com/conda/conda/blob/7cb5f66dd46727ce8f16b969e084555e6221cfc5/conda/activate.py#L396
        if(IS_WINDOWS){
            env.add(Paths.get(prefix, "Library", "mingw-w64", "bin").toString());
            env.add(Paths.get(prefix, "Library", "usr", "bin").toString());
            env.add(Paths.get(prefix, "Library", "bin").toString());
            env.add(Paths.get(prefix, "Scripts").toString());
            env.add(Paths.get(prefix, "bin").toString());
        }else{
            env.add(Paths.get(prefix, "bin").toString());
        }
        String originalPath = this.environment.get("PATH");
        if(originalPath != null){
            env.add(originalPath);
        }else{
            originalPath = System.getenv().get("PATH");
            if(originalPath != null){
                env.add(originalPath);
            }
        }
        this.environment.put("PATH", String.join(File.pathSeparator, env));
    }
    
                
    /****************************
     * WorkingDirectory section
     ****************************/     
    
    /**
     * Returns the working directory which will be used when starting a new process.
     * @return the working directory as a filel object
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Sets the working directory of the external process.
     * If null, it uses the working directory of the current Java process.
     * @param workingDirectory the new working directory
     */
    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
    

    /****************************
     * addArgument section
     ****************************/
    
    
    /**
     * Add one single argument like '-v' or 'test'.
     * @param argument the argument
     */
    public void addArgument(String argument){
        addArguments(Arrays.asList(argument));
    }

    
    /**
     * Add multiple arguments at once. They are all not in a scope.
     * @param arguments the arguments
     */
    public void addArguments(List<String> arguments){
        this.arguments.add(new ArgumentScope(arguments, false));
    }
    
    /**
     * Add multiple arguments at once. They are all not in a scope.
     * @param arguments the arguments
     */
    public void addArguments(String... arguments){
        this.arguments.add(new ArgumentScope(Arrays.asList(arguments), false));
    }
    
    
    /****************************
     * addArgumentScope section
     ****************************/
    
    
    /**
     * Adds multiple arguments in one scope. If one argument contains a variable like ${test}, 
     * then all arguments in this scope are only added if all variables in this scope can be replaced.
     * @param argumentLine the arguments as one string. they will be splitted by whitespaces.
     */
    public void addArgumentScope(String argumentLine){
        addArgumentScope(parseCommandLine(argumentLine));
    }
    
    /**
     * Adds multiple arguments in one scope. If one argument contains a variable like ${test}, 
     * then all arguments in this scope are only added if all variables in this scope can be replaced.
     * @param arguments the arguments
     */
    public void addArgumentScope(List<String> arguments){
        this.arguments.add(new ArgumentScope(arguments, true));
    }
    
    
    /****************************
     * addArgumentLine section
     ****************************/
    
   
    /**
     * Adds an argument line which is one (huge) string containing multiple arguments.
     * The line is splitted by whitespace but quotations are respected.
     * An argument line can contain scopes (scopes are only printed if all variables in a scope can be replaced).
     * @param argumentLine the argument line as one string
     */
    public void addArgumentLine(String argumentLine){        
        //search for argument scopes like $[.......] which is one scope.
        int startScope = 0;
        int openScope = 0;
        int endScope = 0;
        while((openScope = argumentLine.indexOf("$[", startScope)) >= 0){
            this.addArgumentLine(argumentLine.substring(startScope, openScope), false);
            
            endScope = argumentLine.indexOf(']', openScope);
            if(endScope < 0){
                throw new IllegalArgumentException("Argument line has a starting scope $[ but no closing scope ] : " + argumentLine);
            }
            this.addArgumentLine(argumentLine.substring(openScope + 2, endScope), true);
            startScope = endScope + 1;
        }
        this.addArgumentLine(argumentLine.substring(startScope, argumentLine.length()), false);
    }
    
    private void addArgumentLine(String argumentLine, boolean inScope){
        this.arguments.add(new ArgumentScope(parseCommandLine(argumentLine), inScope));
    }
    
    /****************************
     * Substitution loopkups section
     ****************************/
    
    /**
     * Clear all substitution lookups.
     */
    public void clearSubstitutionLoopkups() {
        this.substitutionLookups.clear();
    }
    
    /**
     * Adds all default subsitiution lookups:
     * <ul>
     * <li>SystemProperties</li>
     * <li>EnvironmentVariables</li>
     * <li>JVMArguments</li>
     * </ul>
     * 
     */
    public void addSubstitutionDefaultLookups() {
        addSubstitutionForSystemProperties();
        addSubstitutionForEnvironmentVariables();
        addSubstitutionForJVMArguments();
    }
    
    /**
     * Adds substitution map which replaces all variables in the command line with the elements in this map.
     * @param substitutions the substitution map
     */
    public void addSubstitutionMap(Map<String, Object> substitutions) {
        this.substitutionLookups.add(new Function<String, String>() {
            @Override
            public String apply(String t) {
                Object o = substitutions.get(t);
                if(o == null)
                    return null;
                if(o instanceof File){
                    return replaceFileSeparatorChar(((File) o).getAbsolutePath());
                }else{
                    return o.toString();
                }
            }
        });
    }
    
    /**
     * Adds substitution lookups for system properties. Uusually the following are interesting:
     * <ul>
     * <li>line.separator</li>
     * <li>file.separator</li>
     * <li>path.separator</li>
     * <li>user.dir</li>
     * <li>user.home</li>
     * <li>java.io.tmpdir</li>
     * </ul>
     * All of them has to be written as e.g. ${line.separator}
     */
    public void addSubstitutionForSystemProperties() {
        this.substitutionLookups.add(new Function<String, String>() {
            @Override
            public String apply(String t) {
                try {
                    return System.getProperty(t);
                } catch (final SecurityException | NullPointerException | IllegalArgumentException e) {
                    return null;
                }
            }
        });
    }
    
    /**
     * Adds substitution lookups for environment variables.
     * E.g. the string ${PATH} will be replaced with the corresponding path variable defined in the system.
     */
    public void addSubstitutionForEnvironmentVariables() {
        this.substitutionLookups.add(new Function<String, String>() {
            @Override
            public String apply(String t) {
                try {
                    return System.getenv(t);
                } catch (final SecurityException | NullPointerException e) {
                    return null;
                }
            }
        });
    }
    
    /**
     * Adds substitution lookups for JVM arguments such as -Xmx or -Xms.
     * The string ${Xmx} is replaced by e.g. -Xmx10G
     * Additionally 
     */
    public void addSubstitutionForJVMArguments() {
        this.substitutionLookups.add(new Function<String, String>() {
            @Override
            public String apply(String t) {                
                try {
                    for(String jvmArgument : ManagementFactory.getRuntimeMXBean().getInputArguments()){
                        if(jvmArgument.startsWith("-" + t)){
                            return jvmArgument;
                        }
                    }
                    if(t.equals("allJvmArguments")){
                        
                    }
                    return null;
                } catch (final SecurityException | NullPointerException e) {
                    return null;
                }
            }
        });
    }
    
    /**
     * Add your own substitution function.
     * @param func the fucntion which gets a string (the name of the parameter) and returns the value for or null if not available.
     */
    public void addSubstitutionFunction(Function<String, String> func) {
        this.substitutionLookups.add(func);
    }
    
    
    /**
     * Returns the substituted command line arguments.
     * @return the command line arguments
    */
    public List<String> getArguments(){
        List<String> substitutedArguments = new ArrayList<>();
        for(ArgumentScope scope : this.arguments){
            substitutedArguments.addAll(scope.getSubsitutedArguments(this.substitutionLookups));
        }
        return substitutedArguments;
    }
    
    
    /*********************************
     * Helper methods for parsing
     *********************************/
    
    /**
     * Replace slash and backslash with the platform dependend File.separatorChar.
     * @param text the text which should be replaced
     * @return the replaced text
     */
    private static String replaceFileSeparatorChar(String text) {
        return text.replace('/', File.separatorChar).replace('\\', File.separatorChar);
    }
    
    private static List<String> parseCommandLine(final String commandLine) {
        // parse with a FST (finite state machine)
        QuoteState state = QuoteState.NORMAL;
        StringTokenizer tok = new StringTokenizer(commandLine, "\"\' ", true);
        ArrayList<String> list = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
            case IN_QUOTE:
                if ("\'".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = QuoteState.NORMAL;
                } else {
                    current.append(nextTok);
                }
                break;
            case IN_DOUBLE_QUOTE:
                if ("\"".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = QuoteState.NORMAL;
                } else {
                    current.append(nextTok);
                }
                break;
            default:
                if ("\'".equals(nextTok)) {
                    state = QuoteState.IN_QUOTE;
                } else if ("\"".equals(nextTok)) {
                    state = QuoteState.IN_DOUBLE_QUOTE;
                } else if (" ".equals(nextTok)) {
                    if (lastTokenHasBeenQuoted || current.length() != 0) {
                        list.add(current.toString());
                        current = new StringBuilder();
                    }
                } else {
                    current.append(nextTok);
                }
                lastTokenHasBeenQuoted = false;
                break;
            }
        }
        
        if (lastTokenHasBeenQuoted || current.length() != 0) {
            list.add(current.toString());
        }

        if (state == QuoteState.IN_QUOTE || state == QuoteState.IN_DOUBLE_QUOTE) {
            throw new IllegalArgumentException("Unbalanced quotes in command line: " + commandLine);
        }
        return list;
    }
}

enum QuoteState {
    NORMAL,
    IN_QUOTE,
    IN_DOUBLE_QUOTE
}


class OutputDiscardThread extends Thread {
    private final InputStream streamToCollect;
    private final int bufferSize;
    
    public OutputDiscardThread(InputStream streamToCollect){
        this.streamToCollect = streamToCollect;
        this.bufferSize = 1024; // default
    }
    
    public OutputDiscardThread(InputStream streamToCollect, int bufferSize){
        this.streamToCollect = streamToCollect;
        this.bufferSize = bufferSize;
    }
    
    @Override
    public void run(){
        byte[] buf = new byte[this.bufferSize];
        try {
            while (this.streamToCollect.read(buf) > 0) {
                //do nothing
            }
        } catch (IOException ex) {
            //dont care about it
        }
    }
}

class OutputCollectorThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutputCollectorThread.class);
    
    private final InputStream streamToCollect;
    private final List<ProcessOutputConsumer> consumers;
    
    public OutputCollectorThread(InputStream streamToCollect, List<ProcessOutputConsumer> consumers){
        this.streamToCollect = streamToCollect;
        this.consumers = consumers;
    }
    
    @Override
    public void run(){
        Scanner sc = new Scanner(streamToCollect);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            for(ProcessOutputConsumer c : this.consumers){
                c.processOutput(line);
            }
        }
        
        for(ProcessOutputConsumer c : this.consumers){
            try {
                c.close();
            } catch (Exception ex) { 
                LOGGER.warn("Exception during closing a ProcessOutputConsumer. Not handeled.", ex);
            }
        }
    }
}



/**
 * An argument scope are multiple arguments but if an argument cannot be substituted, the whole argument scope will be empty.
 * E.g. for a named argument the name and the value will be in one argument scope. If the value cannot be replaced, then the whole argument scope is empty.
 * example: "-v ${myv}". If myv cannot be replaced then also the argument -v is not included. 
 */
class ArgumentScope {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentScope.class);

    private final List<String> arguments;
    private final boolean inScope;

    public ArgumentScope(Iterable<String> arguments, boolean inScope){            
        this.arguments = new ArrayList<>();
        for(String arg : arguments){
            arg = arg.trim();
            if(!arg.isEmpty())
                this.arguments.add(arg);
        }
        this.inScope = inScope;
    }

    public List<String> getSubsitutedArguments(List<Function<String, String>> substitutionLookups){
        List<String> subsitutedArguments = new ArrayList<>(this.arguments.size());
        for(String argument : this.arguments){                        
            StringBuilder subsitutedArgument = new StringBuilder();
            int startScope = 0;
            int openScope = 0;
            int endScope = 0;
            while((openScope = argument.indexOf("${", startScope)) >= 0){
                subsitutedArgument.append(argument.substring(startScope, openScope));

                endScope = argument.indexOf('}', openScope);
                if(endScope < 0){
                    throw new IllegalArgumentException("Closing } not found for opening ${ :" + argument.substring(startScope));
                }

                String argumentName = argument.substring(openScope + 2, endScope);
                
                String result = lookUp(argumentName, substitutionLookups);
                if (result == null) {
                    if(inScope){
                        // we are in a scope and cannot replace a variable, so the whole scope is removed
                        return new ArrayList<>(); 
                    } else {
                        if (argumentName.startsWith("!")){
                            //TODO: maybe change to a non runtime exception
                            throw new RuntimeException("No value found for : " + argumentName.substring(1));
                        } else {
                            //do nothing in else branch because we will silently ignore it.
                            LOGGER.debug("Variable with name \"{}\" in commandline is not found. It is replaced with empty string.");
                        }
                    }
                } else {
                    subsitutedArgument.append(result);
                }
                startScope = endScope + 1;
            }
            subsitutedArgument.append(argument.substring(startScope, argument.length()));
            String subsitutedArgumentString = subsitutedArgument.toString().trim();
            if (subsitutedArgumentString.isEmpty() == false)
                subsitutedArguments.add(subsitutedArgumentString);
        }
        return subsitutedArguments;
    }

    public List<String> getRawArguments() {
        return arguments;
    }
    
    private static String lookUp(String variableName, List<Function<String, String>> substitutionLookups){
        for(Function<String, String> lookup : substitutionLookups){
            String result = lookup.apply(variableName);
            if(result != null)
                return result;
        }
        return null;
    }
}