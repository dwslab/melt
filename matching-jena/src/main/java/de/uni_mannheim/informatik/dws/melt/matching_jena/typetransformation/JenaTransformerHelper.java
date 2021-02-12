
package de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation;

import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import de.uni_mannheim.informatik.dws.melt.matching_jena.OntologyCacheJena;
import java.util.Properties;
import org.apache.jena.assembler.assemblers.OntModelSpecAssembler;
import org.apache.jena.ontology.OntModelSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for jena transformers.
 * It usually just retrives some properties for the transformation.
 */
public class JenaTransformerHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(JenaTransformerHelper.class);
    
    public static OntModelSpec getSpec(Properties parameters){
        String spec = parameters.getProperty(ParameterConfigKeys.JENA_ONTMODEL_SPEC);
        if(spec == null)
            return OntologyCacheJena.DEFAULT_JENA_ONT_MODEL_SPEC;
        
        OntModelSpec ontModelSpec = OntModelSpecAssembler.getOntModelSpecField(spec);
        if(ontModelSpec == null){
            LOGGER.warn("The value provided by ParameterConfigKeys.JENA_ONTMODEL_SPEC is not a valid OntModelSpec. Defaulting to OntologyCacheJena.DEFAULT_JENA_ONT_MODEL_SPEC");
            return OntologyCacheJena.DEFAULT_JENA_ONT_MODEL_SPEC;
        }
        return ontModelSpec;
    }
    
    public static boolean shouldUseCache(Properties parameters){
        Object caching = parameters.get(ParameterConfigKeys.USE_ONTOLOGY_CACHE);
        if(caching == null)
            return true;
        if(caching instanceof Boolean){
            return (Boolean) caching;
        }else{
            LOGGER.warn("The value provided by ParameterConfigKeys.USE_ONTOLOGY_CACHE is not of boolean type. Defaulting to true.");
            return true;
        }
    }
    
}
