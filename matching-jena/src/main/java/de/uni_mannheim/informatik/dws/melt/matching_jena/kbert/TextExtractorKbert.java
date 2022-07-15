package de.uni_mannheim.informatik.dws.melt.matching_jena.kbert;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import org.apache.jena.ontology.OntModel;

import java.util.stream.Stream;

/**
 * An interface which extracts resources of a given OntModel.
 * This can be for example all classes, all properties, all object properties etc.
 */
public interface TextExtractorKbert extends TextExtractor {

    Stream<String> getIndexStream(OntModel model);
}
