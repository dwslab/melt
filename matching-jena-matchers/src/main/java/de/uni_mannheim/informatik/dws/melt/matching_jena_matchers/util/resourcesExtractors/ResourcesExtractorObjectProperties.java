package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.resourcesExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformers.TypeTransformerHelper;
import de.uni_mannheim.informatik.dws.melt.matching_jena.ResourcesExtractor;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;

/**
 * Extracts object properties from a given OntModel.
 */
public class ResourcesExtractorObjectProperties implements ResourcesExtractor{
    @Override
    public Iterator<? extends OntResource> extract(OntModel model, Properties parameters) {
        if(TypeTransformerHelper.shouldMatchObjectProperties(parameters)){
            return model.listObjectProperties() ;
        }else{
            return Collections.emptyIterator();
        }
    }
}
