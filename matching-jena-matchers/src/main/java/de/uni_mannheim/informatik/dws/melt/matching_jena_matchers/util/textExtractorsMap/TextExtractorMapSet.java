package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.*;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.PropertyVocabulary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.jetbrains.annotations.NotNull;


/**
 * A {@link TextExtractor} which extracts texts from a resource which can be used by transformer
 * based matchers like {@link de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFilter}
 * or {@link de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFineTuner}.
 */
public class TextExtractorMapSet implements TextExtractorMap {
    private TextExtractorLongAndShortAnnotationProperties annotationExtractor =
            new TextExtractorLongAndShortAnnotationProperties();

    @Override
    public Map<String, Set<String>> extract(Resource r) {
        Map<String, Set<NormalizedLiteral>> texts = getLongAndShortTextNormalizedLiterals(r);

        Map<String, Set<String>> extractedLiterals = new HashMap<>();
        extractedLiterals.put("shortTexts", getTexts(texts.get("short")));
        extractedLiterals.put("longTexts", getTexts(texts.get("long")));
        return extractedLiterals;
    }

    @NotNull
    public Map<String, Set<NormalizedLiteral>> getLongAndShortTextNormalizedLiterals(Resource r) {
        Map<String, Set<NormalizedLiteral>> texts = Map.of("short", new HashSet<>(), "long", new HashSet<>());

        String longestLiteral = "";
        StmtIterator i = r.listProperties();
        while (i.hasNext()) {
            Statement stmt = i.next();
            RDFNode object = stmt.getObject();
            if (object.isLiteral()) {
                Literal literal = object.asLiteral();
                if (TextExtractorAllStringLiterals.isLiteralAString(literal)) {
                    String text = literal.getLexicalForm().trim();
                    if (!text.isEmpty()) {
                        Property p = stmt.getPredicate();

                        if (PropertyVocabulary.hasPropertyLabelFragment(p)) {
                            texts.get("short").add(new NormalizedLiteral(text));
                        } else if (PropertyVocabulary.hasPropertyCommentFragment(p)) {
                            texts.get("long").add(new NormalizedLiteral(text));
                        }
                        if (text.length() > longestLiteral.length()) {
                            longestLiteral = text;
                        }
                    }
                }
            }
        }

        if (longestLiteral.isEmpty() == false) {
            NormalizedLiteral longest = new NormalizedLiteral(longestLiteral);
            if (texts.get("short").contains(longest) == false && texts.get("long").contains(longest) == false) {
                texts.get("long").add(longest);
            }
        }


        // add uri fragment text
        String uri = r.getURI();
        if (uri != null) {
            String fragment = URIUtil.getUriFragment(uri).trim();
            if (StringProcessing.containsMostlyNumbers(fragment) == false) {
                texts.get("short").add(new NormalizedLiteral(fragment));
            }
        }

        // add annotation properties
        annotationExtractor.extract(r).forEach((key, value) ->
                texts.get(key).addAll(value.stream().map(NormalizedLiteral::new).collect(Collectors.toSet()))
        );

        return texts;
    }

    private Set<String> getTexts(Set<NormalizedLiteral> literals) {
        Set<String> extractedLiterals = new HashSet<>();
        for (NormalizedLiteral l : literals) {
            extractedLiterals.add(l.getLexical());
        }
        return extractedLiterals;
    }
}
