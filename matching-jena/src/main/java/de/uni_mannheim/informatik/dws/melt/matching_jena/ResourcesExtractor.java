package de.uni_mannheim.informatik.dws.melt.matching_jena;

import java.util.Iterator;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;

/**
 * An interface which extracts resources of a given OntModel.
 * This can be for example all classes, all properties, all object properties etc.
 */
public interface ResourcesExtractor {
    
    /**
     * This function extracts specific resources of a given OntModel.
     * This can be for example all classes, all properties, all object properties etc.
     * @param model the ontmodel to extract all resources from
     * @param parameters the properties which are given by the match method. This can contain information about which resources should be matched.
     * @return an iterator of the extracted resources.
     */
    Iterator<? extends OntResource> extract(OntModel model, Properties parameters);
    
}
