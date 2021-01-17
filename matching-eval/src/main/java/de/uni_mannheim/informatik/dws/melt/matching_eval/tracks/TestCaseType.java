package de.uni_mannheim.informatik.dws.melt.matching_eval.tracks;

/**
 * Enumerations of the three files that make up a test case.
 */
public enum TestCaseType {
    SOURCE,
    TARGET,
    REFERENCE,
    PARAMETERS;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    /**
     * Convert enum to file name.
     * @return File name as String.
     */
    public String toFileName() {
        return name().toLowerCase() + ".rdf";
    }
}
