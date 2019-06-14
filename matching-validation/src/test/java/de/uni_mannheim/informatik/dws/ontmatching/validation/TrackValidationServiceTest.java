package de.uni_mannheim.informatik.dws.ontmatching.validation;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrackValidationServiceTest {

    @Test
    void analzye() {
        TrackValidationResult result = TrackValidationService.analzye(TrackRepository.Conference.V1);
        assertNotNull(result);
        assertTrue(result.isOK());
    }
}