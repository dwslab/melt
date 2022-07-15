package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_mannheim.informatik.dws.melt.matching_jena.kbert.TextExtractorKbert;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedLiteral;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedRDFNode;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode.ProcessedResource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement.LiteralObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement.ObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement.ResourceObjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.statement.SubjectStatement;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.NormalizedLiteral;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.TextExtractorMapSet;
import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcher.NEWLINE;
import static de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert.KBertSentenceTransformersMatcher.streamFromIterator;
import static org.apache.commons.lang3.StringEscapeUtils.escapeCsv;

public class TextExtractorKBertImpl implements TextExtractorKbert {
    private final boolean useAllTargets;
    private final boolean normalize;

    public TextExtractorKBertImpl(boolean useAllTargets, boolean normalize) {
        this.useAllTargets = useAllTargets;
        this.normalize = normalize;
    }

    @Override
    public Set<String> extract(Resource targetResource) {
        Map<String, Object> molecule = moleculeFromResource(targetResource);

        String jsonMolecule;
        ObjectMapper mapper = new ObjectMapper();
        try {
            jsonMolecule = mapper.writer().writeValueAsString(molecule);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String extracted = escapeCsv(targetResource.getURI()) + "," + escapeCsv(jsonMolecule) + NEWLINE;
        return Set.of(extracted);
    }

    @NotNull
    public Map<String, Object> moleculeFromResource(Resource targetResource) {
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

        ProcessedResource<Resource> processedTargetResource = new ProcessedResource<>(targetResource);
        // Get target resource labels
        final Set<NormalizedLiteral> targets;
        if (this.useAllTargets) {
            targets = getAllTargets(targetResource);
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
                targetNormalizedLiteral = processedTargetResource.getNormalizedLiteral();
            }
            targets = Set.of(targetNormalizedLiteral);
        }
        // skip triples where object has target resource label
        Set<Map<String, String>> objectStatementRows = objectStatementStream
                .filter(osm -> !targets.contains(osm.getNeighbor().getNormalizedLiteral()))
                .map(ObjectStatement::getRow)
                .collect(Collectors.toSet());

        // get subject statement rows
        Set<Map<String, String>> subjectStatementRows = streamFromIterator(
                targetResource.getModel().listStatements(null, null, targetResource)
        ).filter(statement -> !statement.getSubject().isAnon())
                .map(statement -> new SubjectStatement(statement).getRow())
                .collect(Collectors.toSet());

        Map<String, Object> molecule = Map.of(
                "t", processedTargetResource.getKey(),
                "s", SetUtils.union(subjectStatementRows, objectStatementRows)
        );
        return molecule;
    }

    private Set<NormalizedLiteral> getAllTargets(Resource targetResource) {
        return new TextExtractorMapSet().getLongAndShortTextNormalizedLiterals(targetResource).get("short");
    }

    @Override
    public Stream<String> getIndexStream(OntModel model) {
        return streamFromIterator(model.listStatements())
                .flatMap(stmt -> Stream.of(stmt.getSubject(), stmt.getPredicate(), stmt.getObject()))
                .distinct()
                .filter(n -> n.isURIResource() || n.isLiteral())
                .flatMap(n -> {
                    String key;
                    Stream<String> values;
                    if (n.isURIResource()) {
                        Resource resource = n.asResource();
                        ProcessedResource<Resource> processedResource = new ProcessedResource<>(resource);
                        NormalizedLiteral prefLabel = processedResource.getNormalizedLiteral();
                        key = processedResource.getKey();
                        if (this.useAllTargets && !(resource instanceof Property)) {
                            values = getAllTargets(resource)
                                    .stream()
                                    .map(nl -> {
                                        boolean isPrefLabel = nl.equals(prefLabel);
                                        String label = this.normalize ? nl.getNormalized() : nl.getLexical();
                                        return escapeCsv(label) + "," + isPrefLabel;
                                    });
                        } else {
                            String label = this.normalize ? prefLabel.getNormalized() : prefLabel.getLexical();
                            values = Stream.of(escapeCsv(label) + "," + true);
                        }
                    } else {
                        ProcessedLiteral processedLiteral = new ProcessedLiteral(n.asLiteral());
                        key = processedLiteral.getKey();
                        String label = this.normalize ? processedLiteral.getNormalized() : processedLiteral.getRaw();
                        values = Stream.of(escapeCsv(label) + "," + true);
                    }
                    return values.map(v -> escapeCsv(key) + "," + v);
                });
    }
}
