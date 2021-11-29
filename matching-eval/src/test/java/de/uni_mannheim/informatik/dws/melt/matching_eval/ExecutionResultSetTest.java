package de.uni_mannheim.informatik.dws.melt.matching_eval;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.BaselineStringMatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionResultSetTest {


    /**
     * Testing various getDistinct... methods of {@link ExecutionResultSet}.
     */
    @Test
    void getDistinctX() {
        ExecutionResultSet ers = new ExecutionResultSet();
        ers.add(Executor.runSingle(
                TrackRepository.Multifarm.getSpecificMultifarmTrack("de-en").getFirstTestCase(),
                new BaselineStringMatcher(), "B")
        );
        ers.add(Executor.runSingle(
                TrackRepository.Multifarm.getSpecificMultifarmTrack("de-fr").getFirstTestCase(),
                new BaselineStringMatcher(), "B")
        );
        ers.add(Executor.runSingle(
                TrackRepository.Multifarm.getSpecificMultifarmTrack("ar-de").getFirstTestCase(),
                new BaselineStringMatcher(), "A")
        );

        assertEquals(2, getSizeOfIterator(ers.getDistinctMatchers()));
        assertEquals(2, getSizeOfIterator(ers.getDistinctMatchersSorted()));
        assertEquals(3, getSizeOfIterator(ers.getDistinctTestCases()));
        assertEquals(3, getSizeOfIterator(ers.getDistinctTestCasesSorted()));
        assertEquals(2, getSizeOfIterator(ers.getDistinctTestCases("B")));
        assertEquals(2, getSizeOfIterator(ers.getDistinctTestCasesSorted("B")));
        assertEquals(3, getSizeOfIterator(ers.getDistinctTracks()));
        assertEquals(3, getSizeOfIterator(ers.getDistinctTracksSorted()));
        assertEquals(2, getSizeOfIterator(ers.getDistinctTracks("B")));
        assertEquals(2, getSizeOfIterator(ers.getDistinctTracksSorted("B")));
    }

    /**
     * Testing various getDistinct... methods of {@link ExecutionResultSet}.
     */
    @Test
    void getDistinctX2() {
        ExecutionResultSet ers = new ExecutionResultSet();
        ers.add(Executor.runSingle(
                TrackRepository.Phenotype.V2017.DOID_ORDO.getFirstTestCase(),
                new BaselineStringMatcher(),
                "A"));
        ers.add(Executor.runSingle(
                TrackRepository.Phenotype.V2017.HP_MP.getFirstTestCase(),
                new BaselineStringMatcher(),
                "A"));

        assertEquals(1, getSizeOfIterator(ers.getDistinctMatchers()));
        assertEquals(1, getSizeOfIterator(ers.getDistinctMatchersSorted()));
        assertEquals(2, getSizeOfIterator(ers.getDistinctTestCases()));
        assertEquals(2, getSizeOfIterator(ers.getDistinctTestCasesSorted()));
        assertEquals(0, getSizeOfIterator(ers.getDistinctTestCases("B")));
        assertEquals(0, getSizeOfIterator(ers.getDistinctTestCasesSorted("B")));
        assertEquals(2, getSizeOfIterator(ers.getDistinctTracks()));
        assertEquals(2, getSizeOfIterator(ers.getDistinctTracksSorted()));
        assertEquals(0, getSizeOfIterator(ers.getDistinctTracks("B")));
        assertEquals(0, getSizeOfIterator(ers.getDistinctTracksSorted("B")));
    }


    /**
     * Determines the size of an iterable.
     *
     * @param iterable Some iterable.
     * @return Size (i.e. number of elements) of the iterable.
     */
    private int getSizeOfIterator(Iterable<?> iterable) {
        int result = 0;
        for (Object i : iterable) {
            result++;
        }
        return result;
    }

}