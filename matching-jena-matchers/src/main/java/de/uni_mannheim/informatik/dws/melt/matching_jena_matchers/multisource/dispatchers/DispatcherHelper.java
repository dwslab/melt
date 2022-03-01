package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class which contains some static function which are often used in dispatchers.
 */
public class DispatcherHelper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherHelper.class);
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static Object deepCopy(Object o){
        if(o == null)
            return null;
        //the below fix is due to jackson - see the comment at the end of the file
        if(o instanceof Properties){
            return deepCopy((Properties) o);
        }
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(o), o.getClass());
        } catch (JsonProcessingException ex) {
            LOGGER.error("Could not make a deep copy of instance of {}. Returning null.", o.getClass());
            return null;
        }
    }
    
    public static Properties deepCopy(Properties o){
        if(o == null)
            return null;
        //the below fix is due to jackson - see the comment at the end of the file
        try {
            Map<Object, Object> map = objectMapper.readValue(objectMapper.writeValueAsString(o), new TypeReference<Map<Object,Object>>(){});
            Properties p = new Properties();
            p.putAll(map);
            return p;
        } catch (JsonProcessingException ex) {
            LOGGER.error("Could not make a deep copy of instance of {}. Returning null.", o.getClass());
            return null;
        }
    }
    
    /*
    within jackson TypeFactory.java line 1481, they directly set properties to map<string,string> which is not true
    thus we fix it above
    // 19-Oct-2015, tatu: Bit messy, but we need to 'fix' java.util.Properties here...
    if (rawType == Properties.class) {
        result = MapType.construct(rawType, bindings, superClass, superInterfaces,
                CORE_TYPE_STRING, CORE_TYPE_STRING);
    
    */
}
