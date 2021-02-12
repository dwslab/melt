package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AbstractTypeTransformer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Properties;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

/**
 * Transforms a URI to java.uril.Properties.
 * Currently supported formats: YAML, JSON.
 */
public class Properties2URLTransformer extends AbstractTypeTransformer<Properties, URL>  {
    private static final String FILE_PREFIX = "params";

    public Properties2URLTransformer() {
        super(Properties.class, URL.class);
    }
    
    @Override
    public URL transform(Properties value, Properties parameters) throws Exception {
        File f;
        if(parameters.getOrDefault(ParameterConfigKeys.DEFAULT_PARAMETERS_SERIALIZATION_FORMAT, "json").toString().toLowerCase().equals("json")){
            f = TypeTransformerHelper.getRandomSerializationFile(parameters, FILE_PREFIX, ".json");
            try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))){
                bw.write(new JSONObject(value).toString());
            }
        }else{
            f = TypeTransformerHelper.getRandomSerializationFile(parameters, FILE_PREFIX, ".yaml");
            try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))){
                bw.write(new Yaml().dump(value));
            }
        }
        return f.toURI().toURL();
    }
}
