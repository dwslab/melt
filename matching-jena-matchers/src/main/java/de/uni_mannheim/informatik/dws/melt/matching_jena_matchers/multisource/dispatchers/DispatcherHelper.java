package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
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
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(o), o.getClass());
        } catch (JsonProcessingException ex) {
            LOGGER.error("Could not make a deep copy of instance of {}. Returning null.", o.getClass());
            return null;
        }
    }
}
