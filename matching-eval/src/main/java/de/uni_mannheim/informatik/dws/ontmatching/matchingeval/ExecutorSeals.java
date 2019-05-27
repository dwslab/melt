package de.uni_mannheim.informatik.dws.ontmatching.matchingeval;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.LocalTrack;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
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
     * Constructor
     * @param sealsClientJar The path to the local SEALS client JAR file.
     */
    public ExecutorSeals(File sealsClientJar, File sealsHome){
        this.sealsClientJar = sealsClientJar;
        this.sealsHome = sealsHome;
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
            runMultipleMatchers(track, matcher);
        } else if (matcher.getName().endsWith(".zip")) {
            String unzippedDirectory = unzip(matcher);
            if(unzippedDirectory != null){
                resultSet.addAll(runIndividualSealsEvaluationTrack(new File(unzippedDirectory), track));
            }
        } else {
            resultSet.addAll(runIndividualSealsEvaluationTrack(matcher, track));
        }
        return resultSet;
    }

    public static void main(String[] args) {
        ExecutorSeals executorSeals = new ExecutorSeals(new File("C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\SEALS\\SEALS_JAR\\seals-omt-client.jar"), new File("C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\SEALS\\SEALS_HOME"));
        executorSeals.run(
                new LocalTrack("sap_fsdp", "0.1",new File("C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\SAP_Mappings\\track4semantics")),
                new File("C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\OAEI\\2018\\Matching_Systems\\second_test"));
    }


    /**
     * This method runs the matchers in a directory on the specified track.
     * @param track The track on which the matchers shall be run.
     * @param matcherDirectory Directory that contains SEALS matchers in ZIP format or unzipped.
     * @return Execution Result Set instance of the matchers. Note that alignment files will also be available in the matcher directory.
     */
    private ExecutionResultSet runMultipleMatchers(Track track, File matcherDirectory) {
        ExecutionResultSet resultSet = new ExecutionResultSet();
        for (File individualMatcherProject : matcherDirectory.listFiles()) {
            if (individualMatcherProject.isFile() && individualMatcherProject.getName().endsWith(".zip")) {
                String unzippedDirectory = unzip(individualMatcherProject);
                if(unzippedDirectory != null){
                    resultSet.addAll(runIndividualSealsEvaluationTrack(new File(unzippedDirectory), track));
                }
            } else if (individualMatcherProject.isDirectory()){
                boolean binFound = false; // just a quick check whether the project represents SEALS structure (heuristic)
                for(File f : individualMatcherProject.listFiles()){
                    if(f.isDirectory() && f.getName().equals("bin")){
                        binFound = true;
                    }
                }
                if(binFound) {
                    resultSet.addAll(runIndividualSealsEvaluationTrack(individualMatcherProject, track));
                }
            }
        }
        return resultSet;
    }


    /**
     * Evaluate a single matcher using the local SEALS client.
     * @param matcherDirectory The directory containing the matcher.
     * @param track The track on which the matcher shall be run.
     * @return Execution Result Set instance for the matcher on the track.
     */
    private ExecutionResultSet runIndividualSealsEvaluationTrack(File matcherDirectory, Track track){
        ExecutionResultSet resultSet = new ExecutionResultSet();
        for(TestCase testCase : track.getTestCases()) {
            ExecutionResult result = runIndividualSealsEvaluationTestCase(matcherDirectory, testCase);
            if(result!= null){
                resultSet.add(result);
            }
        }
        return resultSet;
    }

    /**
     * Evaluate a single matcher using the local SEALS client.
     * @param matcherDirectory The directory containing the matcher.
     * @param testCase The testCase on which the matcher shall be run.
     */
    private ExecutionResult runIndividualSealsEvaluationTestCase(File matcherDirectory, TestCase testCase) {

        // results folder
        File resultsFolder = new File(matcherDirectory.getParent() + "/results/" + testCase.getName());
        if(!resultsFolder.exists()) resultsFolder.mkdir();

        File systemAlignmentToBeWritten = new File(resultsFolder + "/" + matcherDirectory.getName() + ".rdf");
        File logfileToBeWritten = new File(resultsFolder + "/" + matcherDirectory.getName() + "_log.txt");
        File errorFileToBeWritten = new File(resultsFolder + "/" + matcherDirectory.getName() + "_error.txt");
        systemAlignmentToBeWritten.getParentFile().mkdirs();

        try {
            systemAlignmentToBeWritten.createNewFile();
            long startTime = java.lang.System.currentTimeMillis();
            ProcessBuilder builder = new ProcessBuilder(
                    "java",
                    "-jar",
                    this.sealsClientJar.getAbsolutePath(),
                    surroundWithDoubleQuotes(matcherDirectory.getCanonicalPath()),
                    "-o",
                    surroundWithDoubleQuotes(testCase.getSource().toString()),
                    surroundWithDoubleQuotes(testCase.getTarget().toString()),
                    "-f",
                    surroundWithDoubleQuotes(systemAlignmentToBeWritten.getCanonicalPath().toString()),
                    "-z"
            );
            builder.redirectError(logfileToBeWritten);
            builder.redirectOutput(errorFileToBeWritten);
            builder.directory(this.sealsHome);
            Process process = builder.start();
            int nextMessage = 0;
            while (process.isAlive()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    nextMessage++;
                } catch (InterruptedException e) {
                    LOGGER.error("Interruption while waiting for matcher completion.");
                    e.printStackTrace();
                }
                if(nextMessage % 60 == 0) {
                    System.out.println("Waiting for completion of matcher " + matcherDirectory.getName() + " on test case " + testCase.getName() + ".");
                }
            }

            // this is rather a heuristic: reading of ontologies is added, as well as starting of seals
            long matcherRuntime = startTime - java.lang.System.currentTimeMillis();

            LOGGER.info("Evaluation of matcher " + matcherDirectory.getName() + " on test case " + testCase.getName() + " completed.");
            try {
                return new ExecutionResult(testCase, matcherDirectory.getName(), systemAlignmentToBeWritten.toURI().toURL(), matcherRuntime, null);
            } catch (MalformedURLException mue){
                LOGGER.error("Could not transform originalSystemAlignment URI to URL.");
                return new ExecutionResult(testCase, matcherDirectory.getName(), null, matcherRuntime, null);
            }
        } catch (IOException ioe){
            LOGGER.error("IOException occurred while evaluating a matcher.");
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * Unzips a zip-file in the directory where the zip file resides.
     * A new directory will be created with the name of the zip file (without .zip ending) and the zipped contents
     * will be found therein.
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
                if(ze.isDirectory()){
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

    private static void unzipFile(File fileToBeUnzipped) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(fileToBeUnzipped));
            ZipEntry zipEntry = zis.getNextEntry();
            File destDir = new File(fileToBeUnzipped.getParentFile().getCanonicalPath() + "/" + fileToBeUnzipped.getName().substring(0, fileToBeUnzipped.getName().length() - 4));
            destDir.mkdir();
            byte[] buffer = new byte[1024];
            while (zipEntry != null) {
                File newFile = new File(destDir.getCanonicalPath() + "/" + zipEntry);
                if (zipEntry.isDirectory()) {
                    newFile.mkdir();
                    continue;
                }
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error("Could not write file while unzipping.");
            e.printStackTrace();
        }
    }

    /**
     * Surrounds the given String with double quotes (").
     * @param toBeSurrounded String that is to be surrounded with double quotes.
     * @return String with pre- and post-appended double quotes.
     */
    public static String surroundWithDoubleQuotes(String toBeSurrounded){
        return "\"" + toBeSurrounded + "\"";
    }

}
