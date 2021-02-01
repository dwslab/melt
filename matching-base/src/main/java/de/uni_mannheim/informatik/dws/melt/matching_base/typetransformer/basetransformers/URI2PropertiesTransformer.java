package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformer;
import java.net.URI;
import java.util.Properties;
import java.util.Scanner;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Transforms a URI to java.uril.Properties.
 * Currently supported formats: YAML, JSON.
 */
public class URI2PropertiesTransformer implements TypeTransformer<URI, Properties>  {
    private static final Logger LOGGER = LoggerFactory.getLogger(URI2PropertiesTransformer.class);
    
    @Override
    public Properties transform(URI value, Properties parameters) throws Exception {
        
        String content = null;
        try (Scanner scanner = new Scanner(value.toURL().openStream(), "UTF-8")) {
            content = scanner.useDelimiter("\\A").next();
        }
        
        if(content == null || content.trim().isEmpty()){
            LOGGER.warn("The content of the URI for the parameters is empty. Returning empty set of parameters.");
            return new Properties();
        } 
        
        try{
            Properties p = new Properties();
            p.putAll(new JSONObject(content).toMap());
            return p;
        }catch(JSONException ex){
            LOGGER.debug("Could not parse JSON - continue...");
        }
        
        try{
            Properties p = new Properties();
            p.putAll(new Yaml().load(content));
        }catch(JSONException ex){
            LOGGER.debug("Could not parse YAML - continue...");
        }
        
        LOGGER.warn("Could not parse the parameter file. Returning empty set of parameters.");
        return new Properties();        
    }

    @Override
    public Class<URI> getSourceType() { return URI.class; }

    @Override
    public Class<Properties> getTargetType() { return Properties.class; }


    @Override
    public int getTransformationCost(Properties parameters) { return 30; }
    
}
