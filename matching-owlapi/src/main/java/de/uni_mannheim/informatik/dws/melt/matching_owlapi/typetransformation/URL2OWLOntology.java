package de.uni_mannheim.informatik.dws.melt.matching_owlapi.typetransformation;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AbstractTypeTransformer;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformers.TypeTransformerHelper;
import de.uni_mannheim.informatik.dws.melt.matching_owlapi.OntologyCacheOwlApi;
import java.net.URL;
import java.util.Properties;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Converts the URL to the owlapi OWLOntology
 */
public class URL2OWLOntology extends AbstractTypeTransformer<URL, OWLOntology>{

    public URL2OWLOntology() {
        super(URL.class, OWLOntology.class);
    }
    
    @Override
    public OWLOntology transform(URL value, Properties parameters) throws TypeTransformationException {
        return OntologyCacheOwlApi.get(value, TypeTransformerHelper.shouldUseOntologyCache(parameters));
    }
    
}
