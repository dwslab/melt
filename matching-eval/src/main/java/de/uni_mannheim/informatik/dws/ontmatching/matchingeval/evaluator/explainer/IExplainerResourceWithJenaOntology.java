package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.explainer;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.IExplainerResource;
import org.apache.jena.ontology.OntModel;

/**
 * Class capable of explaining resources using an ontology that can be set.
 */
public interface IExplainerResourceWithJenaOntology extends IExplainerResource {

    /**
     * Set the ontolog that is to be used for a lookup.
     * @param ontModel
     */
    void setOntModel(OntModel ontModel);
}
