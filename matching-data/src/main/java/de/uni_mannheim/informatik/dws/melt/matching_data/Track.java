package de.uni_mannheim.informatik.dws.melt.matching_data;

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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
 * @author Jan Portisch
 */
public abstract class Track {

    /**
     * Default Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Track.class);
    
    static {
        File cacheFolder = new File(System.getProperty("user.home"), "oaei_track_cache");
        setCacheFolder(cacheFolder);
    }

    /**
     * The folder where the individual tracks that were downloaded will be cached so that they can be reused
     * across multiple evaluation session.
     */
    protected static File cacheFolder;

    protected boolean skipTestsWithoutRefAlign;
    
    protected String remoteLocation;
    protected String name;
    protected String version;
    
    protected List<TestCase> testCases; //initialized lazily
    
    /**
     * If true, store and load testcases in a folder structure where ontologies are only stored once and not copied for every testcase.
     * layout:
     *   - ontologies
     *     - ont1.rdf
     *     - ont2.rdf
     *   - references
     *     - ont1-ont2.rdf
     */
    protected boolean useDuplicateFreeStorageLayout;
    
    /**
     * Completeness of the gold standard for all test cases.
     */
    protected GoldStandardCompleteness goldStandardCompleteness;
    
    protected Track(String remoteLocation, String name, String version, boolean useDuplicateFreeStorageLayout, GoldStandardCompleteness goldStandardCompleteness, boolean skipTestsWithoutRefAlign){
        this.remoteLocation = remoteLocation;
        this.name = name;
        this.version = version;
        this.useDuplicateFreeStorageLayout = useDuplicateFreeStorageLayout;
        this.goldStandardCompleteness = goldStandardCompleteness;
        this.testCases = null; //initialized lazily
        this.skipTestsWithoutRefAlign = skipTestsWithoutRefAlign;
    }
    
    protected Track(String remoteLocation, String name, String version, boolean useDuplicateFreeStorageLayout, GoldStandardCompleteness goldStandardCompleteness){
        this(remoteLocation, name, version, useDuplicateFreeStorageLayout, goldStandardCompleteness, true);
    }
    
    protected Track(String remoteLocation, String name, String version, boolean useDuplicateFreeStorageLayout){
        this(remoteLocation, name, version, useDuplicateFreeStorageLayout, GoldStandardCompleteness.COMPLETE);
    }
    
    /**
     * Constructor
     * @param remoteLocation The remote location.
     * @param name The test case name.
     * @param version The test case version.
     */
    protected Track(String remoteLocation, String name, String version){
        this(remoteLocation, name, version, false);
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
        try {
            LOGGER.info("Track cache folder is: " + cacheFolder.getCanonicalPath());
        } catch (IOException e) {
            LOGGER.error("Could not get canonical file path of cache folder. Program will resume normally.", e);
        }
    }
    
    public void setSkipTestCasesWithoutRefAlign(boolean skip){
        this.skipTestsWithoutRefAlign = skip;
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
        testCases.sort((TestCase o1, TestCase o2) -> o1.getName().compareTo(o2.getName()));
        return testCases;
    }
    
    /**
     * Obtain multiple test cases using specified test case names.
     * If name does not exist, a log is written and the testcase is skipped.
     * @param names Names of the test cases.
     * @return List of testcases.
     */
    public List<TestCase> getTestCases(String... names){
        return getTestCases(Arrays.asList(names));
    }
    
    /**
     * Obtain multiple test cases using specified test case names.
     * If name does not exist, a log is written and the testcase is skipped.
     * @param testCaseNames Names of the test cases.
     * @return List of testcases.
     */
    public List<TestCase> getTestCases(List<String> testCaseNames){
        Map<String, TestCase> map = getMapNameToTestCase();        
        List<TestCase> tcs = new ArrayList<>(testCaseNames.size());
        for(String testCaseName : testCaseNames){
            TestCase tc = map.get(testCaseName);
            if(tc == null){
                LOGGER.warn("Did not find testCase with name {}", testCaseName);
                continue;
            }
            tcs.add(tc);
        }
        return tcs;
    }
    
    private Map<String, TestCase> getMapNameToTestCase(){
        Map<String, TestCase> map = new HashMap<>();
        for(TestCase testCase : getTestCases()){
            map.put(testCase.getName(), testCase);
        }
        return map;
    }

    /**
     * Obtain a test case using a specified name.
     * If name does not exist, null is returned.
     * @param name Name of the test case.
     * @return Test case or null if not existent.
     */
    public TestCase getTestCase(String name){
        for(TestCase c : getTestCases()){
            if(c.getName().equals(name)){
                return c;
            }
        }
        LOGGER.warn("Did not find testCase with name {}", name);
        return null;
    }
    
