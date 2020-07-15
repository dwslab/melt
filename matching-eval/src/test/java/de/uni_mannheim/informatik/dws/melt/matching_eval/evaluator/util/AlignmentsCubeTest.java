package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.util;

import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlignmentsCubeTest {

    @Test
    void addAnalyticalMappingInformation() {
        AlignmentsCube cube = new AlignmentsCube();
        cube.putAnalyticalMappingInformation(TrackRepository.Conference.V1.getTestCases().get(0), "myMatcher", new AnalyticalAlignmentInformation());
        assertEquals(1, cube.size());
    }

    @Test
    void getAnalyticalMappingInformation() {
        AlignmentsCube cube = new AlignmentsCube();
        cube.putAnalyticalMappingInformation(TrackRepository.Conference.V1.getTestCases().get(0), "myMatcher", new AnalyticalAlignmentInformation());
        assertEquals(1, cube.size());
        assertNotNull(cube.getAnalyticalMappingInformation(TrackRepository.Conference.V1.getTestCases().get(0), "myMatcher"));
    }

    @Test
    void cutStringAfterThirtyTwoThousandCharacters(){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < 32001; i++){
            sb.append("A");
        }
        assertTrue(sb.toString().length() > 32000);
        String result1 = AlignmentsCube.cutAfterThirtyTwoThousandCharacters(sb.toString());
        assertTrue(result1.length() == 32000);

        String hello = "Hello World!";
        String result2 = AlignmentsCube.cutAfterThirtyTwoThousandCharacters(hello);
        assertEquals(hello, result2);
    }
}