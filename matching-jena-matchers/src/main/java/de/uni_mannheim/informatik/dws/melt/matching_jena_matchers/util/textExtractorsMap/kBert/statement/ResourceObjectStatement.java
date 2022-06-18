package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.statement;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.processedNode.ProcessedResource;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class ResourceObjectStatement extends ObjectStatement<ProcessedResource<Resource>> {
    public ResourceObjectStatement(Statement statement) {
        super(statement);
        this.object = new ProcessedResource<>(statement.getResource());
    }
}
