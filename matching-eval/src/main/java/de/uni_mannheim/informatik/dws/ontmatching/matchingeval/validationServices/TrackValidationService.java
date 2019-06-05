package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.validationServices;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;

import java.io.File;


/**
 * This class analyzes a track (i.e., its individual test cases).
 *
 * @author Jan Portisch
 */
public class TrackValidationService {

    /**
     * Perform an analysis on the level of a track.
     *
     * @param track The track to be validated and analyzed.
     * @return Validation result instance.
     */
    public static TrackValidationResult analzye(Track track) {
        TrackValidationResult result = new TrackValidationResult();
        for(TestCase testCase : track.getTestCases()){
            result.individualTrackValidationResults.put(testCase, TestCaseValidationService.analzye(testCase));
        }
        return result;
    }

}
