package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer;

import org.apache.jena.rdf.model.Property;

/**
 * Internal data structure which represents a tuple of the form (String name, Property property).
 */
public class NamePropertyTuple {
    public String name;
    public Property property;

    public NamePropertyTuple(String name, Property property) {
        this.name = name;
        this.property = property;
    }
}