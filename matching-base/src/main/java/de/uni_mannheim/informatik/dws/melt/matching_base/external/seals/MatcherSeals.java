
package de.uni_mannheim.informatik.dws.melt.matching_base.external.seals;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.MatcherFile;
import de.uni_mannheim.informatik.dws.melt.matching_base.MatchingException;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.cli.process.ExternalProcess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This matcher wraps the SEALS client such that a SEALS zip file or folder can be executed.
 * If multiple matcher should be instantiated, have a look at MatcherSealsBuilder buildFromFolder.
 */
public class MatcherSeals extends MatcherFile {


    private static final String SEALS_DOWNLOAD_URL_VERSION = "7.0.5";

    public static String getSealsDownloadUrlVersion() {
        return SEALS_DOWNLOAD_URL_VERSION;
    }

    private static final String SEALS_DOWNLOAD_URL = "https://github.com/DanFaria/OAEI_SealsClient/releases/download/v" + SEALS_DOWNLOAD_URL_VERSION + "/seals-omt-client.jar";

    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherSeals.class);

    /**
     * The folder which represents one matcher.
     */
    private File matcherFolder;

    /**
     * Path to the JAR of the SEALS client.
     */
    private File sealsClientJar;

    /**
     * Path to a temporary folder. Default is set to the systems tmp.
     * In this folder creates folders will be created and can be removed after execution.
     */
    private File tmpFolder;

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
     * If true, the original matcher folder is untouched and the folder is copied.
     * Some matchers require this, because the do not close some resources.
     */
    private boolean freshMatcherInstance;

    /**
     * If true, the input alignment is not passed to SEALS even if one is provided.
     */
    private boolean doNotUseInputAlignment;

    /**
     * The command to start java in the terminal. Typically, this is "java"
     * Seals needs java version 1.8
     */
    private String javaCommand;

    /**
     * Constructor with all parameters.
     * IMPORTANT: the sealsHome folder will be deleted (do not use any folder which contains content). USE IT WITH CARE.
     * The Seals client will be downloaded on demand, if necessary.
     *
     * @param matcherFileOrFolder    The file (zip file) or folder which represents one matcher.
     * @param sealsClientJar         The path to the local SEALS client JAR file.
     * @param tmpFolder              folder to store the matcher temporary.
     * @param timeout                Timeout for one testcase as long.
     * @param timeoutTimeUnit        The unit of the timeout.
     * @param javaRuntimeParameters  Runtime parameters such as ("-Xmx25g", "-Xms15g").
     * @param freshMatcherInstance   If true, the original matcher folder is untouched and the folder is copied
     * @param doNotUseInputAlignment If true, the input alignment is not passed to SEALS even if one is provided.
     * @param javaCommand            the java 1.8 command on the system. usually it is just java
     */
    public MatcherSeals(File matcherFileOrFolder, File sealsClientJar, File tmpFolder, long timeout, TimeUnit timeoutTimeUnit,
                        List<String> javaRuntimeParameters, boolean freshMatcherInstance, boolean doNotUseInputAlignment,
                        String javaCommand) {
        this.matcherFolder = prepareMatcherFolder(tmpFolder, matcherFileOrFolder);
        this.sealsClientJar = sealsClientJar;
        this.tmpFolder = tmpFolder;
        this.timeout = timeout;
        this.timeoutTimeUnit = timeoutTimeUnit;
        this.javaRuntimeParameters = javaRuntimeParameters;
        this.freshMatcherInstance = freshMatcherInstance;
        this.doNotUseInputAlignment = doNotUseInputAlignment;
        this.javaCommand = javaCommand;

        downloadSealsIfNecessary(this.sealsClientJar);
        if (!this.tmpFolder.mkdirs() && !this.tmpFolder.isDirectory()) {
            LOGGER.error("Directory for tmp {} cannot be created.", this.tmpFolder);
        }
    }

    public MatcherSeals(File matcherFileOrFolder, File sealsClientJar, long timeout, TimeUnit timeoutTimeUnit, List<String> javaRuntimeParameters, boolean freshMatcherInstance, boolean doNotUseInputAlignment, String javaCommand) {
        this(matcherFileOrFolder, sealsClientJar, FileUtil.SYSTEM_TMP_FOLDER, timeout, timeoutTimeUnit, javaRuntimeParameters, freshMatcherInstance, doNotUseInputAlignment, javaCommand);
    }

    /**
     * Constructor with reasonable defaults like 12 hours timeout, no runtime parameter, java as the java command and
     * no new matcher instance every time.
     *
     * @param matcherFileOrFolder The file (zip file) or folder which represents one matcher.
     * @param sealsClientJar      the seals client jar
     */
    public MatcherSeals(File matcherFileOrFolder, File sealsClientJar) {
        this(matcherFileOrFolder, sealsClientJar, 12, TimeUnit.HOURS, new ArrayList<>(), false, false, "java");
    }

    /**
     * The SEALS client will be search in the systems tmp directory with the name seals-omt-client-v7.0.5.jar.
     *
     * @param matcherFileOrFolder The file (zip file) or folder which represents one matcher.
     */
    public MatcherSeals(File matcherFileOrFolder) {
        this(matcherFileOrFolder, new File(FileUtil.SYSTEM_TMP_FOLDER, "seals-omt-client-v" + SEALS_DOWNLOAD_URL_VERSION + ".jar"));
    }

    /**
     * The SEALS client will be search in the systems tmp directory with the name seals-omt-client-v7.0.5.jar.
     *
     * @param matcherFileOrFolderPath The file (zip file) or folder which represents one matcher.
     */
    public MatcherSeals(String matcherFileOrFolderPath) {
        this(new File(matcherFileOrFolderPath), new File(FileUtil.SYSTEM_TMP_FOLDER,
                "seals-omt-client-v" + SEALS_DOWNLOAD_URL_VERSION + ".jar"));
    }

    private static void downloadSealsIfNecessary(File sealsClientJar) {
        //download seals to given location if it does not exist
        if (sealsClientJar.exists() == false) {
            LOGGER.info("Download SEALS client because file {} does not exist.", sealsClientJar);
            File parent = sealsClientJar.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    LOGGER.error("Directory {} can not be created", parent);
                    return;
                }
            }
            try (InputStream source = new URL(SEALS_DOWNLOAD_URL).openStream();
                 FileOutputStream output = new FileOutputStream(sealsClientJar)) {
                byte[] buffer = new byte[4096];
                int n = 0;
                while (-1 != (n = source.read(buffer))) {
                    output.write(buffer, 0, n);
                }
            } catch (IOException ex) {
                LOGGER.warn("Could not download SEALS client.", ex);
            }
        }
    }

    private static File prepareMatcherFolder(File tmpDirectory, File matcherFileOrFolder) {
        if (matcherFileOrFolder.exists() == false) {
            LOGGER.error("matcherFileOrFolder does not exist. MatcherSeals is not usable.");
            return null;
        }
        File matcherFolderTmp = matcherFileOrFolder;
        if (matcherFileOrFolder.isFile()) {
            if (matcherFileOrFolder.getName().endsWith(".zip")) {
                //unzip it to tmp folder
                //make folder for unzipDirectory
                File unzipFolder = FileUtil.createFolderWithRandomNumberInDirectory(tmpDirectory, "meltUnzip");
                unzipFolder.deleteOnExit();
                LOGGER.info("Unzip seals matcher {} now.", matcherFileOrFolder.getName());
                unzipToDirectory(unzipFolder, matcherFileOrFolder, true);
                matcherFolderTmp = unzipFolder;
            } else {
                LOGGER.error("matcher is a file which does not end with \".zip\". MatcherSeals is not usable.");
                return null;
            }
        }

        if (isDirectoryRunnableInSeals(matcherFolderTmp)) {
            return matcherFolderTmp;
        } else {
            LOGGER.warn("Folder is not runnable in SEALS. Search in subDirectories of {}", matcherFolderTmp);
            File firstRunnableFolder = getFirstSubDirectoryRunnableInSeals(matcherFolderTmp);
            if (firstRunnableFolder == null) {
                LOGGER.warn("Did not find any subfolder runnable in SEALS.");
                return null;
            } else {
                LOGGER.warn("Found a subfolder runnable in SEALS which is now used: {}", firstRunnableFolder);
                return firstRunnableFolder;
            }
        }
    }

    //https://stackoverflow.com/questions/29001162/how-to-get-the-value-which-i-sent-via-system-out-println
    //to store the log output of a matcher

    @Override
    public void match(URL source, URL target, URL inputAlignment, File alignmentResult) throws Exception {
        if (this.matcherFolder == null) {
            throw new Exception("Matcher folder is null. See error messages above.");
        }
        File currentInstance;
        if (this.freshMatcherInstance) {
            //copy folder to tmp directory
            currentInstance = FileUtil.createFolderWithRandomNumberInDirectory(this.tmpFolder, "meltFreshInstance");
            LOGGER.info("Copy matcher {} to new directory {} because a fresh instance for each new matching task is requested.", this.matcherFolder.getName(), currentInstance.getName());
            copyDirectory(currentInstance, this.matcherFolder);
        } else {
            currentInstance = this.matcherFolder;
        }
        File sealsHome = FileUtil.createFolderWithRandomNumberInDirectory(this.tmpFolder, "meltSealsHome");
        sealsHome.mkdirs();
        try {
            ExternalProcess sealsProcess = new ExternalProcess();
            sealsProcess.addStdErrConsumer(l -> LOGGER.info("ExternalSEALS(Err): {}", l));
            sealsProcess.addStdOutConsumer(l -> LOGGER.info("ExternalSEALS(Out): {}", l));

            sealsProcess.addArgument(this.javaCommand);
            if (this.javaRuntimeParameters != null) sealsProcess.addArguments(this.javaRuntimeParameters);

            sealsProcess.addArguments("-jar", this.sealsClientJar.getAbsolutePath(), currentInstance.getAbsolutePath());
            if (inputAlignment == null || this.doNotUseInputAlignment) {
                sealsProcess.addArguments("-o", source.toString(), target.toString());
            } else {
                sealsProcess.addArguments("-oi", source.toString(), target.toString(), inputAlignment.toString());
            }
            sealsProcess.addArguments("-f", alignmentResult.getAbsolutePath(), "-z");

            sealsProcess.setWorkingDirectory(sealsHome);
            sealsProcess.addEnvironmentVariable("SEALS_HOME", FileUtil.getCanonicalPathIfPossible(sealsHome));
            sealsProcess.setTimeout(this.timeout, this.timeoutTimeUnit);
            sealsProcess.run();

            if (alignmentResult.length() == 0) { //easy check if file is empty
                LOGGER.error("The result of seals matcher is an empty file. This typically happens when the matcher " +
                        "throws an error (see log messages of ExternalSEALS)." +
                        "In some cases this happens because the matcher does not expect an input alignment. " +
                        "You can solve this by setting  doNotUseInputAlignment to true in MatcherSeals.");
                throw new MatchingException("The result of seals matcher is an empty file.");
            }
        } finally {
            if (this.freshMatcherInstance) {
                //delete fresh instance (dont follow sym links)
                try (Stream<Path> walk = Files.walk(currentInstance.toPath())) {
                    walk.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            }
            //delete seals home:
            try (Stream<Path> walk = Files.walk(sealsHome.toPath())) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }

    /**
     * This method visits all folders below the starting directory and returns the first directory which is runnable in seals.
     * The traversing strategy is breadth first search.
     *
     * @param rootDir Path to the starting directory.
     * @return The first directory which is runnable in seals or null if such a folder does not exist
     */
    public static File getFirstSubDirectoryRunnableInSeals(File rootDir) {
        if (rootDir == null || rootDir.exists() == false)
            return null;
        Queue<File> queue = new LinkedList<>();
        queue.add(rootDir);
        while (!queue.isEmpty()) {
            File current = queue.poll();
            if (isDirectoryRunnableInSeals(current)) {
                return current;
            }
            File[] listOfDirectories = current.listFiles(File::isDirectory);
            if (listOfDirectories != null)
                queue.addAll(Arrays.asList(listOfDirectories));
        }
        return null;
    }

    private static void copyDirectory(File targetDirectory, File sourceDirectory) {
        if (!sourceDirectory.exists()) {
            LOGGER.warn("Source directory for copy does not exist. Do nothing.");
            return;
        }
        Path sourcePath = sourceDirectory.toPath();
        Path targetPath = targetDirectory.toPath();
        try {
            Files.walk(sourcePath).forEach(
                    source -> {
                        try {
                            Files.copy(source, targetPath.resolve(sourcePath.relativize(source)));
                        } catch (IOException e) {
                            LOGGER.error("Could not copy some files from matcher: {}", source);
                        }
                    }
            );
        } catch (IOException ex) {
            LOGGER.error("Could not copy seals directory.", ex);
        }
    }

    private static void unzipToDirectory(File targetDirectory, File sourceFile, boolean deleteOnExit) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceFile))) {
            String destDirPath = targetDirectory.getCanonicalPath();
            byte[] buffer = new byte[1024];
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                File newFile = new File(targetDirectory, ze.getName());
                //check for vulnerability called Zip Slip (unzipped somewhere outside target folder)
                if (!newFile.getCanonicalPath().startsWith(destDirPath + File.separator)) {
                    throw new IOException("Entry is outside of the target dir: " + ze.getName());
                }
                if (deleteOnExit)
                    newFile.deleteOnExit();
                if (ze.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    //create directories for sub directories in zip
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("Cannot unzip matcher directory", ex);
        }
    }

    /**
     * Determines whether the specified directory is runnable in seals.
     *
     * @param directory Path to the directory.
     * @return True if runnable, else false.
     */
    public static boolean isDirectoryRunnableInSeals(File directory) {
        if (directory.isFile()) return false;
        boolean containsBin = false;
        boolean containsLib = false;
        boolean containsConf = false;
        boolean containsDescriptor = false;

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                String name = file.getName();
                switch (name) {
                    case "bin":
                        containsBin = true;
                        break;
                    case "lib":
                        containsLib = true;
                        break;
                    case "conf":
                        containsConf = true;
                        break;
                }
            } else if (file.isFile() && file.getName().equals("descriptor.xml")) {
                containsDescriptor = true;
            }
        }
        return containsBin && containsLib && containsConf && containsDescriptor;
    }


    public String getName() {
        return getMatcherNameFromSealsDescriptor(this.matcherFolder);
    }

    public File getMatcherFolder() {
        return matcherFolder;
    }

    /**
     * Regex pattern to get the matcher name from the SEALS {@code descriptor.xml} file.
     */
    private static final Pattern matcherNamePattern = Pattern.compile("<ns:package.*?id=\"(.*?)\"", Pattern.DOTALL);

    /**
     * Returns the matcher name in the seals descriptor.
     *
     * @param file File instance which points to a descriptor file or a matcher directory.
     * @return name of the matcher or MatcherSeals-randomNumber if not available.
     */
    public static String getMatcherNameFromSealsDescriptor(File file) {
        File descriptorFile = null;
        if (file.isFile() && file.getName().equals("descriptor.xml")) {
            descriptorFile = file;
        } else if (file.isDirectory()) {
            descriptorFile = new File(file, "descriptor.xml");
        } else {
            LOGGER.info("Can not retrieve matcher name because given parameter is not a directory or a descriptor file.");
            return getDefaultMatcherName();
        }

        if (descriptorFile.exists() == false) {
            LOGGER.info("Can not retrieve matcher name because descriptor file does not exist.");
            return getDefaultMatcherName();
        }
        String text = "";
        try {
            text = new String(Files.readAllBytes(descriptorFile.toPath()), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.info("Can not retrieve matcher name because descriptor file can not be read.", ex);
            return getDefaultMatcherName();
        }

        Matcher regex = matcherNamePattern.matcher(text);
        if (regex.find() == false)
            return getDefaultMatcherName();
        return regex.group(1);
    }

    private static String getDefaultMatcherName() {
        return "MatcherSeals-" + FileUtil.getRandomPositiveNumber();
    }

    public File getSealsClientJar() {
        return sealsClientJar;
    }

    public void setSealsClientJar(File sealsClientJar) {
        this.sealsClientJar = sealsClientJar;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public void setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
    }

    public String getTimeoutAsText() {
        return this.timeout + " " + this.timeoutTimeUnit.toString().toLowerCase();
    }
}
