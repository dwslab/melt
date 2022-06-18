package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.statement;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.processedNode.ProcessedProperty;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.processedNode.ProcessedRDFNode;
import org.apache.jena.rdf.model.Statement;

public abstract class ObjectStatement<T extends ProcessedRDFNode> {
    protected final ProcessedProperty predicate;
    protected T object;

    public ObjectStatement(Statement statement) {
        this.predicate = new ProcessedProperty(statement.getPredicate());
        this.object = null;
    }

    public String getNormalized() {
        return predicate.getNormalized() + " " + object.getNormalized();
    }

    public T getObject() {
        return object;
    }

    public ProcessedProperty getPredicate() {
        return predicate;
    }
}
