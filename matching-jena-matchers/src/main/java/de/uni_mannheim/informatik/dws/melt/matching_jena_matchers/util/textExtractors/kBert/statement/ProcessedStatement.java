package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedProperty;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedRDFNode;
import org.apache.jena.rdf.model.Statement;

import java.util.Map;

public abstract class ProcessedStatement<T extends ProcessedRDFNode> {
    protected NeighborRole role;

    protected final ProcessedProperty predicate;
    protected T neighbor;

    public ProcessedStatement(Statement statement) {
        this.predicate = new ProcessedProperty(statement.getPredicate());
        this.neighbor = null;
        this.role = null;
    }

    public Map<String, String> getRawRow() {
        return Map.of("p", predicate.getRaw(), "n", neighbor.getRaw(), "r", role.getRole());
    }

    public Map<String, String> getNormalizedRow() {
        return Map.of("p", predicate.getNormalized(), "n", neighbor.getNormalized(), "r", role.getRole());
    }

    public T getNeighbor() {
        return neighbor;
    }

    public ProcessedProperty getPredicate() {
        return predicate;
    }
}
