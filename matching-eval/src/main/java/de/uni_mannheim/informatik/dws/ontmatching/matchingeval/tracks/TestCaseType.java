package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks;

public enum TestCaseType {
    SOURCE,
    TARGET,
    REFERENCE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
    
    public String toFileName() {
        return name().toLowerCase() + ".rdf";
    }
}
