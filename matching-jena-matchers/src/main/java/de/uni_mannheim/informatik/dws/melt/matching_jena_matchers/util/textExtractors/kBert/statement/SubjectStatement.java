package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedProperty;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedResource;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import static de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcher.NEWLINE;

public class SubjectStatement {
    protected final ProcessedProperty predicate;
    protected final ProcessedResource<Resource> subject;
    protected final String target;

    public SubjectStatement(Statement statement, String target) {
        this.predicate = new ProcessedProperty(statement.getPredicate());
        this.subject = new ProcessedResource<>(statement.getSubject());
        this.target = target;
    }

    public String getNormalized() {
        return String.join(",", target, predicate.getNormalized(), subject.getNormalized(), "s")
                + NEWLINE;
    }

    public ProcessedProperty getPredicate() {
        return predicate;
    }
}
