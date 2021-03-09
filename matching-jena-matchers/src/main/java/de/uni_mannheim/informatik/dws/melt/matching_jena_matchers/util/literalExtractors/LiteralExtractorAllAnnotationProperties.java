package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.LiteralExtractor;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * All annotation properties are followed (recursively).
 */
public class LiteralExtractorAllAnnotationProperties implements LiteralExtractor {


    private static final Logger LOGGER = LoggerFactory.getLogger(LiteralExtractorAllAnnotationProperties.class);

    @Override
    @NotNull
    public Set<Literal> extract(Resource resource) {
        Set<Literal> result = new HashSet<>();

        if (resource == null || resource.isAnon()) {
            // anonymous node →
            return result;
        }

        result.addAll(getAnnotationPropertiesRecursionDeadLockSafe(resource, 0));
        return result;
    }

    /**
     * Infinity loop save version of getAnnotationProperties. Always call the latter
     * one to guarantee thread safety. Do not call this method directly but rather
     * its wrapper {@link LiteralExtractorAllAnnotationProperties#extract(Resource)} .
     *
     * @param resource       The resource to be used for lookup.
     * @param recursionDepth The current recursion depth. Initialize with 0.
     * @return A set of annotation property values (String).
     */
    @NotNull
    private static Set<Literal> getAnnotationPropertiesRecursionDeadLockSafe(Resource resource, int recursionDepth) {
        Set<Literal> result = new HashSet<>();
        if (resource.isAnon()) {
            // anonymous node →
            return result;
        }
        recursionDepth++;

        if (resource instanceof OntResource) {
            ExtendedIterator<AnnotationProperty> propertyIterator = ((OntResource) resource).getOntModel().listAnnotationProperties();
            while (propertyIterator.hasNext()) {
                AnnotationProperty property = propertyIterator.next();
                RDFNode n = ((OntResource) resource).getPropertyValue(property);
                if (n != null) {
                    if (n.isURIResource()) {
                        if (recursionDepth < 10) {
                            result.addAll(getAnnotationPropertiesRecursionDeadLockSafe(n.asResource(), recursionDepth));
                        } else {
                            LOGGER.warn("Potential Infinity Loop Detected - aborting annotation property retrieval.");
                            return result;
                        }
                    } else {
                        result.add(n.asLiteral());
                    }
                }
            }
        }
        Literal label = getLabelOrFragmentWithoutLanguageAnnotation(resource);
        if (label != null) {
            result.add(label);
        }
        return result;
    }

    /**
     * Returns the label. If it does not exist: local name.
     *
     * @param resource The resource for which a string shall be retrieved.
     * @return Label or local name. Null if resource is anonymous.
     */
    private static Literal getLabelOrFragmentWithoutLanguageAnnotation(Resource resource) {
        if (resource.isAnon()) {
            return null;
        }

        if (resource instanceof OntResource) {
            ExtendedIterator<RDFNode> iterator = ((OntResource) resource).listLabels(null);
            while (iterator.hasNext()) {
                RDFNode node = iterator.next();
                return node.asLiteral();
            }
        }

        // no label found: return local name
        return ResourceFactory.createStringLiteral(resource.getLocalName());
    }
}
