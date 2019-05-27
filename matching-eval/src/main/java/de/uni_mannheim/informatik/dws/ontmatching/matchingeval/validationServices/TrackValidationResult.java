package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.validationServices;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;

import java.util.HashMap;

/**
 * Result object of the {@link TrackValidationService} process.
 *
 * @author Jan Portisch
 */
public class TrackValidationResult {

    /**
     * Constructor
     */
    public TrackValidationResult(){
        this.individualTrackValidationResults = new HashMap<>();
    }

    /**
     * The individual validation results per test case.
     */
    HashMap<TestCase, TestCaseValidationResult> individualTrackValidationResults;

    /**
     * Indicates whether the track is free of errors.
     * @return True if error free, else false.
     */
    public boolean isOK(){
        for(HashMap.Entry<TestCase, TestCaseValidationResult> entry: individualTrackValidationResults.entrySet()){
            if(!entry.getValue().isOK()){
                return false;
            }
        }
        return true;
    }


    //---------------------------------------------------------------------------------------------
    // Getter
    //---------------------------------------------------------------------------------------------

    public HashMap<TestCase, TestCaseValidationResult> getIndividualTrackValidationResults() {
        return individualTrackValidationResults;
    }

}


