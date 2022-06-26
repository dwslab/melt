package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedProperty;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedRDFNode;
import org.apache.jena.rdf.model.Statement;

import static de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcher.NEWLINE;
import static org.apache.commons.text.StringEscapeUtils.escapeCsv;

public abstract class ObjectStatement<T extends ProcessedRDFNode> {
    protected final ProcessedProperty predicate;
    private final String subjectId;
    protected T object;

    public ObjectStatement(Statement statement) {
        this.subjectId = escapeCsv(statement.getSubject().getURI());
        this.predicate = new ProcessedProperty(statement.getPredicate());
        this.object = null;
    }

    public String getNormalized(String target) {
        return String.join(",", subjectId, target, predicate.getNormalized(), object.getNormalized(), "o")
                + NEWLINE;
    }

    public T getObject() {
        return object;
    }

    public ProcessedProperty getPredicate() {
        return predicate;
    }
}
