package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedProperty;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedRDFNode;
import org.apache.jena.rdf.model.Statement;

import java.util.Map;

public abstract class ObjectStatement<T extends ProcessedRDFNode> {
    protected final ProcessedProperty predicate;
    protected T object;

    public ObjectStatement(Statement statement) {
        this.predicate = new ProcessedProperty(statement.getPredicate());
        this.object = null;
    }

    public Map<String, String> getNormalized() {
        return Map.of("p", predicate.getNormalized(), "o", object.getNormalized());
    }

    public T getObject() {
        return object;
    }

    public ProcessedProperty getPredicate() {
        return predicate;
    }
}
