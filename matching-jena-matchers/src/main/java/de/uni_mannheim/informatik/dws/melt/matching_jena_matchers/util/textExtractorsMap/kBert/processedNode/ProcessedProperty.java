package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.processedNode;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.PropertyVocabulary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.constant.KBertLabelPropertyTypes;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDFS;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.constant.KBertLabelPropertyTypes.*;

public class ProcessedProperty extends ProcessedResource<Property> {
    public ProcessedProperty(Property property) {
        super(property);
    }

    public KBertLabelPropertyTypes getLabelType() {
        return resource.equals(RDFS.label) ? LABEL
                : resource.equals(RDFS.comment) ? COMMENT
                : PropertyVocabulary.LABEL_LIKE_PROPERTIES.contains(resource) ? LABEL_LIKE
                : PropertyVocabulary.COMMENT_LIKE_PROPERTIES.contains(resource) ? COMMENT_LIKE
                : PropertyVocabulary.hasPropertyLabelFragment(resource) ? LABEL_NAME
                : PropertyVocabulary.hasPropertyCommentFragment(resource) ? COMMENT_NAME
                : OTHER;
    }
}
