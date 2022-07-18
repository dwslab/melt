package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.PropertyVocabulary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * All annotation properties are followed (recursively).
 */
public class TextExtractorLongAndShortAnnotationProperties implements TextExtractorMap {


    private static final Logger LOGGER = LoggerFactory.getLogger(TextExtractorLongAndShortAnnotationProperties.class);

    @Override
    public Map<String, Set<String>> extract(Resource resource) {
        Map<String, Set<String>> result = Map.of("long", new HashSet<>(), "short", new HashSet<>());

        if (resource == null || resource.isAnon()) {
            // anonymous node →
            return result;
        }

        return getAnnotationPropertiesRecursionDeadLockSafe(resource, 0, null);
    }

    /**
     * Infinity loop save version of getAnnotationProperties. Always call the latter
     * one to guarantee thread safety. Do not call this method directly but rather
     * its wrapper {@link TextExtractorLongAndShortAnnotationProperties#extract(Resource)} .
     *
     * @param resource       The resource to be used for lookup.
     * @param recursionDepth The current recursion depth. Initialize with 0.
     * @return A set of annotation property values (String).
     */
    private static Map<String, Set<String>> getAnnotationPropertiesRecursionDeadLockSafe(
            Resource resource, int recursionDepth, Property callingProp) {
        Map<String, Set<String>> result = Map.of("long", new HashSet<>(), "short", new HashSet<>());
        if (resource.isAnon()) {
            // anonymous node →
            return result;
        }
        recursionDepth++;
        Model model = resource.getModel();
        if (model instanceof OntModel) {
            ExtendedIterator<AnnotationProperty> propertyIterator = ((OntModel) model).listAnnotationProperties();
            while (propertyIterator.hasNext()) {
                AnnotationProperty property = propertyIterator.next();
                StmtIterator stmts = resource.listProperties(property);
                while (stmts.hasNext()) {
                    Statement stmt = stmts.next();
                    RDFNode n = stmt.getObject();
                    if (n.isResource()) {
                        if (recursionDepth < 10) {
                            Map<String, Set<String>> nextResult = getAnnotationPropertiesRecursionDeadLockSafe(
                                    n.asResource(), recursionDepth, stmt.getPredicate()
                            );
                            result.get("short").addAll(nextResult.get("short"));
                            result.get("long").addAll(nextResult.get("long"));
                        } else {
                            LOGGER.warn("Potential Infinity Loop Detected - aborting annotation property retrieval.");
                            return result;
                        }
                    } else if (n.isLiteral()) {
                        result.get(getLongOrShort(callingProp)).add(n.asLiteral().getLexicalForm().trim());
                    }
                }
            }
        }
        Literal label = getLabelOrFragmentWithoutLanguageAnnotation(resource);
        if (label != null) {
            result.get(getLongOrShort(callingProp)).add(label.getString());
        }
        return result;
    }

    @NotNull
    private static String getLongOrShort(Property callingProp) {
        String longOrShort = "short";
        if (callingProp != null) {
            if (PropertyVocabulary.hasPropertyLabelFragment(callingProp)) {
                longOrShort = "short";
            } else if (PropertyVocabulary.hasPropertyCommentFragment(callingProp)) {
                longOrShort = "long";
            }
        }
        return longOrShort;
    }

    /**
     * Returns the label. If it does not exist: local name.
     *
     * @param resource The resource for which a string shall be retrieved.
     * @return Label or local name. Null if resource is anonymous.
     */
    private static Literal getLabelOrFragmentWithoutLanguageAnnotation(Resource resource) {
        ExtendedIterator<RDFNode> iterator = resource.listProperties(RDFS.label).mapWith(s -> s.getObject());
        while (iterator.hasNext()) {
            RDFNode node = iterator.next();
            if (node.isLiteral())
                return node.asLiteral();
        }
        String uri = resource.getURI();
        if (uri == null)
            return null;
        return ResourceFactory.createStringLiteral(URIUtil.getUriFragment(uri));
    }


    @Override
    public int hashCode() {
        return 54574258;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return getClass() == obj.getClass();
    }
}
