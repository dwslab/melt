package de.uni_mannheim.informatik.dws.melt.matching_validation.trackspecific;

import java.util.stream.Stream;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import org.junit.jupiter.api.DynamicTest;

/**
 * This test ensures that the conference track is parsable.
 */
public class TestConferenceTrack {


    /*
    @Test
    void analyze() {
        AssertHelper.assertTrack(TrackRepository.Conference.V1);
    }
*/
    
    //@TestFactory //should not be executed every time someone pushes to the github repo
    Stream<DynamicTest> analyze() {
        return AssertHelper.assertDynamicTrack(TrackRepository.Conference.V1);
    }
    
}
