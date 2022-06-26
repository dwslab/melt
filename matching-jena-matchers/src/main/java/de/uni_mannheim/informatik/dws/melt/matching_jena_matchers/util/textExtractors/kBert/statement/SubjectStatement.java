package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedProperty;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedResource;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import static de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcher.NEWLINE;
import static org.apache.commons.text.StringEscapeUtils.escapeCsv;

public class SubjectStatement {
    protected final ProcessedProperty predicate;
    protected final ProcessedResource<Resource> subject;
    protected final String target;
    private final String objectId;

    public SubjectStatement(Statement statement, String target) {
        this.objectId = escapeCsv(statement.getObject().asResource().getURI());
        this.target = target;
        this.predicate = new ProcessedProperty(statement.getPredicate());
        this.subject = new ProcessedResource<>(statement.getSubject());
    }

    public String getNormalized() {
        return String.join(",", objectId, target, predicate.getNormalized(), subject.getNormalized(), "s")
                + NEWLINE;
    }

    public ProcessedProperty getPredicate() {
        return predicate;
    }
}
