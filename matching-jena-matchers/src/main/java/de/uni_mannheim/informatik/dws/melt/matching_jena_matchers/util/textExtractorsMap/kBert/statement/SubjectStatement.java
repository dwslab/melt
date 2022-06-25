package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.statement;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.processedNode.ProcessedProperty;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.processedNode.ProcessedResource;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class SubjectStatement {
    protected final ProcessedProperty predicate;
    protected final ProcessedResource<Resource> subject;

    public SubjectStatement(Statement statement) {
        this.predicate = new ProcessedProperty(statement.getPredicate());
        this.subject = new ProcessedResource<>(statement.getSubject());
    }

    public String getNormalized() {
        return subject.getNormalized() + " " + predicate.getNormalized();
    }

    public ProcessedProperty getPredicate() {
        return predicate;
    }
}