    /**
     * Obtain a test case using the index.
     * @param index The position of the test case that is to be obtained.
     * @return Test case.
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public TestCase getTestCase(int index){
        return getTestCases().get(index);
    }
    
    /**
     * Obtain an example test case (the first one).
     * throws NoSuchElementException if there are no test cases.
     * @return Test case.
     */
    public TestCase getFirstTestCase(){
        return getTestCases().iterator().next();
    }
    
    /**
     * Returns a string containing name and version of track in url encoded fashion.
     * @return String as name and version of track (url encoded)
     */
    public String getNameAndVersionString(){
        try {
            return URLEncoder.encode(this.getName(), "UTF-8") + "_" + URLEncoder.encode(this.getVersion(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.warn("Could not generate encoded name and version string of track.", ex);
            return this.getName() + "_" + this.getVersion();
        }
    }
    
    /**
     * This function returns the distinct ontologies for the whole track.This means if you have testcases like A-B, B-C and A-C, then it would return the ontologies A,B,and C.<br>
     * IMPORTANT: this only works if the testcase name consists of the source and target names separated by "-".
     * This is the case for conference and KG track and maybe some others.
     * @return list of URLs which points to the ontologies.
     */
    public List<URL> getDistinctOntologies(){
        return getDistinctOntologies(getTestCases());
    }
    
    public static List<URL> getDistinctOntologies(List<TestCase> testCases){
        List<URL> distinctOntologies = new ArrayList<>();
        Set<String> alreadySeen = new HashSet<>();
        for(TestCase testCase : testCases){
            String[] sourceTargetNames = testCase.getName().split("-");
            if(sourceTargetNames.length != 2){
                LOGGER.warn("Test case name contains none or more than one '-' character which is not possible when requesting distinct ontologies."
                        + " We just skip this test case. Name of the test case: {} Name of the track: {}", testCase.getName(), testCase.getTrack().getName());
                continue;
            }
            try {
                if(alreadySeen.contains(sourceTargetNames[0]) == false){
                    distinctOntologies.add(testCase.getSource().toURL());
                    alreadySeen.add(sourceTargetNames[0]);
                }            
                if(alreadySeen.contains(sourceTargetNames[1]) == false){
                    distinctOntologies.add(testCase.getTarget().toURL());
                    alreadySeen.add(sourceTargetNames[1]);
                }
            } catch (MalformedURLException ex) {
                LOGGER.warn("Cannot convert URI to URL at test case {}. Just skipping.", testCase.getName());
            }
        }
        return distinctOntologies;
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
        }else if(type == TestCaseType.PARAMETERS){
            File file = Paths.get(
                    cacheFolder.getAbsolutePath(),
                    encode(this.remoteLocation),
                    encode(this.name),
                    encode(this.version),
                    "parameters",
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
            if(f.getName().equalsIgnoreCase(".DS_Store")) continue; // ignore this file for mac operating systems
            File source_file = new File(f, TestCaseType.SOURCE.toFileName());
            File target_file = new File(f, TestCaseType.TARGET.toFileName());
            File reference_file = new File(f, TestCaseType.REFERENCE.toFileName());
            File parameters_file = new File(f, TestCaseType.PARAMETERS.toFileName());
            
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
                    reference_file.exists() ? reference_file.toURI() : null,
                    this,
                    null,
                    this.goldStandardCompleteness,
                    parameters_file.exists() ? parameters_file.toURI() : null));
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
            if(referenceFile.getName().equalsIgnoreCase(".DS_Store")) continue; // ignore this file for mac operating systems
            String fileNameWithoutExtension = removeExtension(referenceFile.getName());
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
            
            File parametersFile = Paths.get(cacheFolder.getAbsolutePath(),
                    encode(this.remoteLocation),encode(this.name), encode(this.version),
                    "parameters", referenceFile.getName()).toFile();
            
            testCases.add(new TestCase(fileNameWithoutExtension,
                    sourceFile.toURI(),
                    targetFile.toURI(),
                    referenceFile.toURI(),
                    this,
                    null,
                    this.goldStandardCompleteness,
                    parametersFile.exists() ? parametersFile.toURI() : null));
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
    
    
    private String removeExtension(String filename){
        int extensionPos = filename.lastIndexOf('.');
        if (extensionPos == -1)
            return filename;
        return filename.substring(0, extensionPos);
    }

    protected static String getNiceRemoteLocation(String url){
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException ex) {
            return url;
        }
    }

    public static File getCacheFolder() {
        return cacheFolder;
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
