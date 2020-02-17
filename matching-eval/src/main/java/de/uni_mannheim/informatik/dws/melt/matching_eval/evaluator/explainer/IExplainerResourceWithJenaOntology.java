package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer;

import de.uni_mannheim.informatik.dws.melt.matching_base.IExplainerResource;
import org.apache.jena.ontology.OntModel;

/**
 * Class capable of explaining resources using an ontology that can be set.
 */
public interface IExplainerResourceWithJenaOntology extends IExplainerResource {

    /**
     * Set the ontology that is to be used for a lookup.
     * @param ontModel The jena ont model to be used for the lookup.
     */
    void setOntModel(OntModel ontModel);
}
