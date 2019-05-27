package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks;

/**
 * Enumerations of the three files that make up a test case.
 */
public enum TestCaseType {
    SOURCE,
    TARGET,
    REFERENCE;

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
