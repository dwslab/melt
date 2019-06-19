package de.uni_mannheim.informatik.dws.ontmatching.validation;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test ensures a correct implementation of the validation services themselves.
 * The tests executed here are not track-specific - they do not test or validate a particular track.
 *
 * Due to the <a href="http://www.mojohaus.org/templating-maven-plugin/">Templating Maven Plugin</a>, this test has
 * to be executed in maven and might fail when run within the IDE.
 */
class TrackValidationServiceTest {

    @Test
    void analzye() {
        TrackValidationService service = new TrackValidationService(TrackRepository.Conference.V1);
        assertTrue(service.isOK());
    }
}