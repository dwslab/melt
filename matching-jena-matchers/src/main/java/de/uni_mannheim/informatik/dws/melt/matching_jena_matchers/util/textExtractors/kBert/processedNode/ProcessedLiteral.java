package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode;

import org.apache.jena.rdf.model.Literal;

public class ProcessedLiteral extends ProcessedRDFNode {
    private final String raw;

    public ProcessedLiteral(Literal literal) {
        this.raw = literal.getLexicalForm().trim();
    }

    public ProcessedLiteral(String raw) {
        this.raw = raw;
    }

    @Override
    public String getRaw() {
        return raw;
    }
}
