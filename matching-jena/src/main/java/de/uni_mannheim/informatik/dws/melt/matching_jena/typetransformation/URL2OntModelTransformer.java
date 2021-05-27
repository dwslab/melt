package de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AbstractTypeTransformer;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_jena.OntologyCacheJena;
import java.net.URL;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class URL2OntModelTransformer extends AbstractTypeTransformer<URL, OntModel>{


    private static final Logger LOGGER = LoggerFactory.getLogger(URL2OntModelTransformer.class);

    public URL2OntModelTransformer() {
        super(URL.class, OntModel.class);
    }
    
    @Override
    public OntModel transform(URL value, Properties parameters) throws TypeTransformationException {
        return OntologyCacheJena.get(value.toString(), 
                JenaTransformerHelper.getSpec(parameters), 
                JenaTransformerHelper.shouldUseCache(parameters), 
                JenaTransformerHelper.hintLang(parameters)
        );
    }
}
