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
    private final boolean multiText;

    public TextExtractorKBertImpl(boolean useAllTargets, boolean normalize, boolean multiText) {
        this.useAllTargets = useAllTargets;
        this.normalize = normalize;
        this.multiText = multiText;
    }

    @Override
    public Set<String> extract(Resource targetResource) {
        Set<Map<String, Set<?>>> molecules = moleculesFromResource(targetResource);
        ObjectMapper mapper = new ObjectMapper();
        return molecules.stream().map(molecule -> {
            String jsonMolecule;
            try {
                jsonMolecule = mapper.writer().writeValueAsString(molecule);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return escapeCsv(targetResource.getURI()) + "," + escapeCsv(jsonMolecule) + NEWLINE;
        }).collect(Collectors.toSet());
    }

    public Set<Map<String, Set<?>>> moleculesFromResource(Resource targetResource) {
        Map<Object, Set<ObjectStatement<? extends ProcessedRDFNode>>> processedObjectStatements =
                getObjectStatementStream(targetResource)
                        .filter(statement -> !statement.getObject().isAnon())
                        .map(statement -> {
                            if (statement.getObject().isLiteral()) return new LiteralObjectStatement(statement);
                            return new ResourceObjectStatement(statement);
                        })
                        .collect(Collectors.groupingBy(ObjectStatement::getClass, Collectors.mapping(Function.identity(), Collectors.toSet())));

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
                targets = Set.of(new ProcessedResource<>(targetResource));
            }
        }

        // nest targets for extracting multiple molecules if needed
        Set<Set<? extends ProcessedRDFNode>> nestedTargets;
        if (this.multiText) {
            nestedTargets = targets.stream().map(Set::of).collect(Collectors.toSet());
        } else {
            nestedTargets = Set.of(targets);
        }

        // get subject statement rows
        Set<Map<String, String>> subjectStatementRows = getSubjectStatementStream(targetResource)
                .filter(statement -> !statement.getSubject().isAnon())
                .map(statement -> new SubjectStatement(statement).getRow())
                .collect(Collectors.toSet());

        return nestedTargets.stream().map(targetSet -> {

            // skip triples where object has target resource label
            Set<Map<String, String>> objectStatementRows = processedObjectStatements.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(osm -> !targetSet.contains(osm.getNeighbor()))
                    .map(ObjectStatement::getRow)
                    .collect(Collectors.toSet());

            return Map.of(
                    "t", targetSet.stream().map(ProcessedRDFNode::getKey).collect(Collectors.toSet()),
                    "s", SetUtils.union(subjectStatementRows, objectStatementRows)
            );
        }).collect(Collectors.toSet());
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
