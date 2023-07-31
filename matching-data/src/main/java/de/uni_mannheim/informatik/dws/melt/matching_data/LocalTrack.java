package de.uni_mannheim.informatik.dws.melt.matching_data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * A track that does not exist on the SEALS repository.
 * use this class if you want to create and evaluate you own data set.
 *
 * @author Jan Portisch
 * @author Sven Hertling
 */
public class LocalTrack extends Track {


    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalTrack.class);

    /**
     * Folder holding the individual test cases of the track.
     */
    private final File folderToTestCases;

    /**
     * Default Constructor
     * @param name Name of the track.
     * @param version Version of the track.
     * @param folderToTestCases The test case folder has to follow a specific structure: It contains a folder for each
     *                          test case. The Folder's name will be the test case's name. In each test case folder,
     *                          there has to be a file named source.rdf, target.rdf, and reference.rdf.
     * @param goldStandardCompleteness Completeness of the gold standard.
     */
    public LocalTrack(String name, String version, File folderToTestCases, GoldStandardCompleteness goldStandardCompleteness){
        super("http://localhost/", name, version, false, goldStandardCompleteness);
        this.folderToTestCases = folderToTestCases;
    }

    /**
     * Default Constructor
     * @param name Name of the track.
     * @param version Version of the track.
     * @param folderToTestCasesPath The test case folder path.
     *                              It has to follow a specific structure:
     *                              It contains a folder for each test case.
     *                              The Folder's name will be the test case's name. In each test case folder,
     *                              there has to be a file named source.rdf, target.rdf, and reference.rdf.
     * @param goldStandardCompleteness Completeness of the gold standard.
     */
    public LocalTrack(String name, String version, String folderToTestCasesPath, GoldStandardCompleteness goldStandardCompleteness){
        super("http://localhost/", name, version, false, goldStandardCompleteness);
        this.folderToTestCases = new File(folderToTestCasesPath);
    }
    
    /**
     * Default Constructor with complete gold standard.
     * @param name Name of the track.
     * @param version Version of the track.
     * @param folderToTestCases The test case folder has to follow a specific structure: It contains a folder for each
     *                          test case. the Folder's name will be the test case's name. In each test case folder,
     *                          there has to be a file named source.rdf, target.rdf, and alignment.rdf.
     */
    public LocalTrack(String name, String version, File folderToTestCases){
        this(name,version, folderToTestCases, GoldStandardCompleteness.COMPLETE);
    }
    
    /**
     * Convenience Constructor
     * @param name Name of the track.
     * @param version Version of the track.
     * @param folderToTestCasesPath The test case folder has to follow a specific structure: It contains a folder for
     *                              each test case. The folder's name will be the test case's name. In each test case
     *                              folder, there has to be a file named source.rdf, target.rdf, and reference.rdf.
     */
    public LocalTrack(String name, String version, String folderToTestCasesPath){
        this(name, version, new File(folderToTestCasesPath));
    }

    
    /**************************************
     * Constructors with list of testcases.
     **************************************/
    
    /**
     * Default Constructor
     * @param name Name of the track. Can be any text identifying the track
     * @param version Version of the track. Can be any text identifying the version e.g. semantic versioning like 1.0.1.
     * @param testCases the testcases corresponding to this track. The track in the testcases can be set to null (will be set in this constructor).
     * @param goldStandardCompleteness how complete is the gold standard for this track.
     */
    public LocalTrack(String name, String version, List<TestCase> testCases, GoldStandardCompleteness goldStandardCompleteness){
        super("http://localhost/", name, version, false, goldStandardCompleteness);
        this.testCases = testCases;
        for(TestCase tc : this.testCases){
            tc.setTrack(this);
        }
        this.folderToTestCases = null;
    }
    
    /**
     * Default Constructor.
     * The gold standard is assumed to be complete use {@link #LocalTrack(java.lang.String, java.lang.String, java.util.List, de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness)} in case it is different. 
     * @param name Name of the track. Can be any text identifying the track
     * @param version Version of the track. Can be any text identifying the version e.g. semantic versioning like 1.0.1.
     * @param testCases the testcases corresponding to this track. The track in the testcases can be set to null (will be set in this constructor).
     */
    public LocalTrack(String name, String version, List<TestCase> testCases){
        this(name,version, testCases, GoldStandardCompleteness.COMPLETE);
    }
    
    /**
     * TestCases needs to be added after generating this track (when using this constructor).
     * @param name Name of the track. Can be any text identifying the track
     * @param version Version of the track. Can be any text identifying the version e.g. semantic versioning like 1.0.1.
     * @param goldStandardCompleteness how complete is the gold standard for this track.
     */
    public LocalTrack(String name, String version, GoldStandardCompleteness goldStandardCompleteness){
        super("http://localhost/", name, version, false, goldStandardCompleteness);
        this.testCases = new ArrayList<>();
        this.folderToTestCases = null;
    }
    
    /**
     * TestCases needs to be added after generating this track (when using this constructor).
     * A complete gold standard is assumed. 
     * @param name Name of the track. Can be any text identifying the track
     * @param version Version of the track. Can be any text identifying the version e.g. semantic versioning like 1.0.1.
     */
    public LocalTrack(String name, String version){
        super("http://localhost/", name, version, false, GoldStandardCompleteness.COMPLETE);
        this.testCases = new ArrayList<>();
        this.folderToTestCases = null;
    }
    
    public TestCase addTestCase(TestCase tc){
        tc.setTrack(this);
        this.testCases.add(tc);
        return tc;
    }
    
    public TestCase addTestCase(String name, URI source, URI target, URI reference, URI inputAlignment, GoldStandardCompleteness goldStandardCompleteness, URI parameters, URI evaluationExclusionAlignment){
        TestCase tc = new TestCase(name,source,target,reference,this, inputAlignment, goldStandardCompleteness, parameters, evaluationExclusionAlignment);
        this.testCases.add(tc);
        return tc;
    }
    
    public TestCase addTestCase(String name, File source, File target, File reference, File inputAlignment, GoldStandardCompleteness goldStandardCompleteness, File parameters, File evaluationExclusionAlignment){
        TestCase tc = new TestCase(name,
                source.toURI(),
                target.toURI(),
                reference == null ? null : reference.toURI(),
                this, 
                inputAlignment == null ? null : inputAlignment.toURI(), 
                goldStandardCompleteness, 
                parameters == null ? null : parameters.toURI(),
                evaluationExclusionAlignment == null ? null : evaluationExclusionAlignment.toURI());
        this.testCases.add(tc);
        return tc;
    }
    
    public TestCase addTestCase(String name, File source, File target, File reference){
        return addTestCase(name, source,target, reference, null, this.goldStandardCompleteness, null, null);
    }
    
    
    @Override
    protected void downloadToCache() throws Exception {
        // intentionally kept empty
    }

    @Override
    protected List<TestCase> readFromCache(){
        List<TestCase> testCases = new ArrayList<>();

        File[] files = folderToTestCases.listFiles(File::isDirectory);
        if(files == null)
            return testCases;

        for(File f : files){
            File source_file = new File(f, TestCaseType.SOURCE.toFileName());
            File target_file = new File(f, TestCaseType.TARGET.toFileName());
            File reference_file = new File(f, TestCaseType.REFERENCE.toFileName());
            File input_file = new File(f, TestCaseType.INPUT.toFileName());
            File parameters_file = new File(f, TestCaseType.PARAMETERS.toFileName());
            File evalExclusion_file = new File(f, TestCaseType.EVALUATIONEXCLUSION.toFileName());

            if(!source_file.exists() || !target_file.exists()){
                LOGGER.error("There was a problem reading local test case: " + f.getName() + ". Files " +
                        source_file.getName() + " and " + target_file.getName() + " need to exist.");
                continue;
            }

            if(!reference_file.exists() && skipTestsWithoutRefAlign) {
                LOGGER.warn("Reference file of test case  " + f.getName() + " not found. Skipping...");
                continue;
            }

            testCases.add(new TestCase(
                    f.getName(),
                    source_file.toURI(),
                    target_file.toURI(),
                    reference_file.exists() ? reference_file.toURI() : null, 
                    this,
                    input_file.exists() ? input_file.toURI() : null,
                    this.goldStandardCompleteness,
                    parameters_file.exists() ? parameters_file.toURI() : null,
                    evalExclusion_file.exists() ? evalExclusion_file.toURI() : null
                    ));
        }
        return testCases;
    }
}
