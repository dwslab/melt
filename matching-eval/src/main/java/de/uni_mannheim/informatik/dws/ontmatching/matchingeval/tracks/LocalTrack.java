package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
    private static Logger LOGGER = LoggerFactory.getLogger(LocalTrack.class);

    /**
     * Folder holding the individual test cases of the track.
     */
    private File folderToTestCases;

    /**
     * Constructor
     * @param name Name of the track.
     * @param version Version of the track.
     * @param folderToTestCases The test case folder has to follow a specific structure: It contains a folder for each
     *                          test case. the Folder's name will be the test case's name. In each test case folder,
     *                          there has to be a file named source.rdf, target.rdf, and alignment.rdf.
     */
    public LocalTrack(String name, String version, File folderToTestCases){
        super("http://localhost/", name, version);
        this.folderToTestCases = folderToTestCases;
    }


    @Override
    protected void downloadToCache() throws Exception {
        // intentionally kept empty
    }


    @Override
    protected List<TestCase> readFromCache(){
        List<TestCase> testCases = new ArrayList<>();

        File[] files = folderToTestCases.listFiles();
        if(files == null)
            return testCases;

        for(File f : files){
            File source_file = new File(f, TestCaseType.SOURCE.toFileName());
            File target_file = new File(f, TestCaseType.TARGET.toFileName());
            File reference_file = new File(f, TestCaseType.REFERENCE.toFileName());

            if(source_file.exists() == false || target_file.exists() == false){
                LOGGER.error("There was a problem reading local test case: " + f.getName() + ". Files " +
                        source_file.getName() + " and " + target_file.getName() + " need to exist.");
                continue;
            }

            if(reference_file.exists() == false && skipTestsWithoutRefAlign) {
                LOGGER.warn("Reference file of test case  " + f.getName() + " not found. Skipping...");
                continue;
            }

            testCases.add(new TestCase(
                    f.getName(),
                    source_file.toURI(),
                    target_file.toURI(),
                    reference_file.exists() ? reference_file.toURI() : null, this));
        }
        return testCases;
    }

}
