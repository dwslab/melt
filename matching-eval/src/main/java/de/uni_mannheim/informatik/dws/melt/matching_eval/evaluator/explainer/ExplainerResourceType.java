package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer;

import de.uni_mannheim.informatik.dws.melt.matching_base.IExplainerResource;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ResourceType;

import java.util.*;

import org.apache.jena.ontology.OntModel;


/**
 * A simple {@link IExplainerResource} which return the type of the resource.
 */
public class ExplainerResourceType implements IExplainerResourceWithJenaOntology {

    private OntModel ontModel;
    
    @Override
    public void setOntModel(OntModel ontModel) {
        this.ontModel = ontModel;
    }

    @Override
    public Map<String, String> getResourceFeatures(String uri) {
        Map<String, String> map = new HashMap<>();
        map.put("ResourceType", ResourceType.analyze(this.ontModel, uri).toString());
        return map;
    }
    
    @Override
    public List<String> getResourceFeatureNames() {
        return Arrays.asList("ResourceType");
    }
}
