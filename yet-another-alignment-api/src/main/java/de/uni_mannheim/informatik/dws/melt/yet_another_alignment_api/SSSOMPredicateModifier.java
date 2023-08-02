package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

public enum SSSOMPredicateModifier {
    NOT("Not");

    private final String repr;

    SSSOMPredicateModifier(String repr) {
        this.repr = repr;
    }

    @Override
    public String toString() {
        return repr;
    }

    public static SSSOMPredicateModifier fromString(String v) {
        if ( v.equals("Not") ) {
            return SSSOMPredicateModifier.NOT;
        }
        return null;
    }
}
