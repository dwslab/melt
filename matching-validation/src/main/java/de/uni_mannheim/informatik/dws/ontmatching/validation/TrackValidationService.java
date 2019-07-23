package de.uni_mannheim.informatik.dws.ontmatching.validation;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.Track;


/**
 * This class analyzes a track (i.e., its individual test cases).
 *
 * @author Jan Portisch
 */
public class TrackValidationService {

    /**
     * The track under analysis.
     */
    private Track track;
    

    /**
     * Constructor
     */
    public TrackValidationService(Track track){
        this.track = track;
    }

    /**
     * Indicates whether the track is free of errors.
     * @return True if error free, else false.
     */
    public boolean isOK(){
        for(TestCase testCase : track.getTestCases()){
            TestCaseValidationService service = new TestCaseValidationService(testCase, SemanticWebLibrary.JENA);
            if(!service.isOK()){
                return false;
            }
            service = new TestCaseValidationService(testCase, SemanticWebLibrary.OWLAPI);
            if(!service.isOK()){
                return false;
            }
        }
        return true;
    }
}
