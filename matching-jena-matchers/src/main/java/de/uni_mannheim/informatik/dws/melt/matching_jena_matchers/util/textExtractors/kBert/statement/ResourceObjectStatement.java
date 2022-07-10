package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedResource;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class ResourceObjectStatement extends ObjectStatement<ProcessedResource<Resource>> {
    public ResourceObjectStatement(Statement statement) {
        super(statement);
        this.neighbor = new ProcessedResource<>(statement.getResource());
    }
}
