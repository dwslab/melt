package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import java.util.Properties;
import java.util.Set;

/**
 * The model (ontology / knowledge graph) and the corressponding index in the list of a multisource matching task.
 */
public class ModelAndIndex {
    private Set<Object> modelRepresentations;
    private int index;
    private Properties parameters; // just for type transformation

    public ModelAndIndex(Set<Object> modelRepresentations, int index) {
        this.modelRepresentations = modelRepresentations;
        this.index = index;
        this.parameters = new Properties();
    }

    public ModelAndIndex(Set<Object> modelRepresentations, int index, Properties parameters) {
        this.modelRepresentations = modelRepresentations;
        this.index = index;
        this.parameters = parameters;
    }
    
    
    public <T> T getModel(Class<? extends T> type){
        return TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(modelRepresentations, type, this.parameters);
    }

    public Set<Object> getModelRepresentations() {
        return modelRepresentations;
    }

    public int getIndex() {
        return index;
    }
}
