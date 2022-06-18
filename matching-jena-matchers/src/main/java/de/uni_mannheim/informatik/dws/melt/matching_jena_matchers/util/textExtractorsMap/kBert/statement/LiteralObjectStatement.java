package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.statement;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.processedNode.ProcessedLiteral;
import org.apache.jena.rdf.model.Statement;

public class LiteralObjectStatement extends ObjectStatement<ProcessedLiteral> {
    public LiteralObjectStatement(Statement statement) {
        super(statement);
        this.object = new ProcessedLiteral(statement.getLiteral());
    }
}
