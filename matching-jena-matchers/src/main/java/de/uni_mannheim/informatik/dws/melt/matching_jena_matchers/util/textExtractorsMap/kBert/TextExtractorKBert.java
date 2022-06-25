package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.processedNode.ProcessedRDFNode;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.processedNode.ProcessedResource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.statement.LiteralObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.statement.ObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.statement.ResourceObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.statement.SubjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert.InputTypes;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TextExtractorKBert implements TextExtractorMap {

    @Override
    public Map<String, Set<String>> extract(Resource targetResource) {
        // todo: maybe refine this if necessary
//        todo: here we can use sorting similar as in de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/textExtractors/TextExtractorForTransformers.java:53, but with different enum, similar to this: de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/textExtractors/TextExtractorForTransformers.java:326 but reflecting my preferences
        Iterable<Statement> statements = targetResource::listProperties;
        Map<Object, Set<ObjectStatement<? extends ProcessedRDFNode>>> processedObjectStatements = StreamSupport
                .stream(statements.spliterator(), false)
                // todo: maybe at some point, we can use blank nodes also for generating statements, but not now
                .filter(statement -> !statement.getObject().isAnon())
                .map(statement -> {
                    if (statement.getObject().isLiteral()) return new LiteralObjectStatement(statement);
                    return new ResourceObjectStatement(statement);
                })
                .collect(Collectors.groupingBy(ObjectStatement::getClass, Collectors.mapping(Function.identity(), Collectors.toSet())));

        // Get label of target resource
        Set<ObjectStatement<? extends ProcessedRDFNode>> literalObjectStatements =
                processedObjectStatements.get(LiteralObjectStatement.class);
        String targetName;
        if (literalObjectStatements != null) {
//            todo: do not use literal if it is not a label
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
                .map(ObjectStatement::getNormalized)
                .collect(Collectors.toSet());

//        todo: with csv encoding, make that map keys contain info both on the predicate and on whether its a subject
//         or object statement (this is less complicated than creating new extractor class)
        Iterable<Statement> subjectStatements = () -> targetResource.getModel().listStatements(null, null, targetResource);
        Set<String> normalizedSubjectStatements = StreamSupport
                .stream(subjectStatements.spliterator(), false)
                // todo: maybe at some point, we can use blank nodes also for generating statements, but not now
                .filter(statement -> !statement.getSubject().isAnon())
                .map(statement -> new SubjectStatement(statement).getNormalized())
                .collect(Collectors.toSet());

        return Map.of(
                InputTypes.TARGET.getName(), Set.of(targetName),
                InputTypes.SUBJECT.getName(), normalizedSubjectStatements,
                InputTypes.OBJECT.getName(), normalizedObjectStatements
        );
    }
}
