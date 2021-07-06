package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper functions for type transformation to URL.
 */
public class TypeTransformerHelper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Properties2URLTransformer.class);
    
    public static File getRandomSerializationFile(Properties parameters, String filePrefix, String fileSuffix) throws IOException{
        String path = parameters.getProperty(ParameterConfigKeys.SERIALIZATION_FOLDER);
        if(path != null){
            File folder = new File(path);
            if(folder.isDirectory()){
                return FileUtil.createFileWithRandomNumber(folder, filePrefix, fileSuffix);
            }
            LOGGER.warn("The parameter key SERIALIZATION_FOLDER is not set to a folder which exists. Defaulting to the system tmp dir.");
        }        
        return File.createTempFile(filePrefix, fileSuffix);
    }    
    
    public static <T> T getOrDefault(Properties parameters, String key, Class<? extends T> type, T defaultValue){        
        Object value = parameters.get(key);
        if(value == null)
            return defaultValue;
        try{
            return type.cast(value);
        }catch(ClassCastException ex){
            LOGGER.warn("The value provided by {} is not of type {}. Defaulting to {}.", key, type, defaultValue);
            return defaultValue;
        }
    }
    
    public static <T> T get(Properties parameters, String key, Class<? extends T> type){        
        Object value = parameters.get(key);
        if(value == null)
            return null;
        try{
            return type.cast(value);
        }catch(ClassCastException ex){
            LOGGER.warn("The value provided by {} is not of type {}. Defaulting to null.", key, type);
            return null;
        }
    }
    
    public static boolean shouldUseOntologyCache(Properties parameters){
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
