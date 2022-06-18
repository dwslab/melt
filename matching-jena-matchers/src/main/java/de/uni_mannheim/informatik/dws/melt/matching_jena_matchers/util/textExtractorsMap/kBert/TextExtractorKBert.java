package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.constant.InputTypes;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.processedNode.ProcessedRDFNode;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.processedNode.ProcessedResource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.statement.LiteralObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.statement.ObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.statement.ResourceObjectStatement;
import org.apache.jena.rdf.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TextExtractorKBert implements TextExtractorMap {

    @Override
    public Map<String, Set<String>> extract(Resource targetResource) {
        // todo: maybe refine this if necessary
//        todo: here we can use sorting similar as in de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/textExtractors/TextExtractorForTransformers.java:53, but with different enum, similar to this: de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/textExtractors/TextExtractorForTransformers.java:326 but reflecting my preferences
        Iterable<Statement> statements = targetResource::listProperties;
//        todo: use streams :)
        Map<Object, Set<ObjectStatement<? extends ProcessedRDFNode>>> processedStatements = StreamSupport
                .stream(statements.spliterator(), false)
                // todo: maybe at some point, we can use blank nodes also for generating statements, but not now
                .filter(statement -> !statement.getObject().isAnon())
                .map(statement -> {
                    if (statement.getObject().isLiteral()) return new LiteralObjectStatement(statement);
                    return new ResourceObjectStatement(statement);
                })
                .collect(Collectors.groupingBy(ObjectStatement::getClass, Collectors.mapping(Function.identity(), Collectors.toSet())));

        Set<ObjectStatement<? extends ProcessedRDFNode>> literalStatements =
                processedStatements.get(LiteralObjectStatement.class);
        String targetName;
        if (literalStatements != null) {
//            todo: do not use literal if it is not a label
            ObjectStatement<? extends ProcessedRDFNode> targetLiteralStatement = literalStatements.stream()
                    .min(Comparator.comparing(s -> s.getPredicate().getLabelType()))
                    .get();
            literalStatements.remove(targetLiteralStatement);
            targetName = targetLiteralStatement.getObject().getNormalized();
        } else {
            targetName = new ProcessedResource<>(targetResource).getNormalized();
        }
        // Get label of target resource
        return Map.of(
                InputTypes.TARGET.getName(), Set.of(targetName),
                InputTypes.OBJECT.getName(), processedStatements.values().stream()
                        .flatMap(Collection::stream)
                        .map(ObjectStatement::getNormalized)
                        .collect(Collectors.toSet())
        );
    }
}
