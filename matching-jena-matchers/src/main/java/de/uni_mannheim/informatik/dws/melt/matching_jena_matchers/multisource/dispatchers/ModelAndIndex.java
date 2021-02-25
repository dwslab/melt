package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The model (ontology / knowledge graph) and the corressponding index in the list of a multisource matching task.
 */
public class ModelAndIndex {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelAndIndex.class);
    
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
    
    
    public <T> T getModel(Class<T> type){
        try {
            return TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(modelRepresentations, type, this.parameters);
        } catch (TypeTransformationException ex) {
            LOGGER.warn("Could not transform model to required type. Return empty model.");
            return TypeTransformerRegistry.getNewInstance(type);
        }
    }

    public Set<Object> getModelRepresentations() {
        return modelRepresentations;
    }

    public int getIndex() {
        return index;
    }
}
