package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedRDFNode;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedResource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement.LiteralObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement.ObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement.ResourceObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement.SubjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.NormalizedLiteral;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.TextExtractorMapSet;
import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcher.NEWLINE;
import static org.apache.commons.lang3.StringEscapeUtils.escapeCsv;

public class TextExtractorKBert implements TextExtractor {
    private boolean useAllTargets;

    public TextExtractorKBert(boolean useAllTargets) {
        this.useAllTargets = useAllTargets;
    }

    @Override
    public Set<String> extract(Resource targetResource) {
        Iterable<Statement> objectStatements = targetResource::listProperties;
        Map<Object, Set<ObjectStatement<? extends ProcessedRDFNode>>> processedObjectStatements = StreamSupport
                .stream(objectStatements.spliterator(), false)
                .filter(statement -> !statement.getObject().isAnon())
                .map(statement -> {
                    if (statement.getObject().isLiteral()) return new LiteralObjectStatement(statement);
                    return new ResourceObjectStatement(statement);
                })
                .collect(Collectors.groupingBy(ObjectStatement::getClass, Collectors.mapping(Function.identity(), Collectors.toSet())));

        Stream<ObjectStatement<? extends ProcessedRDFNode>> objectStatementStream = processedObjectStatements.values()
                .stream()
                .flatMap(Collection::stream);

        // Get target resource labels
        final Set<NormalizedLiteral> targets;
        if (this.useAllTargets) {
            targets = new TextExtractorMapSet()
                    .getLongAndShortTextNormalizedLiterals(targetResource).get("short");
            System.out.println("");
        } else {
            Set<ObjectStatement<? extends ProcessedRDFNode>> literalObjectStatements =
                    processedObjectStatements.get(LiteralObjectStatement.class);
            final NormalizedLiteral targetNormalizedLiteral;
            if (literalObjectStatements != null) {
                ObjectStatement<? extends ProcessedRDFNode> targetLiteralStatement = literalObjectStatements.stream()
                        .min(Comparator.comparing(s -> s.getPredicate().getLabelType()))
                        .get();
                targetNormalizedLiteral = targetLiteralStatement.getNeighbor().getNormalizedLiteral();
            } else {
                targetNormalizedLiteral = new ProcessedResource<>(targetResource).getNormalizedLiteral();
            }
            targets = Set.of(targetNormalizedLiteral);
        }
        // skip triples where object has target resource label
        Set<Map<String, String>> normalizedObjectStatements = null;
        try {
            normalizedObjectStatements = objectStatementStream
                    .filter(osm -> !targets.contains(osm.getNeighbor().getNormalizedLiteral()))
                    .map(ObjectStatement::getRow)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Iterable<Statement> subjectStatements = () -> targetResource.getModel().listStatements(null, null, targetResource);
        Set<Map<String, String>> normalizedSubjectStatements = StreamSupport
                .stream(subjectStatements.spliterator(), false)
                .filter(statement -> !statement.getSubject().isAnon())
                .map(statement -> new SubjectStatement(statement).getRow())
                .collect(Collectors.toSet());

        String jsonMolecule;
        ObjectMapper mapper = new ObjectMapper();
        try {
            jsonMolecule = mapper.writer().writeValueAsString(Map.of(
                    "t", targets.stream().map(NormalizedLiteral::getLexical).collect(Collectors.toSet()),
                    "s", SetUtils.union(normalizedSubjectStatements, normalizedObjectStatements)
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String extracted = escapeCsv(targetResource.getURI()) + "," + escapeCsv(jsonMolecule) + NEWLINE;
        return Set.of(extracted);
    }
}
