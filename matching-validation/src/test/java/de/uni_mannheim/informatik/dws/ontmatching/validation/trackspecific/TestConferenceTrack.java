package de.uni_mannheim.informatik.dws.ontmatching.validation.trackspecific;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

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
    
    //@TestFactory //should not be executed every time someone pushes to the github repro
    Stream<DynamicTest> analyze() {
        return AssertHelper.assertDynamicTrack(TrackRepository.Conference.V1);
    }
    
}
