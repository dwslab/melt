package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedLiteral;
import org.apache.jena.rdf.model.Statement;

public class LiteralObjectStatement extends ObjectStatement<ProcessedLiteral> {
    public LiteralObjectStatement(Statement statement) {
        super(statement);
        this.neighbor = new ProcessedLiteral(statement.getLiteral());
    }
}
