package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.MAC;

class AlignmentsCubeTest {

    @Test
    @EnabledOnOs({ MAC })
    void addAnalyticalMappingInformation() {
        AlignmentsCube cube = new AlignmentsCube();
        cube.putAnalyticalMappingInformation(TrackRepository.Conference.V1.getTestCases().get(0), "myMatcher", new AnalyticalAlignmentInformation());
        assertEquals(1, cube.size());
    }

    @Test
    @EnabledOnOs({ MAC })
    void getAnalyticalMappingInformation() {
        AlignmentsCube cube = new AlignmentsCube();
        cube.putAnalyticalMappingInformation(TrackRepository.Conference.V1.getTestCases().get(0), "myMatcher", new AnalyticalAlignmentInformation());
        assertEquals(1, cube.size());
        assertNotNull(cube.getAnalyticalMappingInformation(TrackRepository.Conference.V1.getTestCases().get(0), "myMatcher"));
    }
}