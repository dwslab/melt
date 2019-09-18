package de.uni_mannheim.informatik.dws.ontmatching.matchingeval;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;

/**
 * This executor can run SEALS matchers.
 *
 * @author Jan Portisch
 */
public class ExecutorSeals {

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
    private static final Logger LOGGER = LoggerFactory.getLogger(Executor.class);

    /**
     * Time out for the external seals process. The timeout is applied for each testcase and not track.
     */
    private long timeout;

    /**
     * Time unit for the process time out.
     */
    private TimeUnit timoutTimeUnit;

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
     * Constructor
     *
     * @param sealsClientJar        The path to the local SEALS client JAR file.
     * @param sealsHome             SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     * @param resultsDirectory      Results folder where the SEALS results will be copied to
     * @param javaRuntimeParameters Runtime parameters such as ("-Xmx25g", "-Xms15g").
     * @param timeout               Timeout for one testcase as long.
     * @param timeoutTimeUnit       The unit of the timeout.
     */
    public ExecutorSeals(File sealsClientJar, File sealsHome, File resultsDirectory, List<String> javaRuntimeParameters, long timeout, TimeUnit timeoutTimeUnit) {
        this.sealsClientJar = sealsClientJar;
        this.sealsHome = sealsHome;
        this.resultsDirectory = resultsDirectory;
        this.javaRuntimeParameters = javaRuntimeParameters;
        this.timeout = timeout;
        this.timoutTimeUnit = timeoutTimeUnit;
        
        if(this.sealsClientJar.exists() == false){
            LOGGER.error("Seals Client does not exists");
        }
        if(this.resultsDirectory.exists() == false){
            LOGGER.info("created seals result folder: {}", this.resultsDirectory.getAbsolutePath());
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
        this(sealsClientJar, sealsHome, new File("sealsResults"), javaRuntimeParameters, timeout, timeoutTimeUnit);
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
     * @param sealsClientJar The file to the local SEALS client JAR file.
     * @param sealsHome      SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     */
    public ExecutorSeals(File sealsClientJar, File sealsHome) {
        this(sealsClientJar, sealsHome, Arrays.asList());
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
     * @param sealsClientJar  The path to the local SEALS client JAR file.
     * @param sealsHome       SEALS Home directory. ALL files in this directory will be removed (this is SEALS default behaviour).
     * @param timeout         Timeout as long.
     * @param timeoutTimeUnit The unit of the timeout.
     */
    public ExecutorSeals(File sealsClientJar, File sealsHome, long timeout, TimeUnit timeoutTimeUnit) {
        this.sealsClientJar = sealsClientJar;
        this.sealsHome = sealsHome;
        this.timeout = timeout;
        this.timoutTimeUnit = timeoutTimeUnit;
    }


    /**
     * Evaluate matcher(s) using the local SEALS client.
     * The file parameter can be a zip file or a directory.
     * If it is a directory and the directory represents a matcher, it will be executed.
     * Otherwise the directory will be scaned for matcher subdirectories and/or zip files (which are unzipped).
     * The unzipping happens with every execution to ensure that the matcher is executed as submitted 
     * (in case some files are not closed corrected due to a previous run).
     * @param track            The track on which the matcher shall be run.
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
        ExecutionResultSet resultSet = new ExecutionResultSet();
        for (TestCase testCase : track.getTestCases()) {
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
        
        Set<File> matcherDirs = getMatcherDirectories(matcher);
        List<File> sortedmatcherDirs = new ArrayList<>(matcherDirs.size());
        sortedmatcherDirs.addAll(matcherDirs);
        Collections.sort(sortedmatcherDirs);
        
        for(File matcherDir : sortedmatcherDirs){  
            LOGGER.info("Run matcher {} on testcase {}", matcherDir, testCase);
            resultSet.add(runUnzippedMatcher(testCase, matcherDir));
        }
        return resultSet;
    }
    
    /**
     * Returns all possible matcher directories for a given file or directory.
     * If it is a file with zip extension, it will unzip it.
     * If it is a directory, it will check if this directory is a matcher.
     * If not, it will inspect the whole directory for matchers.
     * @param matcher the directoroy or file which represents a matcher or a directory of matchers.
     * @return a set of ile which represent the possible matcher directories
     */
    protected Set<File> getMatcherDirectories(File matcher){
        Set<File> set = new HashSet<>();
        if (!matcher.exists()) {
            LOGGER.error("The given matcher path does not exist. Returning no matchers.");
            return set;
        }
        if (matcher.isDirectory()) {
            if(isDirectoryRunnableInSeals(matcher)){
                set.add(matcher);
            } else {
                LOGGER.info("Inspect all direct subdirectories/subfiles(zip) in folder {}.", matcher);
                for (File fileInMatcher : matcher.listFiles()) {
                    if(fileInMatcher.isDirectory()){
                        File sealsMatcherDir = getFirstSubDirectoryRunnableInSeals(fileInMatcher);
                        if(sealsMatcherDir != null)
                            set.add(sealsMatcherDir);
                    }else if(fileInMatcher.isFile() && fileInMatcher.getName().toLowerCase().endsWith(".zip")){
                        File unzippedMatcher = getFirstSubDirectoryRunnableInSeals(unzip(fileInMatcher));
                        if(unzippedMatcher != null){
                            set.add(unzippedMatcher);
                        }else{
                            LOGGER.error("Matcher folder is not runnable in SEALS: {}\n\tbased on zip file {}", unzippedMatcher, fileInMatcher);
                        }
                    }
                }
            }
        } else if (matcher.getName().endsWith(".zip")) {
            File unzippedMatcher = getFirstSubDirectoryRunnableInSeals(unzip(matcher));
            if(unzippedMatcher != null){
                set.add(unzippedMatcher);
            }else{
                LOGGER.error("Matcher folder is not runnable in SEALS: {}\n\tbased on zip file {}", unzippedMatcher, matcher);
            }
        }
        return set;
    }
    

    /**
     * Evaluate a single matcher using the local SEALS client.
     *
     * @param matcherDirectory The directory containing the matcher
     * @param testCase         The testCase on which the matcher shall be run.
     */
    protected ExecutionResult runUnzippedMatcher(TestCase testCase, File matcherDirectory) {

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

        File systemAlignmentToBeWritten = new File(resultsFolder, matcherDirectory.getName() + ".rdf");
        File logfileToBeWritten = new File(resultsFolder, matcherDirectory.getName() + "_log.txt");
        File errorFileToBeWritten = new File(resultsFolder, matcherDirectory.getName() + "_error.txt");

        List<String> commands = new ArrayList<>();
        //dont quote anything here; ProcessBuilder will already take care of.
        //see https://blog.krecan.net/2008/02/09/processbuilder-and-quotes/
        commands.add("java");
        if (javaRuntimeParameters != null) commands.addAll(this.javaRuntimeParameters);
        commands.add("-jar");
        commands.add(this.sealsClientJar.getAbsolutePath());
        commands.add(matcherDirectory.getAbsolutePath());
        commands.add("-o");
        commands.add(testCase.getSource().toString());
        commands.add(testCase.getTarget().toString());
        commands.add("-f");
        commands.add(systemAlignmentToBeWritten.getAbsolutePath());
        commands.add("-z");
        ProcessBuilder builder = new ProcessBuilder(commands);

        builder.redirectError(errorFileToBeWritten);
        builder.redirectOutput(logfileToBeWritten);
        builder.directory(this.sealsHome);

        String timeoutText = this.getTimeoutAsText();

        LOGGER.info("Run SEALS with command: {}",String.join(" ", builder.command()));
        LOGGER.info("Waiting for completion of matcher {} on test case {} with a timeout of {}.",matcherDirectory.getName(), testCase.getName(), timeoutText);
        
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
            matcherFinishesInTime = process.waitFor(this.timeout, this.timoutTimeUnit);
        } catch (InterruptedException e) {
            LOGGER.error("Interruption while waiting for matcher completion.", e);
        }
        // this is rather a heuristic: reading of ontologies is added, as well as starting of seals
        long matcherRuntime = java.lang.System.currentTimeMillis() - startTime;

        if(matcherFinishesInTime){
            LOGGER.info("Evaluation of matcher {} on test case {} completed within {} seconds.",matcherDirectory.getName(), testCase.getName(), matcherRuntime/1000);
            try (FileWriter logWriter = new FileWriter(logfileToBeWritten, true)) {
                logWriter.append("MELT: Matcher finished within " + matcherRuntime/1000 + " seconds.");
            } catch (IOException ex) { LOGGER.warn("Could not write to matcher log file.", ex); }
        }else{
            LOGGER.warn("Evaluation of matcher {} on test case {} did not finish within the given timeout of {}.", matcherDirectory.getName(), testCase.getName(), timeoutText);
            try (FileWriter errorWriter = new FileWriter(errorFileToBeWritten, true)) {
                errorWriter.append("MELT: Matcher did not finish within timeout of " + timeoutText + ". Killig matching process.");
            } catch (IOException ex) { LOGGER.warn("Could not write to matcher error file.", ex); }
        }
        
        URL systemAlignmentURL = null;
        try {
            systemAlignmentURL=systemAlignmentToBeWritten.toURI().toURL();
        } catch (MalformedURLException ex) {
            LOGGER.error("Could not transform originalSystemAlignment URI to URL.", ex);
        }
        
        ExecutionResult result = new ExecutionResult(testCase, matcherDirectory.getName(), systemAlignmentURL, matcherRuntime, null);
        result.setMatcherLog(logfileToBeWritten);
        result.setMatcherErrorLog(errorFileToBeWritten);
        return result;
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

    public void setTimeout(long timeout, TimeUnit timoutTimeUnit) {
        this.timeout = timeout;
        this.timoutTimeUnit = timoutTimeUnit;
    }

    public long getTimeout() {
        return timeout;
    }

    public TimeUnit getTimoutTimeUnit() {
        return timoutTimeUnit;
    }
    
    public String getTimeoutAsText() {
        return this.timeout + " " + this.timoutTimeUnit.toString().toLowerCase();
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
}
