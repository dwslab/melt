package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AbstractTypeTransformer;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Transforms a URI to java.uril.Properties.
 * Currently supported formats: YAML, JSON.
 */
public class URL2PropertiesTransformer extends AbstractTypeTransformer<URL, Properties>  {
    private static final Logger LOGGER = LoggerFactory.getLogger(URL2PropertiesTransformer.class);
    private static final Gson GSON = new Gson();
    
    public URL2PropertiesTransformer() {
        super(URL.class, Properties.class);
    }
    
    @Override
    public Properties transform(URL value, Properties parameters) throws TypeTransformationException {
        try {
            return parse(value);
        }catch(IOException e){
            throw new TypeTransformationException("Could not transform URL to Properties", e);
        }
    }
    
    public static Properties parse(URL value) throws IOException{
        String content = null;
        try (Scanner scanner = new Scanner(value.openStream(), "UTF-8")) {
            content = scanner.useDelimiter("\\A").next();
        }
        
        if(content == null || content.trim().isEmpty()){
            LOGGER.warn("The content of the URI for the parameters is empty. Returning empty set of parameters.");
            return new Properties();
        }
        
        try{
            Properties p = new Properties();
            p.putAll(GSON.fromJson(content, Map.class));
            return p;
        }catch(JsonSyntaxException ex){
            LOGGER.debug("Could not parse JSON - continue...");
        }
        
        try{
            Properties p = new Properties();
            p.putAll(new Yaml().load(content));
            return p;
        }catch(Exception ex){
            LOGGER.debug("Could not parse YAML - continue...");
        }
        
        LOGGER.warn("Could not parse the parameter file. Returning empty set of parameters.");
        return new Properties();
    }
}
