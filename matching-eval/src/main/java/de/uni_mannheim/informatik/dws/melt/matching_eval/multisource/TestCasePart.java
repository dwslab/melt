package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;

/**
 * POJO which represents a testcase and the information if an entity corresponds to source or target.
 */
public class TestCasePart {
    private TestCase testcase;
    private boolean source;

    public TestCasePart(TestCase testcase, boolean source) {
        this.testcase = testcase;
        this.source = source;
    }

    public TestCase getTestcase() {
        return testcase;
    }

    public boolean isSource() {
        return source;
    }
}
