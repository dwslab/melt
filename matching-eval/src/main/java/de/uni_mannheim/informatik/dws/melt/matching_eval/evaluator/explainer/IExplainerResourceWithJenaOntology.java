package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer;

import de.uni_mannheim.informatik.dws.melt.matching_base.IExplainerResource;
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
