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
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.TextExtractorMapSet;
import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Map<Object, Set<ObjectStatement<? extends ProcessedRDFNode>>> processedObjectStatements =
                getObjectStatementStream(targetResource)
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
        final Set<? extends ProcessedRDFNode> targets;
        if (this.useAllTargets) {
            targets = getAllTargets(targetResource);
        } else {
            Set<ObjectStatement<? extends ProcessedRDFNode>> literalObjectStatements =
                    processedObjectStatements.get(LiteralObjectStatement.class);
            if (literalObjectStatements != null) {
                ObjectStatement<? extends ProcessedRDFNode> targetLiteralStatement = literalObjectStatements.stream()
                        .min(Comparator.comparing(s -> s.getPredicate().getLabelType()))
                        .get();
                targets = Set.of(targetLiteralStatement.getNeighbor());
            } else {
                targets = Set.of(processedTargetResource);
            }
        }
        // skip triples where object has target resource label
        Set<Map<String, String>> objectStatementRows = objectStatementStream
                .filter(osm -> !targets.contains(osm.getNeighbor()))
                .map(ObjectStatement::getRow)
                .collect(Collectors.toSet());

        // get subject statement rows
        Set<Map<String, String>> subjectStatementRows = getSubjectStatementStream(targetResource)
                .filter(statement -> !statement.getSubject().isAnon())
                .map(statement -> new SubjectStatement(statement).getRow())
                .collect(Collectors.toSet());

        return Map.of(
                "t", targets.stream().map(ProcessedRDFNode::getKey).collect(Collectors.toSet()),
                "s", SetUtils.union(subjectStatementRows, objectStatementRows)
        );
    }

    private Set<ProcessedLiteral> getAllTargets(Resource targetResource) {
        return new TextExtractorMapSet().getLongAndShortTextNormalizedLiterals(targetResource).get("short")
                .stream().map(nl -> new ProcessedLiteral(nl.getLexical())).collect(Collectors.toSet());
    }

    @Override
    public Stream<String> getIndexStream(Iterator<? extends OntResource> resourceIterator) {
        return streamFromIterator(resourceIterator)
                .flatMap(r -> Stream.concat(getObjectStatementStream(r), getSubjectStatementStream(r)))
                .flatMap(stmt -> Stream.of(stmt.getSubject(), stmt.getPredicate(), stmt.getObject()))
                .distinct()
                .filter(n -> n.isURIResource() || n.isLiteral())
                .map(n -> n.isURIResource() ? new ProcessedResource<>(n.asResource())
                        : new ProcessedLiteral(n.asLiteral()))
                .distinct()
                .map(pn -> pn.getKey() + "," + escapeCsv(normalize ? pn.getNormalized() : pn.getRaw()));
    }

    @NotNull
    private Stream<Statement> getSubjectStatementStream(Resource r) {
        return streamFromIterator(r.getModel().listStatements(null, null, r));
    }

    @NotNull
    private Stream<Statement> getObjectStatementStream(Resource r) {
        return streamFromIterator(r.listProperties());
    }
}
