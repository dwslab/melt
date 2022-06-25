package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.processedNode;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorUrlFragment;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.statement.LiteralObjectStatement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class ProcessedResource<T extends Resource> extends ProcessedRDFNode {
    protected final T resource;

    public ProcessedResource(T resource) {
        this.resource = resource;
    }

    protected String getRaw() {
        Iterable<Statement> statements = resource::listProperties;
        return StreamSupport.stream(statements.spliterator(), false)
                .filter(s -> s.getObject().isLiteral())
                .map(LiteralObjectStatement::new)
                .min(Comparator.comparing(s -> s.getPredicate().getLabelType()))
                .map(s -> s.getObject().getNormalized())
                .or(() -> Optional.ofNullable(new TextExtractorUrlFragment().extract(resource).iterator().next()))
                .orElse(null);
    }
}
