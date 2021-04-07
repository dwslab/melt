package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.significance;

/**
 * The supported test types for McNemar significance tests.
 */
public enum TestType {


    /**
     * Exact McNemar test.
     * Only use the exact test for very small datasets, the factorial quickly gets too large.
     * If you want to use the exact tests for small datasets automatically, use
     * the types with automatic fallback to the exact tests for
     * small data ({@link TestType#ASYMPTOTIC_EXACT_FALLBACK},
     * {@link TestType#ASYMPTOTIC_CONTINUITY_CORRECTION_EXACT_FALLBACK}).
     */
    EXACT,

    // Not implemented:
    //MID_P_TEST,

    /**
     * Asymptotic test. Works only if b + c &gt; 25.
     */
    ASYMPTOTIC,

    /**
     * Asymptotic test. If b + c &gt; 25, the exact test is used.
     */
    ASYMPTOTIC_EXACT_FALLBACK,

    /**
     * Asymptotic test with continuity correction. If b + c &gt; 25, the exact test is used.
     */
    ASYMPTOTIC_CONTINUITY_CORRECTION,

    /**
     * Asymptotic test with continuity correction. If b + c &gt; 25, the exact test is used.
     */
    ASYMPTOTIC_CONTINUITY_CORRECTION_EXACT_FALLBACK;
}