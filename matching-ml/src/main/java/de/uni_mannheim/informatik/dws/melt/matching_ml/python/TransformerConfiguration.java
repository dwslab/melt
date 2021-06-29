package de.uni_mannheim.informatik.dws.melt.matching_ml.python;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the arguments for the huggingface trainer.
 * Any of the training arguments which are <a href="https://huggingface.co/transformers/main_classes/trainer.html#trainingarguments">listed on the documentation</a>
 * can be used.
 * <pre>{@code 
 * TransformerConfiguration config = new TransformerConfiguration("do_train", true, "warmup_ratio", 0.2, ...);
 * config.addParameter("logging_strategy", "no");
 * }</pre>
 */
public class TransformerConfiguration {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerConfiguration.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    private final Map<String, Object> config;
    
    public TransformerConfiguration(){
        this.config = new HashMap<>();
    }
    
    public TransformerConfiguration(Map<String, Object> config){
        this.config = config;
    }
    
    public TransformerConfiguration(Object... config){
        this.config = parseExtensions(config);
    }
    
    private static Map<String,Object> parseExtensions(Object[] arr){
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < arr.length; i+=2) {
            if(i+1 >= arr.length){
                LOGGER.error("Uneven number of configuration arguments. Exepect are Key1, Value1, Key2, Value2, ....->Discard last extension");
                break;
            }
            map.put(arr[i].toString(), arr[i + 1]);
        }
        return map;
    }
    
    
    public TransformerConfiguration addParameter(String key, Object value){
        this.config.put(key, value);
        return this;
    }
    
    public String toJsonString(){
        try {
            return JSON_MAPPER.writeValueAsString(this.config);
        } catch (JsonProcessingException ex) {
            LOGGER.warn("Could not construct JSON string. Sending an empty string. Be warned.", ex);
            return "";
        }
    }
}
