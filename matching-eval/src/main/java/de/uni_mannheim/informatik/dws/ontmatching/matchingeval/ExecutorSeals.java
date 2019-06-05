package de.uni_mannheim.informatik.dws.ontmatching.matchingeval;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
     * Time out for a process.
     */
    long timeout = 12;

    /**
     * Time unit for the process time out.
     */
    TimeUnit timoutTimeUnit = TimeUnit.HOURS;

    /**
     * The parameters that appear between java [parameters] -jar.
     * Example: ("-Xmx25g", "-Xms15g").
     */
    private List<String> javaRuntimeParameters;

    /**
     * Constructor
     *
     * @param sealsClientJar        The path to the local SEALS client JAR file.
     * @param sealsHome             SEALS Home directory
     * @param javaRuntimeParameters Runtime parameters such as ("-Xmx25g", "-Xms15g").
     * @param timeout               Timeout as long.
     * @param timeoutTimeUnit       The unit of the timeout.
     */
    public ExecutorSeals(File sealsClientJar, File sealsHome, List<String> javaRuntimeParameters, long timeout, TimeUnit timeoutTimeUnit) {
        this.sealsClientJar = sealsClientJar;
        this.sealsHome = sealsHome;
        this.javaRuntimeParameters = javaRuntimeParameters;
        this.timeout = timeout;
        this.timoutTimeUnit = timeoutTimeUnit;
    }

    /**
     * Constructor
     * The default timeout will be used.
     *
     * @param sealsClientJar        The path to the local SEALS client JAR file.
     * @param sealsHome             SEALS Home directory
     * @param javaRuntimeParameters Runtime parameters such as ("-Xmx25g", "-Xms15g").
     */
    public ExecutorSeals(File sealsClientJar, File sealsHome, List<String> javaRuntimeParameters) {
        this.sealsClientJar = sealsClientJar;
        this.sealsHome = sealsHome;
        this.javaRuntimeParameters = javaRuntimeParameters;
    }

    /**
     * Constructor
     * The default timeout will be used.
     *
     * @param sealsClientJar The file to the local SEALS client JAR file.
     * @param sealsHome      SEALS Home directory.
     */
    public ExecutorSeals(File sealsClientJar, File sealsHome) {
        this.sealsClientJar = sealsClientJar;
        this.sealsHome = sealsHome;
    }

    /**
     * Constructor
     *
     * @param sealsClientJarPath The path to the local SEALS client JAR file.
     * @param sealsHomePath      SEALS Home directory.
     */
    public ExecutorSeals(String sealsClientJarPath, String sealsHomePath) {
        this(new File(sealsClientJarPath), new File(sealsHomePath));
    }

    /**
     * Constructor
     *
     * @param sealsClientJar  The path to the local SEALS client JAR file.
     * @param sealsHome       SEALS Home directory
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
     * This method runs the specified matcher on the specified track.
     *
     * @param track   Track on which the matcher shall be run.
     * @param matcher The path to the matcher to the matchers that are to be run.
     * @return An {@link ExecutionResultSet} instance.
     */
    public ExecutionResultSet run(Track track, String matcher) {
        return run(track, new File(matcher));
    }


    /**
     * This method runs the specified matcher on the specified track.
     *
     * @param track   Track on which the matcher shall be run.
     * @param matcher The path to the matcher to the matchers that are to be run.
     * @return An {@link ExecutionResultSet} instance.
     */
    public ExecutionResultSet run(Track track, File matcher) {
        ExecutionResultSet resultSet = new ExecutionResultSet();
        if (!matcher.exists()) {
            LOGGER.error("The given matcher path does not exist. Returning null.");
            return null;
        }
        if (matcher.isDirectory()) {
            if(isDirectoryRunnableInSeals(matcher)){
                resultSet.addAll(runIndividualSealsEvaluationTrack(matcher, track));
            } else runMultipleMatchers(track, matcher);
        } else if (matcher.getName().endsWith(".zip")) {
            String unzippedDirectory = unzip(matcher);
            if (unzippedDirectory != null) {
                resultSet.addAll(runIndividualSealsEvaluationTrack(new File(unzippedDirectory), track));
            }
        } else {
            resultSet.addAll(runIndividualSealsEvaluationTrack(matcher, track));
        }
        return resultSet;
    }


    /**
     * This method runs the matchers in a directory on the specified track.
     *
     * @param track            The track on which the matchers shall be run.
     * @param matcherDirectory Directory that contains SEALS matchers in ZIP format or unzipped.
     * @return Execution Result Set instance of the matchers. Note that alignment files will also be available in the matcher directory.
     */
    private ExecutionResultSet runMultipleMatchers(Track track, File matcherDirectory) {
        ExecutionResultSet resultSet = new ExecutionResultSet();
        for (File individualMatcherProject : matcherDirectory.listFiles()) {
            if (individualMatcherProject.isFile() && individualMatcherProject.getName().endsWith(".zip")) {
                String unzippedDirectory = unzip(individualMatcherProject);
                if (unzippedDirectory != null) {
                    resultSet.addAll(runIndividualSealsEvaluationTrack(new File(unzippedDirectory), track));
                }
            } else if (individualMatcherProject.isDirectory()) {
                if (isDirectoryRunnableInSeals(individualMatcherProject)) {
                    resultSet.addAll(runIndividualSealsEvaluationTrack(individualMatcherProject, track));
                }
            }
        }
        return resultSet;
    }


    /**
     * Evaluate a single matcher using the local SEALS client.
     *
     * @param matcherDirectory The directory containing the matcher.
     * @param track            The track on which the matcher shall be run.
     * @return Execution Result Set instance for the matcher on the track.
     */
    private ExecutionResultSet runIndividualSealsEvaluationTrack(File matcherDirectory, Track track) {
        ExecutionResultSet resultSet = new ExecutionResultSet();
        for (TestCase testCase : track.getTestCases()) {
            ExecutionResult result = runIndividualSealsEvaluationTestCase(matcherDirectory, testCase);
            if (result != null) {
                resultSet.add(result);
            }
        }
        return resultSet;
    }


    /**
     * Evaluate a single matcher using the local SEALS client.
     *
     * @param matcherDirectory The directory containing the matcher.
     * @param testCase         The testCase on which the matcher shall be run.
     */
    public ExecutionResult runIndividualSealsEvaluationTestCase(File matcherDirectory, TestCase testCase) {

        // results folder
        File resultsFolder = new File(matcherDirectory.getParent() + "/results_aml_cdmpdm/" + testCase.getName());
        if (!resultsFolder.exists()) resultsFolder.mkdir();

        File systemAlignmentToBeWritten = new File(resultsFolder + "/" + matcherDirectory.getName() + ".rdf");
        File logfileToBeWritten = new File(resultsFolder + "/" + matcherDirectory.getName() + "_log.txt");
        File errorFileToBeWritten = new File(resultsFolder + "/" + matcherDirectory.getName() + "_error.txt");
        systemAlignmentToBeWritten.getParentFile().mkdirs();

        try {
            systemAlignmentToBeWritten.createNewFile();

            List<String> commands = new ArrayList<>();
            commands.add("java");
            if (javaRuntimeParameters != null) commands.addAll(this.javaRuntimeParameters);
            commands.add("-jar");
            commands.add(this.sealsClientJar.getAbsolutePath());
            commands.add(surroundWithDoubleQuotes(matcherDirectory.getCanonicalPath()));
            commands.add("-o");
            commands.add(surroundWithDoubleQuotes(testCase.getSource().toString()));
            commands.add(surroundWithDoubleQuotes(testCase.getTarget().toString()));
            commands.add("-f");
            commands.add(surroundWithDoubleQuotes(systemAlignmentToBeWritten.getCanonicalPath().toString()));
            commands.add("-z");
            ProcessBuilder builder = new ProcessBuilder(commands);

            builder.redirectError(logfileToBeWritten);
            builder.redirectOutput(errorFileToBeWritten);
            builder.directory(this.sealsHome);

            long startTime = java.lang.System.currentTimeMillis();
            Process process = builder.start();

            LOGGER.info("Waiting for completion of matcher " + matcherDirectory.getName() + " on test case " + testCase.getName() + ".");
            LOGGER.info("Current timeout: " + this.timeout + " " + this.timoutTimeUnit.toString());

            try {
                if (process.waitFor(this.timeout, this.timoutTimeUnit)) {
                    LOGGER.info("Matcher finished within timeout.");
                    FileWriter logWriter = new FileWriter(logfileToBeWritten);
                    logWriter.append("MELT: Matcher finished within timeout.");
                    logWriter.flush();
                    logWriter.close();
                } else {
                    LOGGER.warn("Matcher did not finish within the given timeout.");
                    FileWriter errorWriter = new FileWriter(errorFileToBeWritten);
                    errorWriter.append("MELT: Matcher did not finish within the given timeout. Killig matching process.");
                    errorWriter.flush();
                    errorWriter.close();
                }
            } catch (InterruptedException e) {
                LOGGER.error("Interruption while waiting for matcher completion.", e);
            }

            // this is rather a heuristic: reading of ontologies is added, as well as starting of seals
            long matcherRuntime = startTime - java.lang.System.currentTimeMillis();

            LOGGER.info("Evaluation of matcher " + matcherDirectory.getName() + " on test case " + testCase.getName() + " completed.");
            try {
                ExecutionResult result =  new ExecutionResult(testCase, matcherDirectory.getName(), systemAlignmentToBeWritten.toURI().toURL(), matcherRuntime, null);
                result.setMatcherLog(logfileToBeWritten);
                result.setMatcherErrorLog(errorFileToBeWritten);
                return result;
            } catch (MalformedURLException mue) {
                LOGGER.error("Could not transform originalSystemAlignment URI to URL.");
                ExecutionResult result =  new ExecutionResult(testCase, matcherDirectory.getName(), null, matcherRuntime, null);
                result.setMatcherLog(logfileToBeWritten);
                result.setMatcherErrorLog(errorFileToBeWritten);
                return result;
            }
        } catch (IOException ioe) {
            LOGGER.error("IOException occurred while evaluating a matcher.", ioe);
            return null;
        }
    }


    /**
     * Unzips a zip-file in the directory where the zip file resides.
     * A new directory will be created with the name of the zip file (without .zip ending) and the zipped contents
     * will be found therein.
     *
     * @param fileToBeUnzipped The file that shall be unzipped.
     * @return The path to the extracted file.
     */
    private static String unzip(File fileToBeUnzipped) {
        LOGGER.info("Unzipping " + fileToBeUnzipped.getName());
        try {
            String destDir = fileToBeUnzipped.getParentFile().getCanonicalPath() + "/" + fileToBeUnzipped.getName().substring(0, fileToBeUnzipped.getName().length() - 4);
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
            return destDir;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Surrounds the given String with double quotes (").
     *
     * @param toBeSurrounded String that is to be surrounded with double quotes.
     * @return String with pre- and post-appended double quotes.
     */
    public static String surroundWithDoubleQuotes(String toBeSurrounded) {
        return "\"" + toBeSurrounded + "\"";
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
            if(file.isDirectory() && file.getName().equals("bin")){
                containsBin = true;
            }
            if(file.isDirectory() && file.getName().equals("lib")){
                containsLib = true;
            }
            if(file.isDirectory() && file.getName().equals("conf")){
                containsConf = true;
            }
            if(file.isFile() && file.getName().equals("descriptor.xml")){
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
