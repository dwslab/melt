package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedRDFNode;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedResource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement.LiteralObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement.ObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement.ResourceObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement.SubjectStatement;
import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TextExtractorKBert implements TextExtractor {

    @Override
    public Set<String> extract(Resource targetResource) {
        Iterable<Statement> statements = targetResource::listProperties;
        Map<Object, Set<ObjectStatement<? extends ProcessedRDFNode>>> processedObjectStatements = StreamSupport
                .stream(statements.spliterator(), false)
                .filter(statement -> !statement.getObject().isAnon())
                .map(statement -> {
                    if (statement.getObject().isLiteral()) return new LiteralObjectStatement(statement);
                    return new ResourceObjectStatement(statement);
                })
                .collect(Collectors.groupingBy(ObjectStatement::getClass, Collectors.mapping(Function.identity(), Collectors.toSet())));

        // Get label of target resource
        Set<ObjectStatement<? extends ProcessedRDFNode>> literalObjectStatements =
                processedObjectStatements.get(LiteralObjectStatement.class);
        final String targetName;
        if (literalObjectStatements != null) {
            ObjectStatement<? extends ProcessedRDFNode> targetLiteralStatement = literalObjectStatements.stream()
                    .min(Comparator.comparing(s -> s.getPredicate().getLabelType()))
                    .get();
            literalObjectStatements.remove(targetLiteralStatement);
            targetName = targetLiteralStatement.getObject().getNormalized();
        } else {
            targetName = new ProcessedResource<>(targetResource).getNormalized();
        }

        Set<String> normalizedObjectStatements = processedObjectStatements.values().stream()
                .flatMap(Collection::stream)
                .map(objectStatement -> objectStatement.getNormalized(targetName))
                .collect(Collectors.toSet());

        Iterable<Statement> subjectStatements = () -> targetResource.getModel().listStatements(null, null, targetResource);
        Set<String> normalizedSubjectStatements = StreamSupport
                .stream(subjectStatements.spliterator(), false)
                .filter(statement -> !statement.getSubject().isAnon())
                .map(statement -> new SubjectStatement(statement, targetName).getNormalized())
                .collect(Collectors.toSet());

        return SetUtils.union(normalizedObjectStatements, normalizedSubjectStatements);
    }
}
