package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a track from OAEI like anatomy, conference or multifarm etc.
 * For a list of possible tracks, have a look at the TrackRepository.
 * Example:
 * <pre>
 * {@code
 * TrackRepository.Anatomy.Default.getTestCases()
 * }
 * </pre>
 * A track consists of multiple {@link TestCase}.
 * @author Sven Hertling
 */
public abstract class Track {
    private static final Logger LOGGER = LoggerFactory.getLogger(Track.class);
    
    static {
        //TODO: check whats better because a global store is maybe preferable, but does the user know where to look?
        setCacheFolder(new File(System.getProperty("user.home"), "oaei_track_cache"));
        //setCacheFolder(new File("trackCache"));
        setSkipTestCasesWithoutRefAlign(true);
    }
    
    protected static File cacheFolder;    
    protected static boolean skipTestsWithoutRefAlign;
    
    protected String remoteLocation;
    protected String name;
    protected String version;
    
    protected List<TestCase> testCases;//initialized lazily
    
    protected boolean useDuplicateFreeStorageLayout;
    
    /**
     * Constructor
     * @param remoteLocation The remote location.
     * @param name The test case name.
     * @param version The test case version.
     */
    protected Track(String remoteLocation, String name, String version){
        this(remoteLocation, name, version, false);
    }
    
    protected Track(String remoteLocation, String name, String version, boolean useDuplicateFreeStorageLayout){
        this.remoteLocation = remoteLocation;
        this.name = name;
        this.version = version;
        this.useDuplicateFreeStorageLayout = useDuplicateFreeStorageLayout;
        this.testCases = null;//initialized lazily
    }

    /**
     * Folder where the tracks and the corresponding test cases shall be cached.
     * @param directory Target directory.
     */
    public static void setCacheFolder(File directory){
        if (directory == null) {
            throw new IllegalArgumentException("CacheFolder in class Track should not be null.");
        }
        if(!directory.exists()) {
            directory.mkdirs();
        }
        if(directory.isDirectory() == false){
            throw new IllegalArgumentException("CacheFolder should be a directory.");
        }
        cacheFolder = directory;
    }
    
    public static void setSkipTestCasesWithoutRefAlign(boolean skip){
        skipTestsWithoutRefAlign = skip;
    }

    public String getRemoteLocation() {
        return remoteLocation;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Return all test cases of the track.
     * @return A list of {@link TestCase} instances belonging to this track.
     */
    public List<TestCase> getTestCases(){
        if(testCases == null){
            testCases = readFromCache();
            if (testCases.size() > 0) {
                return testCases;
            }
            try {
                downloadToCache();
            } catch (Exception ex) {
                LOGGER.error("Couldn't download test cases and store them in cache folder", ex);
            }
            testCases = readFromCache();
        }
        return testCases;
    }

    /**
     * Obtain a test case using a specified name.
     * @param name Name of the test case.
     * @return Test case.
     */
    public TestCase getTestCase(String name){
        for(TestCase c : getTestCases()){
            if(c.getName().equals(name)){
                return c;
            }
        }
        return null;
    }
    
    protected List<TestCase> readFromCache(){ return readFromDefaultLayout(); } // can be overwritten if download does not use default layout

    protected abstract void downloadToCache() throws Exception;
    
    /**
     * Writes an input stream to a file.
     * Developer remark: Does not close the input stream.
     * @param inputStream Input stream to write.
     * @param file File to write input stream into.
     */
    protected void saveToFile(InputStream inputStream, File file){
        file.getParentFile().mkdirs();

        ReadableByteChannel rbc = Channels.newChannel(inputStream);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException ex) {
            LOGGER.error("Cannot write inputstream to file.", ex);
        }
    }
    
