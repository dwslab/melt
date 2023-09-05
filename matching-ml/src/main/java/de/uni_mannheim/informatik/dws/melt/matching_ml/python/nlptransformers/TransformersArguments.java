package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the arguments for the transformers library.
 * <pre>{@code 
 * TransformersArguments config = new TransformersArguments("do_train", true, "warmup_ratio", 0.2, ...);
 * config.addParameter("logging_strategy", "no");
 * }</pre>
 */
public class TransformersArguments {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformersArguments.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    private final Map<String, Object> config;
    
    public TransformersArguments(){
        this.config = new HashMap<>();
    }
    
    public TransformersArguments(TransformersArguments copyArguments){
        this.config = new HashMap<>(copyArguments.config);
    }
    
    public TransformersArguments(Map<String, Object> config){
        this.config = config;
    }
    
    public TransformersArguments(Object... config){
        this.config = parseExtensions(config);
    }
    
    private static Map<String,Object> parseExtensions(Object[] arr){
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < arr.length; i+=2) {
            if(i+1 >= arr.length){
                LOGGER.error("Uneven number of configuration arguments. Expected are Key1, Value1, Key2, Value2, ..." +
                        ".->Discard last extension");
                break;
            }
            map.put(arr[i].toString(), arr[i + 1]);
        }
        return map;
    }

    public TransformersArguments addAll(TransformersArguments arguments){
        this.config.putAll(arguments.config);
        return this;
    }
    
    public TransformersArguments addAll(Map<String, Object> map){
        this.config.putAll(map);
        return this;
    }
    
    public TransformersArguments addParameter(String key, Object value){
        this.config.put(key, value);
        return this;
    }
    
    public Object getParameter(String key){
        return this.config.get(key);
    }
    
    public Object getParameterOrDefault(String key, Object defaultValue){
        return this.config.getOrDefault(key, defaultValue);
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
