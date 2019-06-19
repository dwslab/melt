package de.uni_mannheim.informatik.dws.ontmatching.validation;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;

import java.util.HashMap;


/**
 * This class analyzes a track (i.e., its individual test cases).
 *
 * @author Jan Portisch
 */
public class TrackValidationService {


    /**
     * Constructor
     */
    public TrackValidationService(Track track){
        this.individualTrackValidationResults = new HashMap<>();
        this.track = track;
        analzye();
    }

    /**
     * Perform an analysis on the level of a track.
     *
     */
    private void analzye() {
        for(TestCase testCase : track.getTestCases()){
            TestCaseValidationService service = new TestCaseValidationService(testCase);
            this.individualTrackValidationResults.put(testCase, service);
        }
    }

    /**
     * The individual validation results per test case.
     */
    private HashMap<TestCase, TestCaseValidationService> individualTrackValidationResults;

    /**
     * The track under analysis.
     */
    private Track track;

    /**
     * Indicates whether the track is free of errors.
     * @return True if error free, else false.
     */
    public boolean isOK(){
        for(HashMap.Entry<TestCase, TestCaseValidationService> entry: individualTrackValidationResults.entrySet()){
            if(!entry.getValue().isOK()){
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString(){
        String result = "";
        for (HashMap.Entry<TestCase, TestCaseValidationService> entry : individualTrackValidationResults.entrySet()){
            result = result + entry.toString() + "\n\n\n";
        }
        return result;
    }

    //---------------------------------------------------------------------------------------------
    // Getter
    //---------------------------------------------------------------------------------------------

    public HashMap<TestCase, TestCaseValidationService> getIndividualTrackValidationResults() {
        return individualTrackValidationResults;
    }

}
