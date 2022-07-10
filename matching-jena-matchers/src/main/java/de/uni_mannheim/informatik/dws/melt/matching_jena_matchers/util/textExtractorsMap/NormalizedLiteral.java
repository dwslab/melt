package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;

import java.util.Objects;

public class NormalizedLiteral {
    private final String lexical;
    private final String normalized;

    public NormalizedLiteral(String lexical) {
        this.lexical = lexical;
        this.normalized = String.join(" ", StringProcessing.normalize(lexical));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.normalized);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NormalizedLiteral other = (NormalizedLiteral) obj;
        if (!Objects.equals(this.normalized, other.normalized)) {
            return false;
        }
        return true;
    }

    public String getLexical() {
        return lexical;
    }

    public String getNormalized() {
        return normalized;
    }
}
