package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedRDFNode;
import org.apache.jena.rdf.model.Statement;

public abstract class ObjectStatement<T extends ProcessedRDFNode> extends ProcessedStatement<T> {
    public ObjectStatement(Statement statement) {
        super(statement);
        this.role = NeighborRole.OBJECT;
    }
}
