package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedProperty;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedResource;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.Map;

public class SubjectStatement {
    protected final ProcessedProperty predicate;
    protected final ProcessedResource<Resource> subject;

    public SubjectStatement(Statement statement) {
        this.predicate = new ProcessedProperty(statement.getPredicate());
        this.subject = new ProcessedResource<>(statement.getSubject());
    }

    public Map<String, String> getNormalized() {
        return Map.of("p", predicate.getNormalized(), "s", subject.getNormalized());
    }

    public ProcessedProperty getPredicate() {
        return predicate;
    }
}
