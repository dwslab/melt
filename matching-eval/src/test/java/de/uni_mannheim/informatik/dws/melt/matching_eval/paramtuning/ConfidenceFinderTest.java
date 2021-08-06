package de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning;

import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ConfidenceFinderTest {


    @Test
    public void getBestConfidenceForFMeasureIncomplete() {
        Alignment reference = new Alignment();
        reference.add("A", "A");
        reference.add("B", "B");
        reference.add("C", "C");

        Alignment system = new Alignment();

        // correct
        system.add("A", "A", 1.0);
        system.add("B", "B", 0.8);

        // cannot be judged
        system.add("D", "D", 0.9);
        system.add("E", "E", 0.9);

        // wrong
        system.add("A", "B", 0.7);

        double result = ConfidenceFinder.getBestConfidenceForFmeasure(reference, system,
                GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE);
        Assertions.assertEquals(0.8, result);
    }

    @Test
    public void getBestConfidenceForFMeasureIncomplete2() {
        Alignment reference = new Alignment();
        reference.add("A", "A");
        reference.add("B", "B");
        reference.add("C", "C");

        Alignment system = new Alignment();

        // correct
        system.add("A", "A", 1.0);
        system.add("B", "B", 0.5);

        // cannot be judged
        system.add("D", "D", 0.9);
        system.add("E", "E", 0.9);

        // wrong
        system.add("A", "B", 0.7);
        system.add("B", "C", 0.7);
        system.add("A", "C", 0.7);

        double result = ConfidenceFinder.getBestConfidenceForFmeasure(reference, system,
                GoldStandardCompleteness.PARTIAL_SOURCE_INCOMPLETE_TARGET_INCOMPLETE);
        Assertions.assertEquals(0.5, result);

        result = ConfidenceFinder.getBestConfidenceForFmeasure(reference, system,
                GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE);
        Assertions.assertEquals(1.0, result);
    }

    @Test
    public void getBestConfidenceForFMeasure() {
        Alignment myAlignment = new Alignment();

        // correct
        myAlignment.add("http://mouse.owl#MA_0002401", "http://human.owl#NCI_C52561", 1.0);
        myAlignment.add("http://mouse.owl#MA_0000270", "http://human.owl#NCI_C33736", 0.9);

        // incorrect
        myAlignment.add("FALSE", "FALSE", 0.8);

        // build the test case
        TestCase anatomyTc = TrackRepository.Anatomy.Default.getFirstTestCase();
        ExecutionResult er = new ExecutionResult(anatomyTc,
                "name",
                myAlignment,
                anatomyTc.getParsedReferenceAlignment());

        double result = ConfidenceFinder.getBestConfidenceForFmeasure(er);
        Assertions.assertEquals(0.9, result);
    }

    /**
     * TODO: This test seems to be incomplete... Since there are no assertions, it is disabled.
     */
    @Test
    @Disabled
    public void checkGetAllPossibleSteps() {
        Alignment a = new Alignment();
        Set<Double> reference = new HashSet<>();

        BigDecimal begin = BigDecimal.ZERO;
        BigDecimal end = BigDecimal.valueOf(1.0);
        BigDecimal step = BigDecimal.valueOf(0.001);
        for (BigDecimal i = begin; i.compareTo(end) < 0; i = i.add(step)) {
            double d = i.doubleValue();
            a.add("http://one.com/" + d, "http://two.com/" + d, d);
            reference.add(d);
        }

        //Set<Double> confs = ConfidenceFinder.getOccurringConfidences(a);
        //assertSame(confs, reference);

        //Set<Double> confs = ConfidenceFinder.getOccurringConfidences(a,1);
        //assertSame(confs, new HashSet<>(Arrays.asList(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0)));
        //TODO: make assertion
    }
}
