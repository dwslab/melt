package de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AbstractTypeTransformer;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Model2OntModelTransformer extends AbstractTypeTransformer<Model, OntModel>{


    private static final Logger LOGGER = LoggerFactory.getLogger(Model2OntModelTransformer.class);

    public Model2OntModelTransformer() {
        super(Model.class, OntModel.class);
    }
    
    @Override
    public OntModel transform(Model value, Properties parameters) throws TypeTransformationException {        
        OntModelSpec spec = JenaTransformerHelper.getSpec(parameters);
        return ModelFactory.createOntologyModel(spec,value);
    }
}