    protected String encode(String s){
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Cannot encode location, name and version of track.", ex);
            return s;
        }
    }
    
    protected void saveInTestCaseLayout(InputStream in, String testCaseName, TestCaseType type){
        File file = Paths.get(
                    cacheFolder.getAbsolutePath(),
                    encode(this.remoteLocation),
                    encode(this.name),
                    encode(this.version),
                    encode(testCaseName),
                    type.toString() + ".rdf").toFile();
        saveToFile(in, file);
    }
    
    protected void saveInDuplicateFreeLayout(InputStream in, String testCaseName, TestCaseType type){
        if(type == TestCaseType.SOURCE || type == TestCaseType.TARGET){
            String[] testCaseSplit = testCaseName.split("-");
            String ontologyName = type == TestCaseType.SOURCE ? testCaseSplit[0] : testCaseSplit[1];
            File file = Paths.get(
                    cacheFolder.getAbsolutePath(),
                    encode(this.remoteLocation),
                    encode(this.name),
                    encode(this.version),
                    "ontologies",
                    ontologyName + ".rdf").toFile();
            if(file.exists() == false)
                saveToFile(in, file);
            
        }else if(type == TestCaseType.REFERENCE){
            File file = Paths.get(
                    cacheFolder.getAbsolutePath(),
                    encode(this.remoteLocation),
                    encode(this.name),
                    encode(this.version),
                    "references",
                    testCaseName + ".rdf").toFile();
            if(file.exists() == false)
                saveToFile(in, file);
        }
    }

    
    protected void saveInDefaultLayout(InputStream in, String testCaseName, TestCaseType type){
        if(this.useDuplicateFreeStorageLayout){
            saveInDuplicateFreeLayout(in, testCaseName, type);
        }else{
            saveInTestCaseLayout(in, testCaseName, type);
        }
    }
    
    protected void saveInDefaultLayout(URL url, String testCaseName, TestCaseType type){
        try (InputStream is = url.openStream()){
            saveInDefaultLayout(is, testCaseName, type);
        } catch (IOException ex) {
            LOGGER.error("Cannot download from inputstream.", ex);
        }
    }
    
    protected void saveInDefaultLayout(File f, String testCaseName, TestCaseType type){
        try (InputStream is = new FileInputStream(f)){
            saveInDefaultLayout(is, testCaseName, type);
        } catch (IOException ex) {
            LOGGER.error("File to copy not found", ex);
        }
    }


    /**
     * Reads and parses test cases.
     * Test Case Layout: Test is a directory with source.rdf, target.rdf, reference.rdf.
     *
     * @return Parsed TestCase list.
     */
    protected List<TestCase> readFromTestCaseLayout(){
        List<TestCase> testCases = new ArrayList<>();        
        File file = Paths.get(
                    cacheFolder.getAbsolutePath(),
                    encode(this.remoteLocation),
                    encode(this.name),
                    encode(this.version)).toFile();
        
        File[] files = file.listFiles();
        if(files == null)
            return testCases;
        
        for(File f : files){
            File source_file = new File(f, TestCaseType.SOURCE.toFileName());
            File target_file = new File(f, TestCaseType.TARGET.toFileName());
            File reference_file = new File(f, TestCaseType.REFERENCE.toFileName());
            
            if(source_file.exists() == false || target_file.exists() == false){
                LOGGER.error("Cache is corrupted - source or target file is not there - continue (to solve it, delete the cache folder)");
                continue;
            }
            
            if(reference_file.exists() == false && skipTestsWithoutRefAlign)
                continue;
            
            testCases.add(new TestCase(
                    f.getName(),
                    source_file.toURI(),
                    target_file.toURI(),
                    reference_file.exists() ? reference_file.toURI() : null, this));
        }
        return testCases;
    }

    /**
     * Reads and parses test cases.
     * Duplicate Free Layout:
     * - Folder references holding reference files with style source_name-target_name.rdf.
     * - Folder ontologies holding the ontologies whose names were specified in the reference file names. Ontologies
     * end with ".rdf".
     *
     * @return Parsed TestCase list.
     */
    protected List<TestCase> readFromDuplicateFreeLayout(){
        File referenceFolder = Paths.get(
                    cacheFolder.getAbsolutePath(),
                    encode(this.remoteLocation),
                    encode(this.name),
                    encode(this.version),
                    "references").toFile();
        
        File[] referenceFiles = referenceFolder.listFiles();
        if(referenceFiles == null || referenceFiles.length == 0){
            return new ArrayList<>();
        }
        if(skipTestsWithoutRefAlign == false)
            LOGGER.warn("Using DuplicateFreeLayout for storing the testcases. returning testcase without reference alignment is not possible here.");
        
        List<TestCase> testCases = new ArrayList<>(); 
        for(File referenceFile : referenceFiles){
            String fileNameWithoutExtension = FilenameUtils.removeExtension(referenceFile.getName());
            String[] sourceTargetName = fileNameWithoutExtension.split("-");
            if(sourceTargetName.length != 2){
                LOGGER.error("A file in references folder of track cache has more splits than expected.");
                continue;
            }
            File sourceFile = Paths.get(cacheFolder.getAbsolutePath(),
                    encode(this.remoteLocation),encode(this.name), encode(this.version),
                    "ontologies", sourceTargetName[0] + ".rdf").toFile();
            File targetFile = Paths.get(cacheFolder.getAbsolutePath(),
                    encode(this.remoteLocation),encode(this.name), encode(this.version),
                    "ontologies", sourceTargetName[1] + ".rdf").toFile();
            
            if(sourceFile.exists() == false || targetFile.exists() == false || referenceFile.exists() == false){
                LOGGER.error("Cache is corrupted - source, target or reference file is not there - continue (to solve it, delete the cache folder)");
                continue;
            }
            testCases.add(new TestCase(fileNameWithoutExtension,
                    sourceFile.toURI(),
                    targetFile.toURI(),
                    referenceFile.toURI(),
                    this));
        }
        return testCases;
    }
    
    private List<TestCase> readFromDefaultLayout(){
        if(this.useDuplicateFreeStorageLayout){
            return readFromDuplicateFreeLayout();
        }else{
            return readFromTestCaseLayout();
        }
    }
    
            
    
    protected static String getNiceRemoteLocation(String url){
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException ex) {
            return url;
        }
    }
    

    @Override
    public String toString() {
        return "Track " + name;
    }

    
    //override equals and hashcode: track is equal when name is equal

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.remoteLocation);
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.version);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Track other = (Track) obj;
        if (!Objects.equals(this.remoteLocation, other.remoteLocation)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        return true;
    }
}
