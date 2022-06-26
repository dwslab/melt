package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode;

import org.apache.jena.rdf.model.Literal;

public class ProcessedLiteral extends ProcessedRDFNode {
    private final Literal literal;

    public ProcessedLiteral(Literal literal) {
        this.literal = literal;
    }

    @Override
    protected String getRaw() {
        return literal.getLexicalForm().trim();
    }
}
