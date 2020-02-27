package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ExtensionMap extends HashMap<String, Object> {
    
    @Override
    public Object put(String key, Object value) {
        if(key.contains("#")==false || key.contains("/") == false){
            return super.put(DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + key, value);
        }else{
            return super.put(key, value);
        }
    }
    
    @Override
    public Object get(Object key){
        Object result = super.get(key);
        if(result == null){
            return super.get(DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + key.toString());
        }
        return result;
    }
    
    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        Object result = super.get(key);
        if(result != null){
            return result;
        }
        result = super.get(DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + key.toString());
        if(result != null){
            return result;
        }
        return defaultValue;
    }
    
}
