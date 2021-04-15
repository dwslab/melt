package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Note that there is also {@link TestExtractors}.
 */
class MaxWeightBipartiteExtractorTest {


    /**
     * Non-deterministic optimal solution (multiple solutions are possible).
     */
    @Test
    void filter1() {
        MaxWeightBipartiteExtractor mwbe = new MaxWeightBipartiteExtractor();

        Alignment alignment = new Alignment();
        alignment.add("A", "B", 1);
        alignment.add("A", "C", 1);

        try {
            Alignment result = mwbe.match(ModelFactory.createOntologyModel(), ModelFactory.createOntologyModel(),
                    alignment,
                    null);
            assertEquals(1, result.size());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Simple case, optimal solution possible.
     */
    @Test
    void filter2() {
        MaxWeightBipartiteExtractor mwbe = new MaxWeightBipartiteExtractor();

        Alignment alignment = new Alignment();
        alignment.add("A", "B", 0.5);
        alignment.add("A", "C", 1);

        try {
            Alignment result = mwbe.match(ModelFactory.createOntologyModel(), ModelFactory.createOntologyModel(),
                    alignment,
                    null);
            assertEquals(1, result.size());
            assertTrue(result.contains(new Correspondence("A", "C", 1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Two optimal global solutions possible.
     */
    @Test
    void filter3() {
        MaxWeightBipartiteExtractor mwbe = new MaxWeightBipartiteExtractor();

        Alignment alignment = new Alignment();
        alignment.add("A1", "B2", 0.5);
        alignment.add("A1", "C2", 1);
        alignment.add("B1", "C2", 0.5);

        try {
            Alignment result = mwbe.match(ModelFactory.createOntologyModel(), ModelFactory.createOntologyModel(),
                    alignment,
                    null);
            assertTrue(result.size() <= 2);
            assertTrue(result.size() >= 1);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Optimal solution possible.
     */
    @Test
    void filter4() {
        MaxWeightBipartiteExtractor mwbe = new MaxWeightBipartiteExtractor();

        Alignment alignment = new Alignment();
        alignment.add("A1", "B2", 0.5);
        alignment.add("A1", "C2", 1);
        alignment.add("B1", "C2", 1);
        alignment.add("B1", "B2", 1);

        try {
            Alignment result = mwbe.match(ModelFactory.createOntologyModel(), ModelFactory.createOntologyModel(),
                    alignment,
                    null);
            assertEquals(2, result.size());
            assertTrue(result.contains(new Correspondence("A1", "C2", 1)));
            assertTrue(result.contains(new Correspondence("B1", "B2", 1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * No optimal solution.
     */
    @Test
    void filter5() {
        MaxWeightBipartiteExtractor mwbe = new MaxWeightBipartiteExtractor();

        Alignment alignment = new Alignment();
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                alignment.add("" + i, "" + j, 1);
            }
        }

        try {
            Alignment result = mwbe.match(ModelFactory.createOntologyModel(), ModelFactory.createOntologyModel(),
                    alignment,
                    null);
            assertEquals(10, result.size());
        } catch (Exception e) {
            fail(e);
        }
    }
}