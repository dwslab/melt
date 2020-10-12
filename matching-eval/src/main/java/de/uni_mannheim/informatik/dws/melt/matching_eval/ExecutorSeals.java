package de.uni_mannheim.informatik.dws.melt.matching_eval;

import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;

/**
 * This executor can run SEALS matchers.
 * Please note that as of 2020, SEALS Client 7.0.5 requires JDK 1.8!
 * If you have a newer JDK installed and you run into trouble, you can install an additional JDK and use
 *
 * @author Jan Portisch
 */
public class ExecutorSeals {
    
    private static final String OS_NAME = System.getProperty("os.name");
    private static final boolean isLinux = OS_NAME.startsWith("Linux") || OS_NAME.startsWith("LINUX");

    /**
     * Path to the JAR of the SEALS client.
     */
    private File sealsClientJar;

    /**
     * Path to the SEALS home directory.
     */
    private File sealsHome;

    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorSeals.class);

    /**
     * Time out for the external seals process. The timeout is applied for each testcase and not track.
     */
    private long timeout;

    /**
     * Time unit for the process time out.
     */
    private TimeUnit timeoutTimeUnit;

    /**
     * The parameters that appear between java [parameters] -jar.
     * Example: ("-Xmx25g", "-Xms15g").
     */
    private List<String> javaRuntimeParameters;
        
    /**
     *  The directory where the results shall be written to.
     */
    private File resultsDirectory;
    
    /**
     *  If true, deletes files in tmp folder which starts with "alignment" before running a matcher
     */
    private boolean deleteTempFiles;

    /**
     * The command to start java in the terminal. Typically, this is "java"
     */
    private String javaCommand = "java";

    /**
     * Constructor
     *
     * @param sealsClientJar        The path to the local SEALS client JAR file.
     * @param sealsHome             SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     * @param resultsDirectory      Results folder where the SEALS results will be copied to
     * @param javaRuntimeParameters Runtime parameters such as ("-Xmx25g", "-Xms15g").
     * @param timeout               Timeout for one testcase as long.
     * @param timeoutTimeUnit       The unit of the timeout.
     * @param deleteTempFiles       If true, deletes files in tmp folder which starts with "alignment" before running a matcher
     */
    public ExecutorSeals(File sealsClientJar, File sealsHome, File resultsDirectory, List<String> javaRuntimeParameters, long timeout, TimeUnit timeoutTimeUnit, boolean deleteTempFiles) {
        this.sealsClientJar = sealsClientJar;
        this.sealsHome = sealsHome;
        this.resultsDirectory = resultsDirectory;
        this.javaRuntimeParameters = javaRuntimeParameters;
        this.timeout = timeout;
        this.timeoutTimeUnit = timeoutTimeUnit;
        this.deleteTempFiles = deleteTempFiles;
        
        if(this.sealsClientJar.exists() == false){
            LOGGER.error("Seals Client does not exist! The execution will fail.");
        }
        if(this.resultsDirectory.exists() == false){
            LOGGER.info("Created seals result folder: {}", this.resultsDirectory.getAbsolutePath());
            this.resultsDirectory.mkdirs();
        }        
    }

    /**
     * Constructor
     *
     * @param javaCommand           Command to start java.
     * @param sealsClientJar        The path to the local SEALS client JAR file.
     * @param sealsHome             SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     * @param resultsDirectory      Results folder where the SEALS results will be copied to
     * @param javaRuntimeParameters Runtime parameters such as ("-Xmx25g", "-Xms15g").
     * @param timeout               Timeout for one testcase as long.
     * @param timeoutTimeUnit       The unit of the timeout.
     * @param deleteTempFiles       If true, deletes files in tmp folder which starts with "alignment" before running a matcher
     */
    public ExecutorSeals(String javaCommand, File sealsClientJar, File sealsHome, File resultsDirectory, List<String> javaRuntimeParameters, long timeout, TimeUnit timeoutTimeUnit, boolean deleteTempFiles) {
        this.sealsClientJar = sealsClientJar;
        this.sealsHome = sealsHome;
        this.resultsDirectory = resultsDirectory;
        this.javaRuntimeParameters = javaRuntimeParameters;
        this.timeout = timeout;
        this.timeoutTimeUnit = timeoutTimeUnit;
        this.deleteTempFiles = deleteTempFiles;
        this.setJavaCommand(javaCommand);

        if(this.sealsClientJar.exists() == false){
            LOGGER.error("Seals Client does not exist! The execution will fail.");
        }
        if(this.resultsDirectory.exists() == false){
            LOGGER.info("Created seals result folder: {}", this.resultsDirectory.getAbsolutePath());
            this.resultsDirectory.mkdirs();
        }
    }

    /**
     * Constructor
     *
     * @param sealsClientJar        The path to the local SEALS client JAR file.
     * @param sealsHome             SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     * @param javaRuntimeParameters Runtime parameters such as ("-Xmx25g", "-Xms15g").
     * @param timeout               Timeout for one testcase as long.
     * @param timeoutTimeUnit       The unit of the timeout.
     */
    public ExecutorSeals(File sealsClientJar, File sealsHome, List<String> javaRuntimeParameters, long timeout, TimeUnit timeoutTimeUnit) {
        this(sealsClientJar, sealsHome, new File("sealsResults"), javaRuntimeParameters, timeout, timeoutTimeUnit, true);
    }

    /**
     * Constructor
     *
     * @param javaCommand           Command to start java.
     * @param sealsClientJar        The path to the local SEALS client JAR file.
     * @param sealsHome             SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     * @param javaRuntimeParameters Runtime parameters such as ("-Xmx25g", "-Xms15g").
     * @param timeout               Timeout for one testcase as long.
     * @param timeoutTimeUnit       The unit of the timeout.
     */
    public ExecutorSeals(String javaCommand, File sealsClientJar, File sealsHome, List<String> javaRuntimeParameters, long timeout, TimeUnit timeoutTimeUnit) {
        this(javaCommand, sealsClientJar, sealsHome, new File("sealsResults"), javaRuntimeParameters, timeout, timeoutTimeUnit, true);
    }

    /**
     * Constructor
     * The default timeout of 12 hours will be used.
     *
     * @param sealsClientJar        The path to the local SEALS client JAR file.
     * @param sealsHome             SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     * @param javaRuntimeParameters Runtime parameters such as ("-Xmx25g", "-Xms15g").
     */
    public ExecutorSeals(File sealsClientJar, File sealsHome, List<String> javaRuntimeParameters) {
        this(sealsClientJar, sealsHome, javaRuntimeParameters, 12, TimeUnit.HOURS);
    }

    /**
     * Constructor
     * The default timeout of 12 hours will be used.
     *
     * @param javaCommand           Command to start java.
     * @param sealsClientJar        The path to the local SEALS client JAR file.
     * @param sealsHome             SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     * @param javaRuntimeParameters Runtime parameters such as ("-Xmx25g", "-Xms15g").
     */
    public ExecutorSeals(String javaCommand, File sealsClientJar, File sealsHome, List<String> javaRuntimeParameters) {
        this(javaCommand, sealsClientJar, sealsHome, javaRuntimeParameters, 12, TimeUnit.HOURS);
    }

    /**
     * Constructor
     * The default timeout of 12 hours will be used.
     *
     * @param sealsClientJar The file to the local SEALS client JAR file.
     * @param sealsHome      SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     */
    public ExecutorSeals(File sealsClientJar, File sealsHome) {
        this(sealsClientJar, sealsHome, Arrays.asList());
    }

    /**
     * Constructor
     * The default timeout of 12 hours will be used.
     * @param javaCommand    Command to start java.
     * @param sealsClientJar The file to the local SEALS client JAR file.
     * @param sealsHome      SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     */
    public ExecutorSeals(String javaCommand, File sealsClientJar, File sealsHome) {
        this(javaCommand, sealsClientJar, sealsHome, Arrays.asList());
    }

    /**
     * Constructor
     *
     * @param sealsClientJarPath The path to the local SEALS client JAR file.
     * @param sealsHomePath      SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     */
    public ExecutorSeals(String sealsClientJarPath, String sealsHomePath) {
        this(new File(sealsClientJarPath), new File(sealsHomePath));
    }

    /**
     * Constructor
     *
     * @param javaCommand        Command to start java.
     * @param sealsClientJarPath The path to the local SEALS client JAR file.
     * @param sealsHomePath      SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     */
    public ExecutorSeals(String javaCommand, String sealsClientJarPath, String sealsHomePath) {
        this(javaCommand, new File(sealsClientJarPath), new File(sealsHomePath));
    }

    /**
     * Constructor
     *
     * @param sealsClientJar  The path to the local SEALS client JAR file.
     * @param sealsHome       SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     * @param timeout         Timeout as long.
     * @param timeoutTimeUnit The unit of the timeout.
     */
    public ExecutorSeals(File sealsClientJar, File sealsHome, long timeout, TimeUnit timeoutTimeUnit) {
        this.sealsClientJar = sealsClientJar;
        this.sealsHome = sealsHome;
        this.timeout = timeout;
        this.timeoutTimeUnit = timeoutTimeUnit;
    }


    /**
     * Evaluate matcher(s) using the local SEALS client.
     * The file parameter can be a zip file or a directory.
     * If it is a directory and the directory represents a matcher, it will be executed.
     * Otherwise the directory will be scanned for matcher subdirectories and/or zip files (which are unzipped).
     * The unzipping happens with every execution to ensure that the matcher is executed as submitted 
     * (in case some files are not closed corrected due to a previous run).
     * @param track   The track on which the matcher shall be run.
     * @param matcher The directory containing the matcher(s).
     * @return Execution Result Set instance for the matcher on the track.
     */
    public ExecutionResultSet run(Track track, String matcher) {
        return run(track, new File(matcher));
    }
    
    /**
     * Evaluate matcher(s) using the local SEALS client.
     * The file parameter can be a zip file or a directory.
     * If it is a directory and the directory represents a matcher, it will be executed.
     * Otherwise the directory will be scaned for matcher subdirectories and/or zip files (which are unzipped).
     * The unzipping happens with every execution to ensure that the matcher is executed as submitted 
     * (in case some files are not closed corrected due to a previous run).
     * @param testcase The testcase on which the matcher shall be run.
     * @param matcher The directory containing the matcher(s).
     * @return Execution Result Set instance for the matcher on the track.
     */
    public ExecutionResultSet run(TestCase testcase, String matcher) {
        return run(testcase, new File(matcher));
    }

    /**
     * Evaluate matcher(s) using the local SEALS client.
     * The file parameter can be a zip file or a directory.
     * If it is a directory and the directory represents a matcher, it will be executed.
     * Otherwise the directory will be scaned for matcher subdirectories and/or zip files (which are unzipped).
     * The unzipping happens with every execution to ensure that the matcher is executed as submitted 
     * (in case some files are not closed corrected due to a previous run).
     * @param track            The track on which the matcher shall be run.
     * @param matcherDirectory The directory containing the matcher(s).
     * @return Execution Result Set instance for the matcher on the track.
     */
    public ExecutionResultSet run(Track track, File matcherDirectory) {
        return run(track.getTestCases(), matcherDirectory);
    }
    
    /**
     * Evaluate matcher(s) using the local SEALS client.
     * The file parameter can be a zip file or a directory.
     * If it is a directory and the directory represents a matcher, it will be executed.
     * Otherwise the directory will be scaned for matcher subdirectories and/or zip files (which are unzipped).
     * The unzipping happens with every execution to ensure that the matcher is executed as submitted 
     * (in case some files are not closed corrected due to a previous run).
     * @param testCases        The testcases on which the matcher shall be run.
     * @param matcherDirectory The directory containing the matcher(s).
     * @return Execution Result Set instance for the matcher on the track.
     */
    public ExecutionResultSet run(List<TestCase> testCases, File matcherDirectory) {
        ExecutionResultSet resultSet = new ExecutionResultSet();
        for (TestCase testCase : testCases) {
            resultSet.addAll(run(testCase, matcherDirectory));
        }
        return resultSet;
    }
    
    
    /**
     * Evaluate matcher(s) using the local SEALS client.
     * The file parameter can be a zip file or a directory.
     * If it is a directory and the directory represents a matcher, it will be executed.
     * Otherwise the directory will be scaned for matcher subdirectories and/or zip files (which are unzipped).
     * The unzipping happens with every execution to ensure that the matcher is executed as submitted 
     * (in case some files are not closed corrected due to a previous run).
     * @param testCase The testcase on which the matcher shall be run.
     * @param matcher The directory containing the matcher(s).
     * @return Execution Result Set instance for the matcher on the track.
     */
    public ExecutionResultSet run(TestCase testCase, File matcher) {
        ExecutionResultSet resultSet = new ExecutionResultSet();
        
        for(Entry<File, String> entry : getMatcherDirectories(matcher).entrySet()){
            resultSet.add(runUnzippedMatcher(testCase, entry.getKey(), entry.getValue()));
        }
        return resultSet;
    }
    
    /**
     * Returns all possible matcher directories for a given file or directory.
     * If it is a file with zip extension, it will unzip it.
     * If it is a directory, it will check if this directory is a matcher.
     * If not, it will inspect the whole directory for matchers.
     * @param matcher the directoroy or file which represents a matcher or a directory of matchers.
     * @return a map of the matcher directory and corresponding matcher name(file name)
     */
    protected Map<File, String> getMatcherDirectories(File matcher){
        Map<File, String> map = new HashMap<>();
        if (!matcher.exists()) {
            LOGGER.error("The given matcher path does not exist. Returning no matchers.");
            return map;
        }
        if (matcher.isDirectory()) {
            if(isDirectoryRunnableInSeals(matcher)){
                map.put(matcher, getMatcherNameFromSealsDescriptor(matcher));
            } else {
                LOGGER.info("Inspect all direct subdirectories/subfiles(zip) in folder {}.", matcher);
                for (File fileInMatcher : matcher.listFiles()) {
                    if(fileInMatcher.isDirectory()){
                        File sealsMatcherDir = getFirstSubDirectoryRunnableInSeals(fileInMatcher);
                        if(sealsMatcherDir != null)
                            map.put(sealsMatcherDir, fileInMatcher.getName());
                    }else if(fileInMatcher.isFile() && fileInMatcher.getName().toLowerCase().endsWith(".zip")){
                        File unzippedDir = unzip(fileInMatcher);
                        File unzippedMatcher = getFirstSubDirectoryRunnableInSeals(unzippedDir);
                        if(unzippedMatcher != null){
                            map.put(unzippedMatcher, unzippedDir.getName());
                        }else{
                            LOGGER.error("Matcher folder is not runnable in SEALS: {}\n\tbased on zip file {}", unzippedMatcher, fileInMatcher);
                        }
                    }
                }
            }
        } else if (matcher.getName().endsWith(".zip")) {
            File unzippedDir = unzip(matcher);
            File unzippedMatcher = getFirstSubDirectoryRunnableInSeals(unzippedDir);
            if(unzippedMatcher != null){
                map.put(unzippedMatcher, unzippedDir.getName());
            }else{
                LOGGER.error("Matcher folder is not runnable in SEALS: {}\n\tbased on zip file {}", unzippedMatcher, matcher);
            }
        }
        return map;
    }
    
    
    /**
     * Evaluate a single matcher using the local SEALS client.
     *
     * @param matcherDirectory The directory containing the matcher
     * @param testCase         The testCase on which the matcher shall be run.
     * @return ExecutionResult
     */
    protected ExecutionResult runUnzippedMatcher(TestCase testCase, File matcherDirectory) {
        return runUnzippedMatcher(testCase, matcherDirectory, matcherDirectory.getName());
    }

    /**
     * Evaluate a single matcher using the local SEALS client.
     *
     * @param matcherDirectory The directory containing the matcher
     * @param testCase         The testCase on which the matcher shall be run.
     * @param matcherName      Matcher name
     * @return ExecutionResult
     */
    protected ExecutionResult runUnzippedMatcher(TestCase testCase, File matcherDirectory, String matcherName) {
        LOGGER.info("Run matcher {} (directory: {}) on testcase {} (track {})",
                matcherName, matcherDirectory, testCase.getName(), testCase.getTrack().getName());

        // results folder
        File resultsFolder = Paths.get(this.resultsDirectory.getAbsolutePath(), testCase.getName()).toFile();
        if (!resultsFolder.exists()) resultsFolder.mkdirs();
        
        LOGGER.info("Remove all files and folders in SEALS_HOME folder which is {}", this.sealsHome);
        try {
            FileUtils.deleteDirectory(this.sealsHome);
        } catch (IOException ex) {
            LOGGER.error("Could not delete SEALS_HOME folder " + this.sealsHome.toString(), ex);
        }
        this.sealsHome.mkdirs();
        if(this.deleteTempFiles)
            Executor.deleteTempFiles();
        

        File systemAlignmentToBeWritten = new File(resultsFolder, matcherName + ".rdf");
        File logfileToBeWritten = new File(resultsFolder, matcherName + "_log.txt");
        File errorFileToBeWritten = new File(resultsFolder, matcherName + "_error.txt");

        List<String> commands = new ArrayList<>();
        //don't quote anything here; ProcessBuilder will already take care of this.
        //see https://blog.krecan.net/2008/02/09/processbuilder-and-quotes/
        if(isLinux){
            commands.add("setsid");
        }
        commands.add(javaCommand);
        if (javaRuntimeParameters != null) commands.addAll(this.javaRuntimeParameters);
        commands.add("-jar");
        commands.add(this.sealsClientJar.getAbsolutePath());
        commands.add(matcherDirectory.getAbsolutePath());
        commands.add("-o");
        commands.add(testCase.getSource().toString());
        commands.add(testCase.getTarget().toString());
        if(testCase.getInputAlignment() != null)
            commands.add(testCase.getInputAlignment().toString());
        commands.add("-f");
        commands.add(systemAlignmentToBeWritten.getAbsolutePath());
        commands.add("-z");
        ProcessBuilder builder = new ProcessBuilder(commands);

        builder.redirectError(errorFileToBeWritten);
        builder.redirectOutput(logfileToBeWritten);
        builder.directory(this.sealsHome);

        String timeoutText = this.getTimeoutAsText();

        LOGGER.info("Run SEALS with command: {}",String.join(" ", builder.command()));
        LOGGER.info("Waiting for completion of matcher {} on test case {} with a timeout of {}." ,
                matcherName, testCase.getName(), timeoutText);
        
        boolean matcherFinishesInTime = true;
        Process process;
        long startTime = java.lang.System.currentTimeMillis();
        try {
            process = builder.start();
        } catch (IOException ex) {
            LOGGER.error("Could not start the SEALS process", ex);
            return null;
        }        
        try {
            matcherFinishesInTime = process.waitFor(this.timeout, this.timeoutTimeUnit);
        } catch (InterruptedException e) {
            LOGGER.error("Interruption while waiting for matcher completion.", e);
        }
        // this is rather a heuristic: reading of ontologies is added, as well as starting of seals
        long matcherRuntime = java.lang.System.currentTimeMillis() - startTime;
        terminateProcess(process);
        if(matcherFinishesInTime){
            LOGGER.info("Evaluation of matcher {} on test case {} completed within {} seconds.",matcherName, testCase.getName(), matcherRuntime/1000);
            try (FileWriter logWriter = new FileWriter(logfileToBeWritten, true)) {
                logWriter.append("MELT: Matcher finished within " + matcherRuntime/1000 + " seconds.");
            } catch (IOException ex) { LOGGER.warn("Could not write to matcher log file.", ex); }
        }else{
            LOGGER.warn("Evaluation of matcher {} on test case {} did not finish within the given timeout of {}.", matcherName, testCase.getName(), timeoutText);
            try (FileWriter errorWriter = new FileWriter(errorFileToBeWritten, true)) {
                errorWriter.append("MELT: Matcher did not finish within timeout of " + timeoutText);
            } catch (IOException ex) { LOGGER.warn("Could not write to matcher error file.", ex); }
        }
        
        URL systemAlignmentURL = null;
        try {
            systemAlignmentURL=systemAlignmentToBeWritten.toURI().toURL();
        } catch (MalformedURLException ex) {
            LOGGER.error("Could not transform originalSystemAlignment URI to URL.", ex);
        }
        
        ExecutionResult result = new ExecutionResult(testCase, matcherName, systemAlignmentURL, matcherRuntime, null);
        result.setMatcherLog(logfileToBeWritten);
        result.setMatcherErrorLog(errorFileToBeWritten);
        return result;
    }
    
    private static void terminateProcess(Process process){
        if(process == null)
            return;
        if(process.isAlive()){
            LOGGER.info("Matcher process is still alive - MELT is now trying to kill it.");
            if(isLinux){
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
    
    private static void killProcessWithJava(Process p){
        LOGGER.info("MELT kills now the matcher process (with vanilla java which might introduce an orphan process)");
        try {
            p.destroyForcibly().waitFor();
        } catch (InterruptedException ex) {
            LOGGER.error("Interruption while forcibly terminating seals/matcher process.", ex);
        }
    }
    
    private static void killAllProcessesWithSameSessionId(Long pid){
        //kill $(ps -s 12345 -o pid=)
        //see https://unix.stackexchange.com/questions/124127/kill-all-descendant-processes
        LOGGER.info("MELT kills all processes of seals matcher in session with sid {}", pid);
        try {
            //Send SIGTERM to all matcher processes
            LOGGER.info("MELT sending SIGTERM to all processes with SID={}", pid);
            Process p = new ProcessBuilder("/bin/bash", "-c", String.format("kill $(ps -s %s -o pid=)", pid)).start();
            p.waitFor(10, TimeUnit.SECONDS);
            if(p.isAlive())
                p.destroyForcibly();
            
            // give 3 seconds to terminate the processes
            Thread.sleep(3000);
            
            //send SIGKILL to all matcher processes
            LOGGER.info("MELT sending SIGKILL to all processes with SID={}", pid);
            p = new ProcessBuilder("/bin/bash", "-c", String.format("kill -9 $(ps -s %s -o pid=)", pid)).start();
            p.waitFor(10, TimeUnit.SECONDS);
            if(p.isAlive())
                p.destroyForcibly();
            //1 second to ensure termination really happend
            Thread.sleep(1000);
        } catch (IOException | InterruptedException ex) {
            LOGGER.error("Could not destroy matcher child processes", ex);
        }
    }

    /**
     * Obtains the process ID given a process.
     * @param process The process for which the ID shall be determined.
     * @return Process ID as Long of a given process.
     */
    private static Long getPid(Process process){
        if(isLinux == false)
            return null;
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
     * Regex pattern to get the matcher name from the SEALS {@code descriptor.xml} file.
     */
    private static final Pattern matcherNamePattern = Pattern.compile("<ns:package.*?id=\"(.*?)\"", Pattern.DOTALL); 
    
    /**
     * Returns the matcher name in the seals descriptor.
     * @param file File instance which points to a descriptor file or a matcher directory.
     * @return name of the matcher or empty string if not available.
     */
    public static String getMatcherNameFromSealsDescriptor(File file){
        File descriptorFile = null;
        if(file.isFile() && file.getName().equals("descriptor.xml")){
            descriptorFile = file;
        }else if(file.isDirectory()){
            descriptorFile = new File(file, "descriptor.xml");
        }else{
            LOGGER.info("Can not retrieve matcher name because given parameter is not a directory or a descriptor file.");
            return "";
        }
        
        if(descriptorFile.exists() == false){
            LOGGER.info("Can not retrieve matcher name because descriptor file does not exist.");
            return "";
        }
        String text = "";
        try {
            text = new String(Files.readAllBytes(descriptorFile.toPath()), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.info("Can not retrieve matcher name because descriptor file can not be read.", ex);
            return "";
        }
        
        Matcher regex = matcherNamePattern.matcher(text);
        if(regex.find() == false)
            return "";
        return regex.group(1);
    }
    
    public static String getMatcherNameFromSealsDescriptor(String file){
        return getMatcherNameFromSealsDescriptor(new File(file));
    }


    /**
     * Unzips a zip-file in the directory where the zip file resides.
     * A new directory will be created with the name of the zip file (without .zip ending) and the zipped contents
     * will be found therein.
     *
     * @param fileToBeUnzipped The file that shall be unzipped.
     * @return The path to the extracted file.
     */
    private static File unzip(File fileToBeUnzipped) {
        LOGGER.info("Unzipping " + fileToBeUnzipped.getName());
        try {
            String destDir = fileToBeUnzipped.getParentFile().getCanonicalPath() + File.separator + fileToBeUnzipped.getName().substring(0, fileToBeUnzipped.getName().length() - 4);
            File dir = new File(destDir);
            // create output directory if it doesn't exist
            if (!dir.exists()) dir.mkdirs();
            FileInputStream fis;
            //buffer for read and write data to file
            byte[] buffer = new byte[1024];
            fis = new FileInputStream(fileToBeUnzipped);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                if (ze.isDirectory()) {
                    newFile.mkdir();
                    ze = zis.getNextEntry();
                    continue;
                }
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
            return dir;
        } catch (IOException e) {
            LOGGER.warn("Cannot unzip matcher directory", e);
        }
        return null;
    }
    
    
    /**
     * This method visits all folders below the starting directory and returns the first directory which is runnable in seals.
     * The traversing strategy is breadth first search.
     * @param rootDir Path to the starting directory.
     * @return The first directory which is runnable in seals or null if such a folder does not exist
     */
    public static File getFirstSubDirectoryRunnableInSeals(File rootDir){
        if(rootDir == null || rootDir.exists() == false)
            return null;
        Queue<File> queue = new LinkedList<>();
        queue.add(rootDir);
        while (!queue.isEmpty())
        {
            File current = queue.poll();
            if(isDirectoryRunnableInSeals(current)){
                return current;
            }            
            File[] listOfDirectories = current.listFiles(File::isDirectory);
            if (listOfDirectories != null)
                queue.addAll(Arrays.asList(listOfDirectories));
        }
        return null;
    }
    
    /**
     * Determines whether the specified directory is runnable in seals.
     * @param directory Path to the directory.
     * @return True if runnable, else false.
     */
    public static boolean isDirectoryRunnableInSeals(File directory){
        if(directory.isFile()) return false;
        boolean containsBin = false;
        boolean containsLib = false;
        boolean containsConf = false;
        boolean containsDescriptor = false;

        for(File file : directory.listFiles()){
            if(file.isDirectory()){
                String name = file.getName();
                if(name.equals("bin")){
                    containsBin = true;
                } else if(name.equals("lib")){
                    containsLib = true;
                } else if(name.equals("conf")){
                    containsConf = true;
                }
            }else if(file.isFile() && file.getName().equals("descriptor.xml")){
                containsDescriptor = true;
            }
        }
        return containsBin && containsLib && containsConf && containsDescriptor;
    }


    /**
     * Determines whether the specified directory is runnable in seals.
     * @param directory Path to the directory.
     * @return True if runnable, else false.
     */
    public static boolean isDirectoryRunnableInSeals(String directory){
        return isDirectoryRunnableInSeals(new File(directory));
    }


    //----------------------------------------------------------------------------
    // Getters and Setters
    //----------------------------------------------------------------------------

    public void setTimeout(long timeout, TimeUnit timeoutTimeUnit) {
        this.timeout = timeout;
        this.timeoutTimeUnit = timeoutTimeUnit;
    }

    public long getTimeout() {
        return timeout;
    }

    public TimeUnit getTimeoutTimeUnit() {
        return timeoutTimeUnit;
    }
    
    public String getTimeoutAsText() {
        return this.timeout + " " + this.timeoutTimeUnit.toString().toLowerCase();
    }

    public File getSealsClientJar() {
        return sealsClientJar;
    }

    public void setSealsClientJar(File sealsClientJar) {
        this.sealsClientJar = sealsClientJar;
    }

    public File getSealsHome() {
        return sealsHome;
    }

    public void setSealsHome(File sealsHome) {
        this.sealsHome = sealsHome;
    }

    public List<String> getJavaRuntimeParameters() {
        return javaRuntimeParameters;
    }

    public void setJavaRuntimeParameters(List<String> javaRuntimeParameters) {
        this.javaRuntimeParameters = javaRuntimeParameters;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    /**
     * If in your system, the "java" command links to a jdk &gt; 8, issues might occur. Therefore, you can use
     * this command to point to a jdk 8 executable.
     * @param javaCommand The command to start the desired java version such as a file path to the executable.
     */
    public void setJavaCommand(String javaCommand) {
        if(javaCommand == null){
            LOGGER.error("Cannot set javaCommand null. The command will not be changed.");
            return;
        }
        this.javaCommand = javaCommand;
    }
}
