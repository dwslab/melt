package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.resourcesExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformers.TypeTransformerHelper;
import de.uni_mannheim.informatik.dws.melt.matching_jena.ResourcesExtractor;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;

/**
 * Extracts RDF properties from a given OntModel.
 */
public class ResourcesExtractorRDFProperties implements ResourcesExtractor{
    @Override
    public Iterator<? extends OntResource> extract(OntModel model, Properties parameters) {
        if(TypeTransformerHelper.shouldMatchRDFProperties(parameters)){
            Set<OntProperty> allProperties = model.listAllOntProperties().toSet();
            allProperties.removeAll(model.listObjectProperties().toSet());
            allProperties.removeAll(model.listDatatypeProperties().toSet());
            return allProperties.iterator();
        }else{
            return Collections.emptyIterator();
        }
    }
}
